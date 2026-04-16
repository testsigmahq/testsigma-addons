package com.testsigma.addons.windows;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.addons.util.ScreenshotUtils;
import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;

@Data
@Action(actionText = "Ai: Click on Image/text matching prompt prompt-describing-image",
        description = "Locate and click a UI element using a single AI call. " +
                "AI reports the image dimensions it analyzed; coordinates are scaled back to screen space automatically. " +
                "Fails if the element is not found.",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = true)
public class ClickOnImageUsingAi extends WindowsAction {

    @TestData(reference = "prompt-describing-image")
    private com.testsigma.sdk.TestData queryDescribingElement;

    @AI
    private com.testsigma.sdk.AI ai;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private static final int HIGHLIGHT_STROKE_WIDTH = 2;
    private static final int DOT_RADIUS = 2;

    // The AI must report the pixel dimensions of the image it received so we can
    // compute the exact scale factor back to original screen coordinates.
    private static final String PROMPT =
            "You are a UI element locator. Given a screenshot, find the EXACT pixel " +
                    "bounding box of the requested element.\n\n" +
                    "STEP 1 — Measure the image:\n" +
                    "  Look at the raw pixel dimensions of the image you received (width × height).\n" +
                    "  You MUST include these as \"imageWidth\" and \"imageHeight\" in your JSON response.\n\n" +
                    "STEP 2 — Find the element:\n" +
                    "  Locate the element visually using pixel-level analysis. If there is an application opened " +
                    "consider only the opened application for locating the element \n" +
                    "  For image/graphic elements (text, icons, logos) find the actual picture.\n" +
                    "  If there is an application running in windowed mode than consider only that application for " +
                    "   identification task and ignore task bar and other elements present on wallpaper" +
                    "  For text elements, look for the distinctive font color and style.\n" +
                    "  Report the bounding box as pixel coordinates: top-left corner (x1, y1) and bottom-right corner (x2, y2).\n\n" +
                    "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
                    "If found:\n" +
                    "  {\"found\": true, \"x1\": <int>, \"y1\": <int>, \"x2\": <int>, \"y2\": <int>, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": <0-100>, \"description\": \"<what you found>\"}\n" +
                    "If not found:\n" +
                    "  {\"found\": false, \"x1\": 0, \"y1\": 0, \"x2\": 0, \"y2\": 0, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": 0, \"description\": \"<why not found>\"}\n\n" +
                    "TASK: Find the exact bounding box of: ";

    @Override
    public Result execute() {

        logger.info("=== ClickElementUsingAi: Starting ===");
        File screenshotFile     = null;
        File finalAnnotatedFile = null;

        try {

            String query = queryDescribingElement.getValue().toString();
            logger.info("Query: " + query);

            // ── Step 1: Determine logical screen dimensions ──
            // Toolkit returns the OS-level logical (DIP) screen size, e.g. 1920×1080 even on
            // a 2× HiDPI display.  This is what Selenium / WinAppDriver uses for coordinates.
            Dimension logicalScreen = Toolkit.getDefaultToolkit().getScreenSize();
            int logicalScreenW = logicalScreen.width;
            int logicalScreenH = logicalScreen.height;
            logger.info("Logical screen size (Toolkit): " + logicalScreenW + "x" + logicalScreenH);

            // ── Step 2: Capture the full desktop via Robot ──
            // Robot.createScreenCapture returns physical pixels on HiDPI displays
            // (e.g. 3840×2160 on a 2× Retina Mac, 3840×2160 on a 200% Windows display).
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(logicalScreen);
            BufferedImage desktopCapture = robot.createScreenCapture(screenRect);
            int captureW = desktopCapture.getWidth();
            int captureH = desktopCapture.getHeight();
            logger.info("Robot desktop capture size (physical px): " + captureW + "x" + captureH);

            // Display scale = physical / logical.  On a non-HiDPI display this is 1.0×1.0.
            double displayScaleX = (double) captureW / logicalScreenW;
            double displayScaleY = (double) captureH / logicalScreenH;
            logger.info(String.format(
                    "Display scale (capture / logical): %.4fx%.4f", displayScaleX, displayScaleY));

            // ── Step 3: Write desktop capture to temp file for AI ──
            // Format is chosen by inspecting the captured image's color model:
            //   • alpha channel present → PNG  (JPEG codec does not support alpha)
            //   • no alpha              → JPEG (smaller file; Vertex AI accepts both)
            boolean hasAlpha   = desktopCapture.getColorModel().hasAlpha();
            String imageFormat = hasAlpha ? "PNG" : "JPEG";
            String fileExt     = hasAlpha ? ".png" : ".jpg";
            logger.info(String.format(
                    "Capture color model: type=%d  hasAlpha=%b  → writing as %s",
                    desktopCapture.getType(), hasAlpha, imageFormat));

            screenshotFile = File.createTempFile("ai_desktop_capture", fileExt);
            if (hasAlpha) {
                // PNG supports alpha — write the capture directly.
                ImageIO.write(desktopCapture, "PNG", screenshotFile);
            } else {
                // JPEG requires TYPE_INT_RGB (no alpha).  Convert if necessary.
                BufferedImage rgbCapture = desktopCapture.getType() == BufferedImage.TYPE_INT_RGB
                        ? desktopCapture
                        : toRgb(desktopCapture);
                ImageIO.write(rgbCapture, "JPEG", screenshotFile);
            }
            logger.info(String.format(
                    "%s written: %s  (size=%d bytes, dims=%dx%d)",
                    imageFormat, screenshotFile.getAbsolutePath(),
                    screenshotFile.length(), captureW, captureH));

            // ── Step 4: Build and send the AI request ──
            String fullPrompt = PROMPT + query +
                    "<custom_instructions>\n" +
                    "{\n" +
                    "  \"provider\": \"vertex-ai\",\n" +
                    "  \"image_detail\": \"high\"\n" +
                    "}\n" +
                    "</custom_instructions>";

            AIRequest aiRequest = new AIRequest();
            aiRequest.setPrompt(fullPrompt);
            aiRequest.setModel("anthropic.claude-opus-4-6");
            aiRequest.setFiles(List.of(screenshotFile));

            logger.info("Sending AI request...");
            String aiResponse = ai.invokeAI(aiRequest);
            logger.info("AI response: " + aiResponse);

            // ── Step 5: Parse the AI response ──
            JsonNode responseNode = parseJson(aiResponse);
            if (responseNode == null) {
                setErrorMessage("Failed to get the image response from ai (contact to support)" );
                logger.info("Failed to parse AI response as JSON. Raw response: " + aiResponse);
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile , logger);
                return Result.FAILED;
            }

            boolean found = responseNode.path("found").asBoolean(false);
            if (!found) {
                String reason = responseNode.path("description").asText("element not found");
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile , logger);
                setErrorMessage("AI could not locate '" + query + "': " + reason);
                return Result.FAILED;
            }

            int aiX1        = responseNode.path("x1").asInt(0);
            int aiY1        = responseNode.path("y1").asInt(0);
            int aiX2        = responseNode.path("x2").asInt(0);
            int aiY2        = responseNode.path("y2").asInt(0);
            int imageWidth  = responseNode.path("imageWidth").asInt(0);
            int imageHeight = responseNode.path("imageHeight").asInt(0);
            int confidence  = responseNode.path("confidence").asInt(50);
            String description = responseNode.path("description").asText("");

            logger.info(String.format(
                    "AI result — bbox: (%d,%d)-(%d,%d) | AI image dims: %dx%d | confidence: %d | desc: '%s'",
                    aiX1, aiY1, aiX2, aiY2, imageWidth, imageHeight, confidence, description));

            if (imageWidth <= 0 || imageHeight <= 0) {
                setErrorMessage(String.format(
                        "AI returned invalid image dimensions (imageWidth=%d, imageHeight=%d). " +
                                "Cannot compute scale factor.", imageWidth, imageHeight));
                // upload the orignal desktop capture for debugging
                finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(desktopCapture, "ai_click_failed");
                ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile , logger);
                return Result.FAILED;
            }

            // ── Step 6: Two-stage coordinate scaling ──
            //
            // Stage A — AI image → capture pixels
            //   The AI may receive a down-sampled version of the JPEG we sent.
            //   imageWidth/imageHeight is what the AI actually analyzed; scale back
            //   to the physical pixel space of the Robot capture.
            double aiToCapX = (double) captureW / imageWidth;
            double aiToCapY = (double) captureH / imageHeight;
            logger.info(String.format(
                    "Stage-A scale (AI→capture): %.4fx%.4f  (capture %dx%d / ai-image %dx%d)",
                    aiToCapX, aiToCapY, captureW, captureH, imageWidth, imageHeight));

            int capX1 = (int) Math.round(aiX1 * aiToCapX);
            int capY1 = (int) Math.round(aiY1 * aiToCapY);
            int capX2 = (int) Math.round(aiX2 * aiToCapX);
            int capY2 = (int) Math.round(aiY2 * aiToCapY);
            int capCX  = (capX1 + capX2) / 2;
            int capCY  = (capY1 + capY2) / 2;
            logger.info(String.format(
                    "Capture-pixel bbox: (%d,%d)-(%d,%d)  center: (%d,%d)",
                    capX1, capY1, capX2, capY2, capCX, capCY));

            // Stage B — capture pixels → logical screen coordinates
            //   Selenium Actions / WinAppDriver expects logical (DIP) coordinates,
            //   so divide out the HiDPI display scale.
            int logicalX1 = (int) Math.round(capX1 / displayScaleX);
            int logicalY1 = (int) Math.round(capY1 / displayScaleY);
            int logicalX2 = (int) Math.round(capX2 / displayScaleX);
            int logicalY2 = (int) Math.round(capY2 / displayScaleY);
            int logicalCX  = (logicalX1 + logicalX2) / 2;
            int logicalCY  = (logicalY1 + logicalY2) / 2;
            logger.info(String.format(
                    "Stage-B scale (capture→logical): 1/%.4f × 1/%.4f",
                    displayScaleX, displayScaleY));
            logger.info(String.format(
                    "Logical screen bbox: (%d,%d)-(%d,%d)  center: (%d,%d)",
                    logicalX1, logicalY1, logicalX2, logicalY2, logicalCX, logicalCY));

            // ── Step 7: Annotate the desktop capture (in capture-pixel space) ──
            BufferedImage finalAnnotated = drawHighlightRect(
                    desktopCapture, capX1, capY1, capX2, capY2, capCX, capCY);
            finalAnnotatedFile = ScreenshotUtils.saveScreenshotToFile(finalAnnotated, "ai_click_elem_result");
            logger.info("Uploading annotated screenshot: " + finalAnnotatedFile.getAbsolutePath());
            ScreenshotUtils.uploadScreenshotToS3(testStepResult, finalAnnotatedFile, logger);

            // ── Step 8: Click via Robot (logical coordinates) ──
            logger.info(String.format(
                    "Clicking via Robot — logical=(%d,%d)  confidence=%d",
                    logicalCX, logicalCY, confidence));
            robot.mouseMove(logicalCX, logicalCY);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            logger.info("Click performed successfully");
            setSuccessMessage(String.format(
                    "Successfully clicked '%s' at logical (%d,%d) | capture bbox (%d,%d)-(%d,%d) | confidence=%d | format=%s | %s",
                    query, logicalCX, logicalCY,
                    capX1, capY1, capX2, capY2, confidence, imageFormat, description));
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to click using AI. Error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            deleteQuietly(screenshotFile);
            deleteQuietly(finalAnnotatedFile);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Convert any BufferedImage to TYPE_INT_RGB (strips alpha for JPEG output).
    // ─────────────────────────────────────────────────────────────────────────
    private BufferedImage toRgb(BufferedImage src) {
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Draw a green bounding rectangle + green dot at the click center (cx, cy).
    // ─────────────────────────────────────────────────────────────────────────
    private BufferedImage drawHighlightRect(BufferedImage original,
                                            int x1, int y1, int x2, int y2,
                                            int cx, int cy) {
        BufferedImage copy = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);

        // Clamp rectangle to image bounds
        int rx1 = Math.max(0, x1);
        int ry1 = Math.max(0, y1);
        int rx2 = Math.min(original.getWidth()  - 1, x2);
        int ry2 = Math.min(original.getHeight() - 1, y2);

        // Green bounding rectangle
        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(HIGHLIGHT_STROKE_WIDTH));
        g.drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1);

        // Green filled dot at click center
        g.setColor(Color.GREEN);
        g.fillOval(cx - DOT_RADIUS, cy - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);

        g.dispose();
        return copy;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Extract JSON object from AI response, tolerating leading/trailing text.
    // ─────────────────────────────────────────────────────────────────────────
    private JsonNode parseJson(String aiResponse) {
        try {
            String json = aiResponse.trim();
            if (json.contains("```")) {
                json = json.replaceAll("(?s)```[a-z]*\\s*", "").
                        replaceAll("```", "").trim();
            }
            int start = json.indexOf('{');
            int end   = json.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                json = json.substring(start, end + 1);
            }
            return new ObjectMapper().readTree(json);
        } catch (Exception e) {
            logger.info("Failed to parse AI JSON: " + e.getMessage() + " | raw: " + aiResponse);
            return null;
        }
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
