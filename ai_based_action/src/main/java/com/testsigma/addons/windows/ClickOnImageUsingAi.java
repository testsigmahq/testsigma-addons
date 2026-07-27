package com.testsigma.addons.windows;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Locates and clicks a UI element on a Windows application using the same coarse-locate +
 * zoomed-crop-refine + zoomed-crop-verify AI pipeline as the iOS action
 * ({@code com.testsigma.addons.ios.ClickOnImageUsingAi}) for pixel-accurate clicks.
 *
 * The previous single-pass implementation had two problems:
 *
 *  1) LOCATING accuracy — it never downscaled the screenshot before sending it to the AI and
 *     trusted whatever {@code imageWidth}/{@code imageHeight} the model echoed back. Claude's
 *     vision input silently downscales large images internally
 *     ({@link AiActionUtils#MAX_IMAGE_EDGE} long edge / {@link AiActionUtils#MAX_IMAGE_AREA}
 *     area) — e.g. a 1920x1080 capture is reasoned about at 1456x816 — so the mapping back to
 *     capture space depended on the model guessing its own internal resolution correctly. This is
 *     fixed by resizing to a known-safe size ourselves (via
 *     {@link AiActionUtils#fitWithinAiLimits}) and telling the AI those exact dimensions up front,
 *     then re-projecting with the known ratio. A crop-and-zoom refine/verify pass then magnifies
 *     the target so the same percentage estimate error becomes a much smaller physical-pixel error.
 *
 *  2) CLICKING — it used {@code new Actions(driver).moveToLocation(...).click()}, which builds a
 *     W3C MOUSE pointer sequence. The Appium Windows driver (WinAppDriver) rejects mouse pointer
 *     input ("Currently only pen and touch pointer input source types are supported"). This is
 *     fixed by clicking with {@link java.awt.Robot} — a native OS-level left-button click at
 *     absolute desktop coordinates — falling back to a W3C TOUCH pointer sequence if Robot is
 *     unavailable.
 *
 * Pipeline:
 *  0) CAPTURE — full-screen screenshot via {@link java.awt.Robot} (NOT the driver), so it is in
 *               the same coordinate space Robot clicks in and the AI coords map 1:1 to the click.
 *  1) LOCATE  — resize to Claude's real vision limits, coarse-locate on that, scale back to full
 *               capture space (retried once if not found).
 *  2) CROP+ZOOM — crop around the coarse center and upscale it (also capped to vision limits).
 *  3) REFINE  — precise click point re-located from scratch on the zoomed crop.
 *  4) VERIFY  — dot-overlay double-check on the same zoomed crop, with correction if needed.
 *  5) CLICK   — map back to full-capture (screen) pixels and click natively.
 */
@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and click a UI element on a Windows app using a coarse-locate + " +
                "zoomed-crop-refine + zoomed-crop-verify AI pipeline for pixel-accurate clicks. " +
                "Retries the initial locate once before failing if the element isn't found.",
        displayName = "Ai: Click on Image/text matching prompt",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class ClickOnImageUsingAi extends WindowsAction {

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
            "You are a UI element locator working on a ZOOMED-IN CROP of a larger Windows desktop " +
                    "screenshot. This crop was extracted around the approximate location of a target " +
                    "element found by an earlier, coarser pass — the element you are looking for should " +
                    "be visible somewhere in this image, but may not be perfectly centered.\n\n" +
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
                    "clickable area.\n" +
                    "  If the element is NOT visible anywhere in this crop, set \"found\" to false — do " +
                    "not guess.\n\n" +
                    "STEP 3 — Return the precise click point:\n" +
                    "  Report the single best (click_x, click_y) point to click — the visual center of " +
                    "the element, accounting for padding/text centering, never on a border, gap, or a " +
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
                    "larger Windows desktop screenshot:\n" +
                    "  1. CLEAN CROP (first attachment) — the crop with no overlays.\n" +
                    "  2. ANNOTATED CROP (second attachment) — the same crop with a GREEN DOT added by " +
                    "an automated tool (NOT part of the original UI) marking a proposed click point.\n\n" +
                    "Use the clean crop to understand the UI. Use the annotated crop to evaluate the " +
                    "green dot.\n\n" +
                    "The element we are trying to click is described as: \"";

    private static final String VERIFY_PROMPT_PART2 =
            "\"\n\n" +
                    "STEP 1 — Use the given image dimensions:\n" +
                    "  Both images share the exact pixel dimensions stated at the top of this prompt " +
                    "(see IMAGE DIMENSIONS). Do NOT guess or re-measure them. Return coordinates in that " +
                    "pixel space and echo the given \"imageWidth\"/\"imageHeight\".\n\n" +
                    "STEP 2 — Evaluate the green dot:\n" +
                    "  • Is the green dot on the CORRECT target element described above?\n" +
                    "  • Is it at a good clickable position — center of the element, not on a border, " +
                    "gap, or a neighboring element?\n" +
                    "  Set \"accurate\": true only if BOTH conditions are met.\n\n" +
                    "STEP 3 — Return the best click location:\n" +
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
        logger.info("=== ClickOnImageUsingAi (Windows): Starting ===");
        File fullShotFile      = null;
        File zoomCleanFile     = null;
        File zoomAnnotatedFile = null;
        File finalResultFile   = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

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
                fullCapture = captureScreenshotWithRobot();
                captureW = fullCapture.getWidth();
                captureH = fullCapture.getHeight();
                logger.info("Robot screenshot size: " + captureW + "x" + captureH);

                // Resize BEFORE sending so we control the exact pixel space the AI reasons in —
                // raw desktop captures (e.g. 1920x1080, 4K) exceed Claude's real vision-input
                // limits, and relying on its internal auto-downscale (while trusting the dims it
                // echoes back) causes systematic coordinate drift.
                int[] sent = AiActionUtils.fitWithinAiLimits(captureW, captureH);
                sentW = sent[0];
                sentH = sent[1];
                BufferedImage aiImage = (sentW == captureW && sentH == captureH)
                        ? fullCapture : AiActionUtils.resizeImage(fullCapture, sentW, sentH);
                logger.info(String.format("[Pass 1] capture %dx%d -> sent %dx%d",
                        captureW, captureH, sentW, sentH));

                AiActionUtils.deleteQuietly(fullShotFile);
                fullShotFile = AiActionUtils.captureAsJpeg(aiImage, "ai_win_capture", JPEG_QUALITY, logger);

                logger.info("[Pass 1 attempt " + attempts + "] Coarse locate on resized screenshot...");
                String locateResponse = AiActionUtils.invokeAiAnthropicFirst(
                        ai, fullShotFile, AiActionUtils.LOCATE_PROMPT_DESKTOP, query, sentW, sentH, logger);
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

            zoomCleanFile = AiActionUtils.captureAsJpeg(zoomed, "ai_win_zoom_clean", JPEG_QUALITY, logger);

            // ── PASS 2 — precise refine on the zoomed crop ──────────────────────
            logger.info("[Pass 2] Refining click point on zoomed crop...");
            String refinePrompt = REFINE_PROMPT_PART1 + query + REFINE_PROMPT_PART2;
            String refineResponse = AiActionUtils.invokeAiAnthropicFirst(
                    ai, zoomCleanFile, refinePrompt, "", zoomW, zoomH, logger);
            JsonNode refineNode = AiActionUtils.parseAiJson(refineResponse, logger);

            int zoomClickX = clamp((int) Math.round((coarseCx - cropX1) * zoomScaleX), 0, zoomW - 1);
            int zoomClickY = clamp((int) Math.round((coarseCy - cropY1) * zoomScaleY), 0, zoomH - 1);
            int conf2 = conf1;
            String desc2 = desc1;

            if (refineNode != null && refineNode.path("found").asBoolean(true)) {
                int rx = refineNode.path("click_x").asInt(0);
                int ry = refineNode.path("click_y").asInt(0);
                if (rx > 0 && ry > 0) {
                    zoomClickX = clamp(rx, 0, zoomW - 1);
                    zoomClickY = clamp(ry, 0, zoomH - 1);
                    conf2 = refineNode.path("confidence").asInt(conf1);
                    desc2 = refineNode.path("reason").asText(desc1);
                }
            } else {
                logger.info("[Pass 2] Refine pass did not confirm the element in the crop — " +
                        "keeping the coarse (re-projected) center as the working point.");
            }

            logger.info(String.format("[Pass 2] zoom click: (%d,%d) | confidence: %d | desc: '%s'",
                    zoomClickX, zoomClickY, conf2, desc2));

            // ── PASS 3 — verify the proposed point with a dot overlay, on the zoom ─
            BufferedImage zoomAnnotated = AiActionUtils.drawHighlight(
                    zoomed, 0, 0, 0, 0, zoomClickX, zoomClickY, Color.GREEN);
            zoomAnnotatedFile = AiActionUtils.captureAsJpeg(zoomAnnotated, "ai_win_zoom_annotated", JPEG_QUALITY, logger);

            logger.info("[Pass 3] Verifying click point via dot overlay on zoomed crop...");
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
                    int prevX = zoomClickX;
                    int prevY = zoomClickY;
                    zoomClickX = clamp(vx, 0, zoomW - 1);
                    zoomClickY = clamp(vy, 0, zoomH - 1);
                    logger.info(String.format(
                            "[Pass 3] corrected: (%d,%d) -> (%d,%d) | reason: '%s'",
                            prevX, prevY, zoomClickX, zoomClickY, reason));
                } else {
                    logger.info("[Pass 3] accurate=" + accurate + " | reason: '" + reason + "'");
                }
                finalDesc = reason;
            } else if (verifyNode != null) {
                logger.info("[Pass 3] Verification reports the element is out of this crop — " +
                        "keeping Pass 2's click point.");
            }

            // ── Map final zoom-space point back to full-capture (screen) pixels ──
            int clickX = clamp(cropX1 + (int) Math.round(zoomClickX / zoomScaleX), 0, captureW - 1);
            int clickY = clamp(cropY1 + (int) Math.round(zoomClickY / zoomScaleY), 0, captureH - 1);

            logger.info(String.format("[Final] screen click point: (%d,%d)", clickX, clickY));

            BufferedImage finalAnnotated = AiActionUtils.drawFinalAnnotation(
                    fullCapture, lx1, ly1, lx2, ly2, clickX, clickY);
            finalResultFile = ScreenshotUtils.saveScreenshotToFile(finalAnnotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalResultFile, logger);

            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }

            logger.info(String.format("Clicking at (%d,%d) confidence=%d", clickX, clickY, conf2));
            performClick(clickX, clickY);

            setSuccessMessage(String.format(
                    "Successfully clicked '%s' at (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, clickX, clickY, lx1, ly1, lx2, ly2, conf2, finalDesc));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to click using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(fullShotFile);
            AiActionUtils.deleteQuietly(zoomCleanFile);
            AiActionUtils.deleteQuietly(zoomAnnotatedFile);
            AiActionUtils.deleteQuietly(finalResultFile);
        }
    }

    /**
     * Captures a full-screen screenshot using {@link java.awt.Robot} instead of the Appium
     * (WinAppDriver) driver. This is essential for click accuracy: {@link #performClick} clicks
     * with Robot at ABSOLUTE DESKTOP coordinates, so the image the AI reasons about must be in
     * that SAME coordinate space. A driver screenshot can be captured at a different
     * resolution/origin than the physical desktop Robot acts on, which makes the AI-returned
     * coordinates land in the wrong place. Capturing AND clicking both via Robot keeps them in one
     * coordinate system, so the click point maps 1:1 to the AI-given coordinates.
     *
     * On HiDPI displays {@code createScreenCapture} may return an image in physical pixels while
     * {@code mouseMove} uses logical coordinates; we resize the capture back to the logical screen
     * size so image pixel (x,y) maps 1:1 to the click coordinate (x,y).
     */
    private BufferedImage captureScreenshotWithRobot() throws AWTException {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Robot robot = new Robot();
        BufferedImage shot = robot.createScreenCapture(new Rectangle(screen));
        if (shot.getWidth() != screen.width || shot.getHeight() != screen.height) {
            logger.info(String.format(
                    "Robot capture %dx%d differs from logical screen %dx%d (HiDPI) — resizing to " +
                            "logical size so click coords match the AI coords.",
                    shot.getWidth(), shot.getHeight(), screen.width, screen.height));
            shot = AiActionUtils.resizeImage(shot, screen.width, screen.height);
        }
        return shot;
    }

    /**
     * Clicks at the given screen coordinates using {@link java.awt.Robot} — a native OS-level
     * mouse click. This deliberately bypasses the W3C Actions API, whose MOUSE pointer is NOT
     * supported by WinAppDriver ("Currently only pen and touch pointer input source types are
     * supported"). Robot injects a real left-button click at absolute desktop coordinates on the
     * machine the addon runs on (the same Windows host as the app under test).
     *
     * If Robot is unavailable (e.g. headless), it falls back to a W3C TOUCH pointer sequence,
     * which WinAppDriver does accept.
     *
     * Note on DPI: the coordinates are in the screenshot's physical-pixel space. Robot uses the
     * desktop coordinate system, which matches when the display scale is 100%. If run on a display
     * with scaling != 100% and a consistent offset appears, a DPI-scale correction is needed here.
     */
    private void performClick(int x, int y) {
        try {
            Robot robot = new Robot();
            robot.setAutoDelay(40);
            robot.mouseMove(x, y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            logger.info("Click executed via java.awt.Robot at (" + x + "," + y + ").");
        } catch (Exception robotEx) {
            logger.info("Robot click failed (" + robotEx.getMessage()
                    + "); falling back to W3C touch pointer action.");
            PointerInput touch = new PointerInput(PointerInput.Kind.TOUCH, "touch");
            Sequence click = new Sequence(touch, 0);
            click.addAction(touch.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
            click.addAction(touch.createPointerDown(0));
            click.addAction(new Pause(touch, Duration.ofMillis(80)));
            click.addAction(touch.createPointerUp(0));
            ((Interactive) driver).perform(Collections.singletonList(click));
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
