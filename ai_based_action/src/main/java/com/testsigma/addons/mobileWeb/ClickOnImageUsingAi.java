package com.testsigma.addons.mobileWeb;

import com.fasterxml.jackson.databind.JsonNode;
import com.testsigma.addons.util.AiActionUtils;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.Collections;

@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and tap a UI element on a mobile web page using AI. " +
                "The AI identifies the element bounding box; the tap always lands at its geometric center. " +
                "Fails if the element is not found.",
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

    @Override
    public Result execute() {
        logger.info("=== ClickOnImageUsingAi (Mobile Web): Starting ===");
        File annotatedFile = null;

        try {
            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            // Capture the device screenshot
            BufferedImage pageCapture = readScreenshot();
            int captureW = pageCapture.getWidth();
            int captureH = pageCapture.getHeight();
            logger.info("Device screenshot size: " + captureW + "x" + captureH);

            // Locate the element via AI
            LocateResult result = locate(pageCapture, query);
            if (result == null) {
                return fail(pageCapture, "Failed to get image response from AI (contact support)");
            }
            if (!result.found) {
                return fail(pageCapture, "AI could not locate '" + query + "': " + result.description);
            }

            int[] box = result.box;
            logger.info(String.format("AI element box (capture px): (%d,%d)-(%d,%d) conf=%d",
                    box[0], box[1], box[2], box[3], result.confidence));

            // Refine the box onto real content pixels before computing the tap point
            box = snapToContent(pageCapture, box, logger);

            int tapX = (box[0] + box[2]) / 2;
            int tapY = (box[1] + box[3]) / 2;
            logger.info(String.format("Tap centre (capture px): (%d,%d)", tapX, tapY));

            // Upload the annotated screenshot before tapping, for diagnostics either way
            BufferedImage annotated = AiActionUtils.drawFinalAnnotation(
                    pageCapture, box[0], box[1], box[2], box[3], tapX, tapY);
            annotatedFile = ScreenshotUtils.saveScreenshotToFile(annotated, "ai_click_elem_result");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, annotatedFile, logger);

            // Convert physical screenshot pixels to logical tap coordinates
            int[] logical = toLogical(captureW, tapX, tapY);

            logger.info(String.format("Tapping at (%d,%d)  confidence=%d",
                    logical[0], logical[1], result.confidence));
            tap(logical[0], logical[1]);

            setSuccessMessage(String.format(
                    "Successfully tapped '%s' at (%d,%d) | bbox (%d,%d)-(%d,%d) | confidence=%d | %s",
                    query, logical[0], logical[1], box[0], box[1], box[2], box[3],
                    result.confidence, result.description));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            setErrorMessage("Failed to tap using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            AiActionUtils.deleteQuietly(annotatedFile);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Reads the current screenshot, tolerating iOS Appium returning a base64 payload. */
    private BufferedImage readScreenshot() throws Exception {
        return AiActionUtils.captureScreenshotWithFallback((TakesScreenshot) driver, logger);
    }

    /** Locates {@code query} in {@code source}; returns null on no response, found=false if absent. */
    private LocateResult locate(BufferedImage source, String query) throws Exception {
        int srcW = source.getWidth();
        int srcH = source.getHeight();

        // Resize before sending so we control the pixel space the AI reasons in
        int[] sent = AiActionUtils.fitWithinAiLimits(srcW, srcH);
        int sentW = sent[0];
        int sentH = sent[1];
        BufferedImage aiImage = AiActionUtils.resizeImage(source, sentW, sentH);
        logger.info(String.format("Locate: source %dx%d → sent %dx%d", srcW, srcH, sentW, sentH));

        File file = AiActionUtils.captureAsJpeg(aiImage, "ai_mobileweb_capture", logger);
        try {
            String response = AiActionUtils.invokeAi(
                    ai, file, AiActionUtils.LOCATE_PROMPT_MOBILE_WEB, query, logger);
            JsonNode node = AiActionUtils.parseAiJson(response, logger);
            if (node == null) {
                return null;
            }
            if (!node.path("found").asBoolean(false)) {
                return new LocateResult(false, null, 0, node.path("description").asText("element not found"));
            }

            int x1 = node.path("x1").asInt(0);
            int y1 = node.path("y1").asInt(0);
            int x2 = node.path("x2").asInt(0);
            int y2 = node.path("y2").asInt(0);
            int iw = node.path("imageWidth").asInt(0);
            int ih = node.path("imageHeight").asInt(0);
            int confidence = node.path("confidence").asInt(50);
            String desc = node.path("description").asText("");

            int refW = sentW;
            int refH = sentH;
            // Only trust the AI's reported dims if they're in a plausible range vs what we sent
            boolean plausible = iw > 0 && ih > 0
                    && iw >= sentW / 3 && iw <= sentW * 3
                    && ih >= sentH / 3 && ih <= sentH * 3;
            if (plausible) {
                refW = (iw + sentW) / 2;
                refH = (ih + sentH) / 2;
            }
            logger.info(String.format(
                    "Locate: AI bbox (%d,%d)-(%d,%d) reported %dx%d → scaling denom %dx%d conf=%d desc='%s'",
                    x1, y1, x2, y2, iw, ih, refW, refH, confidence, desc));

            int[] box = AiActionUtils.scaleAiToCapture(x1, y1, x2, y2, refW, refH, srcW, srcH);
            return new LocateResult(true, box, confidence, desc);
        } finally {
            AiActionUtils.deleteQuietly(file);
        }
    }

    /** Snaps an approximate AI box onto the real content pixels around it; safe no-op on failure. */
    private static int[] snapToContent(BufferedImage img, int[] box, com.testsigma.sdk.Logger logger) {
        int imgW = img.getWidth();
        int imgH = img.getHeight();
        int x1 = Math.max(0, Math.min(box[0], imgW));
        int y1 = Math.max(0, Math.min(box[1], imgH));
        int x2 = Math.max(0, Math.min(box[2], imgW));
        int y2 = Math.max(0, Math.min(box[3], imgH));
        int w = Math.max(1, x2 - x1);
        int h = Math.max(1, y2 - y1);

        // Search window padded more upward than sideways (drift is vertical, not horizontal)
        int padX = Math.max(w / 10, 10);
        int padUp = Math.max(h * 2, 40);
        int padDown = Math.max(h, 20);
        int wx1 = Math.max(0, x1 - padX);
        int wy1 = Math.max(0, y1 - padUp);
        int wx2 = Math.min(imgW, x2 + padX);
        int wy2 = Math.min(imgH, y2 + padDown);
        int ww = wx2 - wx1;
        int wh = wy2 - wy1;
        if (ww < 4 || wh < 4) {
            return box;
        }

        // Background colour = average of the window's top/bottom border rows
        long sr = 0, sg = 0, sb = 0;
        int n = 0;
        for (int x = wx1; x < wx2; x++) {
            int top = img.getRGB(x, wy1);
            int bot = img.getRGB(x, wy2 - 1);
            sr += ((top >> 16) & 0xFF) + ((bot >> 16) & 0xFF);
            sg += ((top >> 8) & 0xFF) + ((bot >> 8) & 0xFF);
            sb += (top & 0xFF) + (bot & 0xFF);
            n += 2;
        }
        int bgR = (int) (sr / n);
        int bgG = (int) (sg / n);
        int bgB = (int) (sb / n);

        final int COLOR_DIST = 60;                     // channel-sum distance from background
        int minRowPixels = Math.max(2, ww / 100);      // ignore isolated speckle rows

        int top = -1, bot = -1, left = -1, right = -1;
        for (int yy = 0; yy < wh; yy++) {
            int rowCount = 0;
            for (int xx = 0; xx < ww; xx++) {
                int rgb = img.getRGB(wx1 + xx, wy1 + yy);
                int dist = Math.abs(((rgb >> 16) & 0xFF) - bgR)
                        + Math.abs(((rgb >> 8) & 0xFF) - bgG)
                        + Math.abs((rgb & 0xFF) - bgB);
                if (dist > COLOR_DIST) {
                    rowCount++;
                    if (left < 0 || xx < left) left = xx;
                    if (xx > right) right = xx;
                }
            }
            if (rowCount >= minRowPixels) {
                if (top < 0) top = yy;
                bot = yy;
            }
        }

        if (top < 0 || left < 0) {
            return box;                                 // nothing distinct found
        }
        if ((bot - top) >= wh * 0.95 || (right - left) >= ww * 0.95) {
            return box;                                 // content fills window → unreliable
        }

        int[] refined = {wx1 + left, wy1 + top, wx1 + right + 1, wy1 + bot + 1};
        if (refined[2] - refined[0] < 3 || refined[3] - refined[1] < 3) {
            return box;                                 // too small to trust
        }
        if (logger != null) {
            logger.info(String.format("snapToContent: (%d,%d)-(%d,%d) → (%d,%d)-(%d,%d)",
                    box[0], box[1], box[2], box[3], refined[0], refined[1], refined[2], refined[3]));
        }
        return refined;
    }

    /** Converts a physical screenshot point to logical tap coordinates (iOS/Android only). */
    private int[] toLogical(int captureW, int tapX, int tapY) {
        Object platformCap = ((HasCapabilities) driver).getCapabilities().getCapability("platformName");
        String platform = platformCap != null ? platformCap.toString() : "";
        boolean isMobile = "iOS".equalsIgnoreCase(platform) || "ANDROID".equalsIgnoreCase(platform);
        if (!isMobile) {
            return new int[]{tapX, tapY};
        }
        Dimension windowSize = driver.manage().window().getSize();
        double pixelRatio = windowSize.width > 0 ? (double) captureW / windowSize.width : 1.0;
        int lx = (int) Math.round(tapX / pixelRatio);
        int ly = (int) Math.round(tapY / pixelRatio);
        logger.info(String.format(
                "%s: physical (%d,%d) → logical (%d,%d)  window=%dx%d  pixelRatio=%.4f",
                platform, tapX, tapY, lx, ly, windowSize.width, windowSize.height, pixelRatio));
        return new int[]{lx, ly};
    }

    /** Performs a single tap at the given logical coordinates using W3C pointer actions. */
    private void tap(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence seq = new Sequence(finger, 0);
        seq.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        seq.addAction(finger.createPointerDown(0));
        seq.addAction(new Pause(finger, Duration.ofMillis(100))); // brief hold so it registers as a tap
        seq.addAction(finger.createPointerUp(0));
        ((Interactive) driver).perform(Collections.singletonList(seq));
    }

    /** Uploads the plain screenshot for context and returns a FAILED result with the given message. */
    private Result fail(BufferedImage capture, String message) {
        try {
            File failFile = ScreenshotUtils.saveScreenshotToFile(capture, "ai_click_failed");
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, failFile, logger);
            AiActionUtils.deleteQuietly(failFile);
        } catch (Exception ignore) {
            // best-effort diagnostic upload only
        }
        setErrorMessage(message);
        return Result.FAILED;
    }

    /** Result of a locate call; {@code box} is [x1,y1,x2,y2] in the located image's pixels. */
    private static final class LocateResult {
        final boolean found;
        final int[] box;
        final int confidence;
        final String description;

        LocateResult(boolean found, int[] box, int confidence, String description) {
            this.found = found;
            this.box = box;
            this.confidence = confidence;
            this.description = description;
        }
    }
}