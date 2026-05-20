package com.testsigma.addons.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and click a UI element on a web page using two AI passes: " +
                "Pass 1 locates the element; Pass 2 verifies the green-dot click location and " +
                "corrects it if needed. Fails if the element is not found.",
        displayName = "Ai: Click on Image/text matching prompt",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)
public class ClickOnImageUsingAi extends WebAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    // ── Iteration-2 prompt ────────────────────────────────────────────────────
    // The annotated image has two overlays drawn by the tool (NOT part of the UI):
    //   • GREEN DOT   — exact intended click point from Iteration 1
    //   • MAGENTA ARROW — shaft + arrowhead pointing from the left toward the green dot,
    //                     with its tip stopping ~20 px to the left of the dot
    //
    // Build order: PART1 + query + PART2_COORDS + clickX + ", " + clickY + PART2_STEPS
    // Injecting the dot's pixel coordinates lets the AI report the exact offset correction.
    private static final String VERIFY_PROMPT_PART1 =
            "You are given two screenshots of the same UI, attached in order:\n" +
            "  1. CLEAN IMAGE (first attachment) — the original, unaltered screenshot with no overlays.\n" +
            "  2. ANNOTATED IMAGE (second attachment) — the same screenshot with two overlays " +
            "added by an automated tool (NOT part of the original UI):\n" +
            "       • GREEN DOT — the exact intended click point proposed by a first AI pass.\n" +
            "       • MAGENTA ARROW — points from the left toward the green dot " +
            "(arrowhead stops ~20 px to the left of the dot) to indicate the click location.\n\n" +
            "Use the clean image to understand the UI. " +
            "Use the annotated image to find the green dot via the arrow, then evaluate it.\n\n" +
            "The element we are trying to click is described as: \"";

    // Inserted after the query; clickX and clickY are injected here at runtime.
    private static final String VERIFY_PROMPT_PART2_COORDS =
            "\"\n\nThe green dot is currently placed at pixel coordinates (";
    // caller appends: clickX + ", " + clickY + ") in this image.\n\n"

    private static final String VERIFY_PROMPT_PART2_STEPS =
            "STEP 1 — Measure the image:\n" +
            "  Record the raw pixel dimensions as \"imageWidth\" and \"imageHeight\".\n\n" +
            "STEP 2 — Follow the arrow to the green dot:\n" +
            "  In the annotated image, the magenta arrow points at the green dot at the " +
            "coordinates given above. Cross-reference with the clean image to identify " +
            "exactly which UI element the green dot is sitting on.\n" +
            "  • Is the green dot on the CORRECT target element described by the query?\n" +
            "  • Is it at a good clickable position — center of the element, " +
            "not on a border, gap, or neighboring element?\n" +
            "  Set \"accurate\": true only if BOTH conditions are met.\n\n" +
            "STEP 3 — Return the best click location:\n" +
            "  Always return the most precise (click_x, click_y) for the target element.\n" +
            "  • Green dot IS correct → confirm it, return the same or very close coordinates.\n" +
            "  • Green dot is wrong or slightly off → return the corrected center of the " +
            "actual target element.\n" +
            "  The returned point MUST land at the visual center of the target's clickable " +
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
        logger.info("=== ClickOnImageUsingAi (Web): Starting ===");
        File screenshotFile     = null;
        File annotatedJpegFile  = null;
        File finalAnnotatedFile = null;

        // add hardcoded 2 seconds wait before click
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.info("Sleep interrupted: " + e.getMessage());
        }

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage pageCapture = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Physical screenshot: " + captureW + "x" + captureH);

            int[] cssViewport = AiActionUtils.getCssViewport(driver, captureW, captureH, logger);
            int cssW = cssViewport[0];
            int cssH = cssViewport[1];
            logger.info("CSS viewport: " + cssW + "x" + cssH);

            // Resize to CSS viewport before sending to AI (eliminates Retina/HiDPI scaling errors)
            BufferedImage analysisImage = AiActionUtils.resizeImage(pageCapture, cssW, cssH);
            logger.info("Analysis image: " + analysisImage.getWidth() + "x" + analysisImage.getHeight());

            screenshotFile = AiActionUtils.captureAsJpeg(analysisImage, "ai_web_capture", logger);

            // ──────────────────────────────────────────────────────────────────────
            // ITERATION 1 — Locate the element in the clean screenshot
            // ──────────────────────────────────────────────────────────────────────
            logger.info("[Iteration 1] Locating element...");
            String aiResponse1 = AiActionUtils.invokeAi(
                    ai, screenshotFile, AiActionUtils.LOCATE_PROMPT_WEB, query, logger);
            JsonNode node1 = AiActionUtils.parseAiJson(aiResponse1, logger);

            if (node1 == null) {
                setErrorMessage("Failed to get image response from AI (contact support)");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(analysisImage, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            if (!node1.path("found").asBoolean(false)) {
                String reason = node1.path("description").asText("element not found");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(analysisImage, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                setErrorMessage("AI could not locate '" + query + "': " + reason);
                return Result.FAILED;
            }

            int imgW1 = node1.path("imageWidth").asInt(0);
            int imgH1 = node1.path("imageHeight").asInt(0);
            if (imgW1 <= 0 || imgH1 <= 0) {
                setErrorMessage(String.format("AI returned invalid image dimensions (%dx%d).", imgW1, imgH1));
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(analysisImage, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);
                return Result.FAILED;
            }

            double sx1 = (double) cssW / imgW1;
            double sy1 = (double) cssH / imgH1;
            int cX1    = (int) Math.round(node1.path("x1").asInt(0) * sx1);
            int cY1    = (int) Math.round(node1.path("y1").asInt(0) * sy1);
            int cX2    = (int) Math.round(node1.path("x2").asInt(0) * sx1);
            int cY2    = (int) Math.round(node1.path("y2").asInt(0) * sy1);
            int clickX  = (cX1 + cX2) / 2;
            int clickY  = (cY1 + cY2) / 2;
            int conf1   = node1.path("confidence").asInt(50);
            String desc1 = node1.path("description").asText("");

            logger.info(String.format(
                    "[Iteration 1] bbox: (%d,%d)-(%d,%d) | CSS click: (%d,%d) | " +
                    "scale: (%.3f,%.3f) | confidence: %d | desc: '%s'",
                    cX1, cY1, cX2, cY2, clickX, clickY, sx1, sy1, conf1, desc1));

            BufferedImage annotated1 = AiActionUtils.drawHighlight(
                    analysisImage, cX1, cY1, cX2, cY2, clickX, clickY, Color.MAGENTA);
            annotatedJpegFile = AiActionUtils.captureAsJpeg(annotated1, "ai_web_annotated", logger);

            // ──────────────────────────────────────────────────────────────────────
            // ITERATION 2 — Verify the GREEN DOT in the annotated screenshot
            // The AI sees the magenta box + green dot and judges whether the dot is
            // on the correct element, then returns the best click_x / click_y.
            // ──────────────────────────────────────────────────────────────────────
            // Embed the dot's current pixel coordinates into the prompt so the AI knows
            // exactly where the green dot is and can quantify any correction offset.
            logger.info(String.format(
                    "[Iteration 2] Verifying green-dot at CSS (%d,%d)...", clickX, clickY));
            String verifyPrompt = VERIFY_PROMPT_PART1 + query
                    + VERIFY_PROMPT_PART2_COORDS
                    + clickX + ", " + clickY + ") in this image.\n\n"
                    + VERIFY_PROMPT_PART2_STEPS;
            String aiResponse2  = AiActionUtils.invokeAiWithFiles(
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
                    double sx2  = (double) cssW / imgW2;
                    double sy2  = (double) cssH / imgH2;
                    int prevX   = clickX;
                    int prevY   = clickY;
                    clickX = (int) Math.round(rawX * sx2);
                    clickY = (int) Math.round(rawY * sy2);
                    int dx = clickX - prevX;
                    int dy = clickY - prevY;
                    boolean corrected = Math.abs(dx) > 2 || Math.abs(dy) > 2;
                    finalDesc = reason;

                    logger.info(String.format(
                            "[Iteration 2] accurate: %b | corrected: %b | " +
                            "dot was (%d,%d) → corrected to (%d,%d) | delta: dx=%d dy=%d | reason: '%s'",
                            accurate, corrected, prevX, prevY, clickX, clickY, dx, dy, reason));
                } else {
                    logger.info("[Iteration 2] Invalid coordinates in response — using Iteration 1 result.");
                }
            } else {
                logger.info("[Iteration 2] No usable result — using Iteration 1 coordinates.");
            }

            // Upload the final annotated image: green dot at the definitive click location
            BufferedImage finalAnnotated = AiActionUtils.drawHighlight(
                    analysisImage, cX1, cY1, cX2, cY2, clickX, clickY, Color.MAGENTA);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(finalAnnotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            logger.info(String.format("Clicking at CSS (%d,%d)  confidence=%d", clickX, clickY, finalConf));
            new Actions(driver).moveToLocation(clickX, clickY).click().perform();

            setSuccessMessage(String.format(
                    "Successfully clicked '%s' at CSS (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, clickX, clickY, cX1, cY1, cX2, cY2, finalConf, finalDesc));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to click using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(screenshotFile);
            AiActionUtils.deleteQuietly(annotatedJpegFile);
            AiActionUtils.deleteQuietly(finalAnnotatedFile);
        }
    }
}
