package com.testsigma.addons.mobileWeb;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
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
        description = "Locate and tap a UI element on a mobile web page using two AI passes: " +
                "Pass 1 locates the element; Pass 2 verifies the green-dot tap location and " +
                "corrects it if needed. Fails if the element is not found.",
        displayName = "Ai: Click on Image/text matching prompt",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = true)
public class ClickOnImageUsingAi extends WebAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    // ── Iteration-2 prompt ────────────────────────────────────────────────────
    private static final String VERIFY_PROMPT_PART1 =
            "You are given two screenshots of the same UI, attached in order:\n" +
                    "  1. CLEAN IMAGE (first attachment) — the original, unaltered screenshot with no overlays.\n" +
                    "  2. ANNOTATED IMAGE (second attachment) — the same screenshot with two overlays " +
                    "added by an automated tool (NOT part of the original UI):\n" +
                    "       • GREEN DOT — the exact intended tap point proposed by a first AI pass.\n" +
                    "       • MAGENTA ARROW — points from the left toward the green dot " +
                    "(arrowhead stops ~20 px to the left of the dot) to indicate the tap location.\n\n" +
                    "Use the clean image to understand the UI. " +
                    "Use the annotated image to find the green dot via the arrow, then evaluate it.\n\n" +
                    "The element we are trying to tap is described as: \"";

    private static final String VERIFY_PROMPT_PART2_STEPS =
            "\"\n\n" +
                    "STEP 1 — Measure the image:\n" +
                    "  Record the raw pixel dimensions as \"imageWidth\" and \"imageHeight\".\n\n" +
                    "STEP 2 — Follow the arrow to the green dot:\n" +
                    "  In the annotated image, follow the magenta arrow to locate the green dot. " +
                    "Cross-reference with the clean image to identify " +
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
        logger.info("=== ClickOnImageUsingAi (Mobile Web): Starting ===");
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

            screenshotFile = AiActionUtils.captureAsJpeg(pageCapture, "ai_mobileweb_capture", logger);

            // ──────────────────────────────────────────────────────────────────────
            // ITERATION 1 — Locate the element in the clean screenshot
            // ──────────────────────────────────────────────────────────────────────
            logger.info("[Iteration 1] Locating element...");
            String aiResponse1 = AiActionUtils.invokeAi(
                    ai, screenshotFile, AiActionUtils.LOCATE_PROMPT_MOBILE_WEB, query, logger);
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
            int aiCx        = node1.path("cx").asInt(0);
            int aiCy        = node1.path("cy").asInt(0);
            int imageWidth  = node1.path("imageWidth").asInt(0);
            int imageHeight = node1.path("imageHeight").asInt(0);
            int conf1       = node1.path("confidence").asInt(50);
            String desc1    = node1.path("description").asText("");

            logger.info(String.format(
                    "[Iteration 1] AI result — bbox: (%d,%d)-(%d,%d) | cx/cy: (%d,%d) | " +
                            "AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    aiX1, aiY1, aiX2, aiY2, aiCx, aiCy, imageWidth, imageHeight, conf1, desc1));

            if (imageWidth <= 0 || imageHeight <= 0) {
                setErrorMessage(String.format(
                        "AI returned invalid image dimensions (imageWidth=%d, imageHeight=%d).",
                        imageWidth, imageHeight));
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(pageCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            int[] cap = AiActionUtils.scaleAiToCapture(aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, captureW, captureH);

            // Use AI's reported visual center (cx/cy) when present; fall back to bbox midpoint.
            int tapX, tapY;
            if (aiCx > 0 && aiCy > 0) {
                tapX = (int) Math.round((double) aiCx / imageWidth  * captureW);
                tapY = (int) Math.round((double) aiCy / imageHeight * captureH);
            } else {
                tapX = (cap[0] + cap[2]) / 2;
                tapY = (cap[1] + cap[3]) / 2;
            }
            // Clamp to bbox so the tap always lands within the detected element
            tapX = Math.max(cap[0], Math.min(cap[2], tapX));
            tapY = Math.max(cap[1], Math.min(cap[3], tapY));

            logger.info(String.format(
                    "[Iteration 1] Device bbox: (%d,%d)-(%d,%d)  tap: (%d,%d)",
                    cap[0], cap[1], cap[2], cap[3], tapX, tapY));

            BufferedImage annotated1 = AiActionUtils.drawHighlight(
                    pageCapture, cap[0], cap[1], cap[2], cap[3], tapX, tapY, Color.MAGENTA);
            annotatedJpegFile = AiActionUtils.captureAsJpeg(annotated1, "ai_mobileweb_annotated", logger);

            // ──────────────────────────────────────────────────────────────────────
            // ITERATION 2 — Verify the GREEN DOT in the annotated screenshot
            // ──────────────────────────────────────────────────────────────────────
            logger.info(String.format(
                    "[Iteration 2] Verifying green-dot at (%d,%d)...", tapX, tapY));
            String verifyPrompt = VERIFY_PROMPT_PART1 + query + VERIFY_PROMPT_PART2_STEPS;
            String aiResponse2  = AiActionUtils.invokeAiWithFiles(
                    ai, List.of(screenshotFile, annotatedJpegFile), verifyPrompt, "", logger);
            JsonNode node2 = AiActionUtils.parseAiJson(aiResponse2, logger);

            String finalDesc = desc1;

            if (node2 != null) {
                boolean accurate = node2.path("accurate").asBoolean(true);
                int     rawX     = node2.path("click_x").asInt(0);
                int     rawY     = node2.path("click_y").asInt(0);
                int     imgW2    = node2.path("imageWidth").asInt(0);
                int     imgH2    = node2.path("imageHeight").asInt(0);
                String  reason   = node2.path("reason").asText("");

                if (rawX > 0 && rawY > 0 && imgW2 > 0 && imgH2 > 0) {
                    int prevX = tapX;
                    int prevY = tapY;

                    if (accurate) {
                        // Dot confirmed correct — use the geometric center of the Iteration 1 bbox.
                        tapX = (cap[0] + cap[2]) / 2;
                        tapY = (cap[1] + cap[3]) / 2;
                    } else {
                        // Dot is on the wrong element — use Iteration 2's corrected coordinates,
                        // clamped to the Iteration 1 bbox as a safety bound.
                        double sx2 = (double) captureW / imgW2;
                        double sy2 = (double) captureH / imgH2;
                        tapX = (int) Math.round(rawX * sx2);
                        tapY = (int) Math.round(rawY * sy2);
                        tapX = Math.max(cap[0], Math.min(cap[2], tapX));
                        tapY = Math.max(cap[1], Math.min(cap[3], tapY));
                    }

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
            }

            // Final annotation: bbox rectangle + crosshair + green dot (no misleading arrow)
            BufferedImage finalAnnotated = AiActionUtils.drawFinalAnnotation(
                    pageCapture, cap[0], cap[1], cap[2], cap[3], tapX, tapY);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(finalAnnotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            // iOS screenshots are physical pixels; PointerInput expects logical UIKit points.
            String platform = (String) ((HasCapabilities) driver).getCapabilities().getCapability("platformName");
            boolean isIos = "iOS".equalsIgnoreCase(platform);

            int finalTapX = tapX;
            int finalTapY = tapY;
            if (isIos) {
                Dimension windowSize = driver.manage().window().getSize();
                finalTapX = (int) Math.round((double) tapX * windowSize.width  / captureW);
                finalTapY = (int) Math.round((double) tapY * windowSize.height / captureH);
                logger.info(String.format(
                        "iOS: physical (%d,%d) → logical (%d,%d)  window=%dx%d",
                        tapX, tapY, finalTapX, finalTapY, windowSize.width, windowSize.height));
            }

            logger.info(String.format("Tapping at (%d,%d)  confidence=%d", finalTapX, finalTapY, conf1));
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), finalTapX, finalTapY));
            tap.addAction(finger.createPointerDown(0));
            tap.addAction(new Pause(finger, Duration.ofMillis(100)));
            tap.addAction(finger.createPointerUp(0));
            ((Interactive) driver).perform(Collections.singletonList(tap));

            setSuccessMessage(String.format(
                    "Successfully tapped '%s' at (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, finalTapX, finalTapY, cap[0], cap[1], cap[2], cap[3], conf1, finalDesc));
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
