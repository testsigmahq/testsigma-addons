package com.testsigma.addons.ios;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and tap a UI element on an iOS device using two AI passes: " +
                "Pass 1 locates the element; Pass 2 verifies the green-dot tap location and " +
                "corrects it if needed. Fails if the element is not found.",
        displayName = "Ai: Click on Image/text matching prompt",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = true)
public class ClickOnImageUsingAi extends IOSAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    // ── Iteration-2 prompt ────────────────────────────────────────────────────
    // Build order: PART1 + query + PART2_COORDS + tapX + ", " + tapY + PART2_STEPS
    private static final String VERIFY_PROMPT_PART1 =
            "You are given two screenshots of the same iOS screen, attached in order:\n" +
            "  1. CLEAN IMAGE (first attachment) — the original, unaltered screenshot with no overlays.\n" +
            "  2. ANNOTATED IMAGE (second attachment) — the same screenshot with two overlays " +
            "added by an automated tool (NOT part of the original UI):\n" +
            "       • GREEN DOT — the exact intended tap point proposed by a first AI pass.\n" +
            "       • MAGENTA ARROW — points from the left toward the green dot " +
            "(arrowhead stops ~20 px to the left of the dot) to indicate the tap location.\n\n" +
            "Use the clean image to understand the UI. " +
            "Use the annotated image to find the green dot via the arrow, then evaluate it.\n\n" +
            "The element we are trying to tap is described as: \"";

    private static final String VERIFY_PROMPT_PART2_COORDS =
            "\"\n\nThe green dot is currently placed at pixel coordinates (";

    // %d %d → captureW, captureH injected at runtime so AI uses exact dimensions
    private static final String VERIFY_PROMPT_STEPS_FMT =
            "The screenshot dimensions are exactly %d × %d pixels (width × height).\n\n" +
            "STEP 1 — Image dimensions:\n" +
            "  The exact pixel dimensions are already stated above. " +
            "Use those values verbatim as \"imageWidth\" and \"imageHeight\" in your output — do NOT guess or remeasure.\n\n" +
            "STEP 2 — Follow the arrow to the green dot:\n" +
            "  In the annotated image, the magenta arrow points at the green dot at the " +
            "coordinates given above. Cross-reference with the clean image to identify " +
            "exactly which UI element the green dot is sitting on.\n" +
            "  • Is the green dot on the CORRECT target element described by the query?\n" +
            "  • Is it at a good tappable position — center of the element, " +
            "not on a border, gap, or neighboring element?\n" +
            "  Set \"accurate\": true only if BOTH conditions are met.\n\n" +
            "STEP 3 — Return the best tap location:\n" +
            "  Always return the most precise (click_x, click_y) for the target element.\n" +
            "  • Green dot IS correct → confirm it, return the same or very close coordinates.\n" +
            "  • Green dot is wrong or slightly off → return the corrected center of the " +
            "actual target element.\n" +
            "  The returned point MUST land at the visual center of the target's tappable " +
            "area and must NOT fall on any adjacent or neighboring element.\n" +
            "  In the \"reason\" field, state which element the dot is on, whether it was " +
            "correct, and if corrected — by how many pixels it moved (dx, dy).\n\n" +
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
            "{\"accurate\": <bool>, " +
            "\"click_x\": <int>, \"click_y\": <int>, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"reason\": \"<element the dot is on; accurate or corrected; if corrected: dx=N dy=N>\"}";

    @Override
    public Result execute() {
        logger.info("=== ClickOnImageUsingAi (iOS): Starting ===");
        File screenshotFile     = null;
        File annotatedJpegFile  = null;
        File finalAnnotatedFile = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Device screenshot size: " + captureW + "x" + captureH);

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_ios_capture", logger);

            // ──────────────────────────────────────────────────────────────────────
            // ITERATION 1 — Locate the element in the clean screenshot
            // ──────────────────────────────────────────────────────────────────────
            logger.info("[Iteration 1] Locating element...");
            String aiResponse1 = AiActionUtils.invokeAiAnthropicFirst(
                    ai, screenshotFile, AiActionUtils.LOCATE_PROMPT_IOS, query, logger);
            JsonNode node1 = AiActionUtils.parseAiJson(aiResponse1, logger);

            if (node1 == null) {
                setErrorMessage("Failed to get image response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            if (!node1.path("found").asBoolean(false)) {
                String reason = node1.path("description").asText("element not found");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                setErrorMessage("AI could not locate '" + query + "': " + reason);
                return Result.FAILED;
            }

            int aiX1        = node1.path("x1").asInt(0);
            int aiY1        = node1.path("y1").asInt(0);
            int aiX2        = node1.path("x2").asInt(0);
            int aiY2        = node1.path("y2").asInt(0);
            int imageWidth  = node1.path("imageWidth").asInt(0);
            int imageHeight = node1.path("imageHeight").asInt(0);
            int conf1       = node1.path("confidence").asInt(50);
            String desc1    = node1.path("description").asText("");

            logger.info(String.format(
                    "[Iteration 1] AI result — bbox: (%d,%d)-(%d,%d) | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, conf1, desc1));

            if (imageWidth <= 0 || imageHeight <= 0) {
                setErrorMessage(String.format(
                        "AI returned invalid image dimensions (imageWidth=%d, imageHeight=%d).",
                        imageWidth, imageHeight));
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);
            int tapX = (cap[0] + cap[2]) / 2;
            int tapY = (cap[1] + cap[3]) / 2;
            logger.info(String.format(
                    "[Iteration 1] Device bbox: (%d,%d)-(%d,%d)  tap: (%d,%d)",
                    cap[0], cap[1], cap[2], cap[3], tapX, tapY));

            BufferedImage annotated1 = AiActionUtils.drawHighlight(
                    pageCapture, cap[0], cap[1], cap[2], cap[3], tapX, tapY, Color.MAGENTA);
            annotatedJpegFile = AiActionUtils.captureAsJpeg(annotated1, "ai_ios_annotated", logger);

            /*// ──────────────────────────────────────────────────────────────────────
            // ITERATION 2 — Verify the GREEN DOT in the annotated screenshot
            // ──────────────────────────────────────────────────────────────────────
            logger.info(String.format(
                    "[Iteration 2] Verifying green-dot at physical (%d,%d)...", tapX, tapY));
            String verifyPrompt = VERIFY_PROMPT_PART1 + query
                    + VERIFY_PROMPT_PART2_COORDS
                    + tapX + ", " + tapY + ") in this image.\n\n"
                    + String.format(VERIFY_PROMPT_STEPS_FMT, captureW, captureH);
            String aiResponse2 = AiActionUtils.invokeAiWithFilesAnthropicFirst(
                    ai, List.of(screenshotFile, annotatedJpegFile), verifyPrompt, "", logger);
            JsonNode node2 = AiActionUtils.parseAiJson(aiResponse2, logger);

            String finalDesc = desc1;
            int    finalConf = conf1;

            if (node2 != null) {
                boolean accurate = node2.path("accurate").asBoolean(true);
                int     rawX     = node2.path("click_x").asInt(0);
                int     rawY     = node2.path("click_y").asInt(0);
                int     imgW2    = node2.path("imageWidth").asInt(0);
                int     imgH2    = node2.path("imageHeight").asInt(0);
                String  reason   = node2.path("reason").asText("");

                if (rawX > 0 && rawY > 0 && imgW2 > 0 && imgH2 > 0) {
                    double sx2    = (double) captureW / imgW2;
                    double sy2    = (double) captureH / imgH2;
                    int prevX     = tapX;
                    int prevY     = tapY;
                    tapX = (int) Math.round(rawX * sx2);
                    tapY = (int) Math.round(rawY * sy2);
                    int dx = tapX - prevX;
                    int dy = tapY - prevY;
                    boolean corrected = Math.abs(dx) > 2 || Math.abs(dy) > 2;
                    finalDesc = reason;

                    logger.info(String.format(
                            "[Iteration 2] accurate: %b | corrected: %b | " +
                            "dot was (%d,%d) → corrected to (%d,%d) | delta: dx=%d dy=%d | reason: '%s'",
                            accurate, corrected, prevX, prevY, tapX, tapY, dx, dy, reason));
                } else {
                    logger.info("[Iteration 2] Invalid coordinates in response — using Iteration 1 result.");
                }
            } else {
                logger.info("[Iteration 2] No usable result — using Iteration 1 coordinates.");
            }*/

            // Upload the final annotated image: green dot at the definitive tap location
            BufferedImage finalAnnotated = AiActionUtils.drawHighlight(
                    pageCapture, cap[0], cap[1], cap[2], cap[3], tapX, tapY, Color.MAGENTA);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(finalAnnotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            // Screenshot is in physical pixels; PointerInput expects logical points (UIKit).
            // Derive the pixel ratio from the driver's logical window size.
            IOSDriver iosDriver = (IOSDriver) driver;
            Dimension windowSize = iosDriver.manage().window().getSize();
            int logicalTapX = (int) Math.round((double) tapX * windowSize.width  / captureW);
            int logicalTapY = (int) Math.round((double) tapY * windowSize.height / captureH);
            logger.info(String.format(
                    "Tapping at physical (%d,%d) → logical (%d,%d)  window=%dx%d  confidence=%d",
                    tapX, tapY, logicalTapX, logicalTapY,
                    windowSize.width, windowSize.height, conf1));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), logicalTapX, logicalTapY));
            tap.addAction(finger.createPointerDown(0));
            tap.addAction(new Pause(finger, Duration.ofMillis(500)));
            tap.addAction(finger.createPointerUp(0));
            ((Interactive) iosDriver).perform(Collections.singletonList(tap));

            setSuccessMessage(String.format(
                    "Successfully tapped '%s' at logical (%d,%d) | physical (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, logicalTapX, logicalTapY, tapX, tapY,
                    cap[0], cap[1], cap[2], cap[3], conf1, desc1));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to tap using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(annotatedJpegFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
