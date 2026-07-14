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
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Locates and taps a UI element on an iOS device using a coarse-locate + zoomed-crop-refine +
 * zoomed-crop-verify AI pipeline for pixel-accurate taps.
 *
 * The previous single-pass implementation had two problems, found by comparing it against the
 * working mobile-web equivalent ({@code com.testsigma.addons.mobileWeb.ClickOnImageUsingAi}):
 *
 *  1) It never downscaled the screenshot before sending it to the AI, and told the model to trust
 *     and echo back the RAW capture dimensions (often 3-4 MP on retina iPhones). Claude's vision
 *     input silently downscales large images internally ({@link AiActionUtils#MAX_IMAGE_EDGE} long
 *     edge / {@link AiActionUtils#MAX_IMAGE_AREA} area), so the model ended up reasoning on a
 *     resolution it was never told about, while the code mapped its answer back using the wrong
 *     (too large) dimensions — a systematic miscalibration. This is fixed by resizing to a
 *     known-safe size ourselves (via {@link AiActionUtils#fitWithinAiLimits}) and telling the AI
 *     those exact dimensions up front instead of asking it to measure them.
 *  2) It always called {@code getScreenshotAs(BYTES)} with no fallback; some iOS driver/WDA combos
 *     need BASE64 instead — handled by {@link AiActionUtils#captureScreenshotWithFallback}.
 *
 * On top of that fix, this pipeline improves click PRECISION (as opposed to gross identification)
 * via a crop-and-zoom refine/verify pass: the same percentage pixel-estimate error from the model
 * becomes a much smaller physical-pixel error once the target area is magnified.
 *
 * Pipeline:
 *  0) CAPTURE — screenshot via {@link AiActionUtils#captureScreenshotWithFallback}.
 *  1) LOCATE  — resize to Claude's real vision limits, coarse-locate on that, scale back to full
 *               capture space (retried once if not found).
 *  2) CROP+ZOOM — crop around the coarse center and upscale it (also capped to vision limits).
 *  3) REFINE  — precise tap point re-located from scratch on the zoomed crop.
 *  4) VERIFY  — dot-overlay double-check on the same zoomed crop, with correction if needed.
 *  5) TAP     — map back to full-image/physical/logical coordinates and tap, preferring the
 *               native "mobile: tap" command with a W3C pointer-action fallback.
 */
@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and tap a UI element on an iOS device using a coarse-locate + " +
                "zoomed-crop-refine + zoomed-crop-verify AI pipeline for pixel-accurate taps. " +
                "Retries the initial locate once before failing if the element isn't found.",
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

    private static final int MIN_CROP_SIZE = 240;
    private static final int MAX_CROP_SIZE = 900;
    private static final int CROP_PADDING_MULTIPLIER = 4;
    private static final int ZOOM_TARGET_LONG_SIDE = 1200;
    private static final double MAX_ZOOM_SCALE = 4.0;
    private static final float JPEG_QUALITY = 0.97f;

    private static final String REFINE_PROMPT_PART1 =
            "You are a UI element locator working on a ZOOMED-IN CROP of a larger iOS screenshot. " +
                    "This crop was extracted around the approximate location of a target element found " +
                    "by an earlier, coarser pass — the element you are looking for should be visible " +
                    "somewhere in this image, but may not be perfectly centered.\n\n" +
                    "The element to find is described as: \"";

    private static final String REFINE_PROMPT_PART2 =
            "\"\n\n" +
                    "STEP 1 — Use the given image dimensions:\n" +
                    "  The exact pixel dimensions of THIS CROPPED image are stated at the top of this " +
                    "prompt (see IMAGE DIMENSIONS). Do NOT guess or re-measure them. Return coordinates " +
                    "in that pixel space and echo the given \"imageWidth\"/\"imageHeight\".\n\n" +
                    "STEP 2 — Locate the element precisely:\n" +
                    "  Because this image is a zoomed-in crop, you can be far more precise than on a " +
                    "full-screen screenshot. Find the exact visual center of the described element's " +
                    "tappable area.\n" +
                    "  If the element is NOT visible anywhere in this crop, set \"found\" to false — do " +
                    "not guess.\n\n" +
                    "STEP 3 — Return the precise tap point:\n" +
                    "  Report the single best (click_x, click_y) point to tap — the visual center of the " +
                    "element, accounting for padding/text centering, never on a border, gap, or a " +
                    "neighboring element.\n\n" +
                    "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
                    "If found:\n" +
                    "  {\"found\": true, \"click_x\": <int>, \"click_y\": <int>, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": <0-100>, \"reason\": \"<what the element is and why this point>\"}\n" +
                    "If NOT found in this crop:\n" +
                    "  {\"found\": false, \"click_x\": 0, \"click_y\": 0, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": 0, \"reason\": \"<why it is not visible in this crop>\"}";

    private static final String VERIFY_PROMPT_PART1 =
            "You are given two images, attached in order, both showing the SAME ZOOMED-IN CROP of a " +
                    "larger iOS screenshot:\n" +
                    "  1. CLEAN CROP (first attachment) — the crop with no overlays.\n" +
                    "  2. ANNOTATED CROP (second attachment) — the same crop with a GREEN DOT added by " +
                    "an automated tool (NOT part of the original UI) marking a proposed tap point.\n\n" +
                    "Use the clean crop to understand the UI. Use the annotated crop to evaluate the " +
                    "green dot.\n\n" +
                    "The element we are trying to tap is described as: \"";

    private static final String VERIFY_PROMPT_PART2 =
            "\"\n\n" +
                    "STEP 1 — Use the given image dimensions:\n" +
                    "  Both images share the exact pixel dimensions stated at the top of this prompt " +
                    "(see IMAGE DIMENSIONS). Do NOT guess or re-measure them. Return coordinates in that " +
                    "pixel space and echo the given \"imageWidth\"/\"imageHeight\".\n\n" +
                    "STEP 2 — Evaluate the green dot:\n" +
                    "  • Is the green dot on the CORRECT target element described above?\n" +
                    "  • Is it at a good tappable position — center of the element, not on a border, " +
                    "gap, or a neighboring element?\n" +
                    "  Set \"accurate\": true only if BOTH conditions are met.\n\n" +
                    "STEP 3 — Return the best tap location:\n" +
                    "  Because this is a zoomed-in crop, be as precise as possible.\n" +
                    "  • Dot is correct → confirm it, return the same or very close coordinates.\n" +
                    "  • Dot is wrong or off → return the corrected center of the actual target element " +
                    "within this same crop. If the target element is not visible in this crop at all, " +
                    "set \"accurate\" to false and \"outOfCrop\" to true.\n\n" +
                    "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
                    "{\"accurate\": <bool>, \"outOfCrop\": <bool>, " +
                    "\"click_x\": <int>, \"click_y\": <int>, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"reason\": \"<element the dot is on; accurate or corrected; if corrected: dx=N dy=N>\"}";

    @Override
    public Result execute() {
        logger.info("=== ClickOnImageUsingAi (iOS): Starting ===");
        File fullShotFile      = null;
        File zoomCleanFile     = null;
        File zoomAnnotatedFile = null;
        File finalResultFile   = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            IOSDriver iosDriver = (IOSDriver) driver;
            Dimension windowSize = iosDriver.manage().window().getSize();
            logger.info("Device logical window size: " + windowSize.width + "x" + windowSize.height);

            BufferedImage fullCapture = null;
            int captureW = 0;
            int captureH = 0;
            int sentW = 0;
            int sentH = 0;
            JsonNode locateNode = null;
            int attempts = 0;

            // ── PASS 1 — coarse locate on a resized screenshot, retried once ───
            while (attempts < 2) {
                attempts++;
                fullCapture = AiActionUtils.captureScreenshotWithFallback((TakesScreenshot) driver, logger);
                captureW = fullCapture.getWidth();
                captureH = fullCapture.getHeight();
                logger.info("Device screenshot size: " + captureW + "x" + captureH);

                // Resize BEFORE sending so we control the exact pixel space the AI reasons in —
                // raw iOS retina captures routinely exceed Claude's real vision-input limits, and
                // relying on its internal auto-downscale (while telling it the raw dimensions)
                // causes systematic coordinate drift.
                int[] sent = AiActionUtils.fitWithinAiLimits(captureW, captureH);
                sentW = sent[0];
                sentH = sent[1];
                BufferedImage aiImage = (sentW == captureW && sentH == captureH)
                        ? fullCapture : AiActionUtils.resizeImage(fullCapture, sentW, sentH);
                logger.info(String.format("[Pass 1] capture %dx%d -> sent %dx%d",
                        captureW, captureH, sentW, sentH));

                AiActionUtils.deleteQuietly(fullShotFile);
                fullShotFile = AiActionUtils.captureAsJpeg(aiImage, "ai_ios_capture", JPEG_QUALITY, logger);

                logger.info("[Pass 1 attempt " + attempts + "] Coarse locate on resized screenshot...");
                String locateResponse = AiActionUtils.invokeAiAnthropicFirst(
                        ai, fullShotFile, AiActionUtils.LOCATE_PROMPT_IOS, query, sentW, sentH, logger);
                locateNode = AiActionUtils.parseAiJson(locateResponse, logger);

                if (locateNode != null && locateNode.path("found").asBoolean(false)) {
                    break;
                }
                if (attempts < 2) {
                    logger.info("[Pass 1] Element not found — retrying once after a short settle delay.");
                    Thread.sleep(600);
                }
            }

            if (locateNode == null) {
                setErrorMessage("Failed to get a response from AI while locating the element (contact support)");
                finalResultFile = ScreenshotUtils.saveScreenshotToFile(fullCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalResultFile, logger);
                return Result.FAILED;
            }
            if (!locateNode.path("found").asBoolean(false)) {
                String reason = locateNode.path("description").asText("element not found");
                finalResultFile = ScreenshotUtils.saveScreenshotToFile(fullCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalResultFile, logger);
                setErrorMessage("AI could not locate '" + query + "': " + reason);
                return Result.FAILED;
            }

            int rawX1 = locateNode.path("x1").asInt(0);
            int rawY1 = locateNode.path("y1").asInt(0);
            int rawX2 = locateNode.path("x2").asInt(0);
            int rawY2 = locateNode.path("y2").asInt(0);
            int rawCx = locateNode.path("cx").asInt(0);
            int rawCy = locateNode.path("cy").asInt(0);
            int conf1 = locateNode.path("confidence").asInt(50);
            String desc1 = locateNode.path("description").asText("");

            // The AI reasoned in the RESIZED (sentW x sentH) pixel space we told it about — scale
            // its bbox back up to the full-capture pixel space using the known sent/capture ratio,
            // rather than trusting any imageWidth/imageHeight it echoed back.
            int[] scaledBox = AiActionUtils.scaleAiToCapture(rawX1, rawY1, rawX2, rawY2, sentW, sentH, captureW, captureH);
            int lx1 = clamp(scaledBox[0], 0, captureW - 1);
            int ly1 = clamp(scaledBox[1], 0, captureH - 1);
            int lx2 = clamp(scaledBox[2], 0, captureW - 1);
            int ly2 = clamp(scaledBox[3], 0, captureH - 1);
            if (lx2 <= lx1) lx2 = Math.min(captureW - 1, lx1 + 1);
            if (ly2 <= ly1) ly2 = Math.min(captureH - 1, ly1 + 1);

            int coarseCx, coarseCy;
            if (rawCx > 0 && rawCy > 0) {
                coarseCx = clamp((int) Math.round((double) rawCx / sentW * captureW), 0, captureW - 1);
                coarseCy = clamp((int) Math.round((double) rawCy / sentH * captureH), 0, captureH - 1);
            } else {
                coarseCx = (lx1 + lx2) / 2;
                coarseCy = (ly1 + ly2) / 2;
            }

            logger.info(String.format(
                    "[Pass 1] bbox: (%d,%d)-(%d,%d) | center: (%d,%d) | confidence: %d | desc: '%s'",
                    lx1, ly1, lx2, ly2, coarseCx, coarseCy, conf1, desc1));

            // ── Build a crop around the coarse location and upscale it ─────────
            int elementSize = Math.max(lx2 - lx1, ly2 - ly1);
            int cropSize = clamp(elementSize * CROP_PADDING_MULTIPLIER, MIN_CROP_SIZE, MAX_CROP_SIZE);

            int cropX1 = clamp(coarseCx - cropSize / 2, 0, Math.max(0, captureW - 1));
            int cropY1 = clamp(coarseCy - cropSize / 2, 0, Math.max(0, captureH - 1));
            int cropW = Math.max(1, Math.min(cropSize, captureW - cropX1));
            int cropH = Math.max(1, Math.min(cropSize, captureH - cropY1));

            BufferedImage crop = cropRegion(fullCapture, cropX1, cropY1, cropW, cropH);

            double desiredScale = Math.max(1.0, Math.min(MAX_ZOOM_SCALE,
                    (double) ZOOM_TARGET_LONG_SIDE / Math.max(cropW, cropH)));
            int desiredZoomW = (int) Math.round(cropW * desiredScale);
            int desiredZoomH = (int) Math.round(cropH * desiredScale);
            // Cap to the same real vision limits used for Pass 1 — a near-square crop at
            // MAX_CROP_SIZE upscaled by desiredScale can otherwise exceed Claude's ~1.15 MP cap.
            int[] zoomFit = AiActionUtils.fitWithinAiLimits(desiredZoomW, desiredZoomH);
            int zoomW = zoomFit[0];
            int zoomH = zoomFit[1];
            BufferedImage zoomed = AiActionUtils.resizeImage(crop, zoomW, zoomH);

            // Compute actual applied scale per axis (may differ minutely due to rounding) so the
            // reverse mapping back to full-capture space stays exact.
            double zoomScaleX = (double) zoomW / cropW;
            double zoomScaleY = (double) zoomH / cropH;

            logger.info(String.format(
                    "[Crop] region: (%d,%d) %dx%d | zoomed to %dx%d (scaleX=%.2f scaleY=%.2f)",
                    cropX1, cropY1, cropW, cropH, zoomW, zoomH, zoomScaleX, zoomScaleY));

            zoomCleanFile = AiActionUtils.captureAsJpeg(zoomed, "ai_ios_zoom_clean", JPEG_QUALITY, logger);

            // ── PASS 2 — precise refine on the zoomed crop ──────────────────────
            logger.info("[Pass 2] Refining tap point on zoomed crop...");
            String refinePrompt = REFINE_PROMPT_PART1 + query + REFINE_PROMPT_PART2;
            String refineResponse = AiActionUtils.invokeAiAnthropicFirst(
                    ai, zoomCleanFile, refinePrompt, "", zoomW, zoomH, logger);
            JsonNode refineNode = AiActionUtils.parseAiJson(refineResponse, logger);

            int zoomTapX = clamp((int) Math.round((coarseCx - cropX1) * zoomScaleX), 0, zoomW - 1);
            int zoomTapY = clamp((int) Math.round((coarseCy - cropY1) * zoomScaleY), 0, zoomH - 1);
            int conf2 = conf1;
            String desc2 = desc1;

            if (refineNode != null && refineNode.path("found").asBoolean(true)) {
                int rx = refineNode.path("click_x").asInt(0);
                int ry = refineNode.path("click_y").asInt(0);
                if (rx > 0 && ry > 0) {
                    zoomTapX = clamp(rx, 0, zoomW - 1);
                    zoomTapY = clamp(ry, 0, zoomH - 1);
                    conf2 = refineNode.path("confidence").asInt(conf1);
                    desc2 = refineNode.path("reason").asText(desc1);
                }
            } else {
                logger.info("[Pass 2] Refine pass did not confirm the element in the crop — " +
                        "keeping the coarse (re-projected) center as the working point.");
            }

            logger.info(String.format("[Pass 2] zoom tap: (%d,%d) | confidence: %d | desc: '%s'",
                    zoomTapX, zoomTapY, conf2, desc2));

            // ── PASS 3 — verify the proposed point with a dot overlay, on the zoom ─
            BufferedImage zoomAnnotated = AiActionUtils.drawHighlight(
                    zoomed, 0, 0, 0, 0, zoomTapX, zoomTapY, Color.GREEN);
            zoomAnnotatedFile = AiActionUtils.captureAsJpeg(zoomAnnotated, "ai_ios_zoom_annotated", JPEG_QUALITY, logger);

            logger.info("[Pass 3] Verifying tap point via dot overlay on zoomed crop...");
            String verifyPrompt = VERIFY_PROMPT_PART1 + query + VERIFY_PROMPT_PART2;
            String verifyResponse = AiActionUtils.invokeAiWithFilesAnthropicFirst(
                    ai, List.of(zoomCleanFile, zoomAnnotatedFile), verifyPrompt, "",
                    zoomW, zoomH, logger);
            JsonNode verifyNode = AiActionUtils.parseAiJson(verifyResponse, logger);

            String finalDesc = desc2;
            if (verifyNode != null && !verifyNode.path("outOfCrop").asBoolean(false)) {
                boolean accurate = verifyNode.path("accurate").asBoolean(true);
                int vx = verifyNode.path("click_x").asInt(0);
                int vy = verifyNode.path("click_y").asInt(0);
                String reason = verifyNode.path("reason").asText(finalDesc);

                if (!accurate && vx > 0 && vy > 0) {
                    int prevX = zoomTapX;
                    int prevY = zoomTapY;
                    zoomTapX = clamp(vx, 0, zoomW - 1);
                    zoomTapY = clamp(vy, 0, zoomH - 1);
                    logger.info(String.format(
                            "[Pass 3] corrected: (%d,%d) -> (%d,%d) | reason: '%s'",
                            prevX, prevY, zoomTapX, zoomTapY, reason));
                } else {
                    logger.info("[Pass 3] accurate=" + accurate + " | reason: '" + reason + "'");
                }
                finalDesc = reason;
            } else if (verifyNode != null) {
                logger.info("[Pass 3] Verification reports the element is out of this crop — " +
                        "keeping Pass 2's tap point.");
            }

            // ── Map final zoom-space point back to full-image physical pixels ──
            int tapX = clamp(cropX1 + (int) Math.round(zoomTapX / zoomScaleX), 0, captureW - 1);
            int tapY = clamp(cropY1 + (int) Math.round(zoomTapY / zoomScaleY), 0, captureH - 1);

            logger.info(String.format("[Final] physical tap point: (%d,%d)", tapX, tapY));

            BufferedImage finalAnnotated = AiActionUtils.drawFinalAnnotation(
                    fullCapture, lx1, ly1, lx2, ly2, tapX, tapY);
            finalResultFile = ScreenshotUtils.saveScreenshotToFile(finalAnnotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalResultFile, logger);

            // iOS screenshot is in physical pixels; taps need logical points (UIKit).
            int logicalTapX = (int) Math.round((double) tapX * windowSize.width / captureW);
            int logicalTapY = (int) Math.round((double) tapY * windowSize.height / captureH);
            logger.info(String.format(
                    "Tapping at physical (%d,%d) -> logical (%d,%d)  window=%dx%d",
                    tapX, tapY, logicalTapX, logicalTapY, windowSize.width, windowSize.height));

            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }

            performTap(iosDriver, logicalTapX, logicalTapY);

            setSuccessMessage(String.format(
                    "Successfully tapped '%s' at logical (%d,%d) | physical (%d,%d) | confidence=%d | %s",
                    query, logicalTapX, logicalTapY, tapX, tapY, conf2, finalDesc));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to tap using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(fullShotFile);
            AiActionUtils.deleteQuietly(zoomCleanFile);
            AiActionUtils.deleteQuietly(zoomAnnotatedFile);
            AiActionUtils.deleteQuietly(finalResultFile);
        }
    }

    /**
     * Prefers the XCUITest "mobile: tap" native command (the officially recommended, most
     * reliable tap for iOS — it handles custom controls/webviews that raw W3C pointer sequences
     * sometimes miss) and falls back to a manual W3C touch action if it's unsupported.
     */
    private void performTap(IOSDriver iosDriver, int x, int y) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("x", x);
            params.put("y", y);
            iosDriver.executeScript("mobile: tap", params);
            logger.info("Tap executed via 'mobile: tap'.");
        } catch (Exception mobileTapEx) {
            logger.info("'mobile: tap' failed (" + mobileTapEx.getMessage() + "); falling back to W3C touch action.");
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(0));
            tap.addAction(new Pause(finger, Duration.ofMillis(80)));
            tap.addAction(finger.createPointerUp(0));
            ((Interactive) iosDriver).perform(Collections.singletonList(tap));
        }
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private static BufferedImage cropRegion(BufferedImage src, int x, int y, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, w, h, x, y, x + w, y + h, null);
        g.dispose();
        return out;
    }
}
