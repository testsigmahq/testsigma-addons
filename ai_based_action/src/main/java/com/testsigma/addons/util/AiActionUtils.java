package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.AI;
import com.testsigma.sdk.AIRequest;
import com.testsigma.sdk.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class AiActionUtils {

    public static final String AI_MODEL = "anthropic.claude-opus-4-6";

    private static final String CUSTOM_INSTRUCTIONS =
            "<custom_instructions>\n" +
            "{\n" +
            "  \"provider\": \"vertex-ai\",\n" +
            "  \"image_detail\": \"high\"\n" +
            "}\n" +
            "</custom_instructions>";

    private static final String LOCATE_STEP1 =
            "STEP 1 — Measure the image:\n" +
            "  Look at the raw pixel dimensions of the image you received (width × height).\n" +
            "  You MUST include these as \"imageWidth\" and \"imageHeight\" in your JSON response.\n\n";

    private static final String LOCATE_STEP2_SUFFIX =
            "  For image/graphic elements (buttons, icons, logos, images) find the actual element.\n" +
            "  For text elements, look for the distinctive font color and style.\n" +
            "  Report the bounding box as pixel coordinates: top-left corner (x1, y1) and bottom-right corner (x2, y2).\n\n";

    private static final String LOCATE_OUTPUT_FORMAT =
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

    private static final String VERIFY_STEP1 =
            "STEP 1 — Measure the image:\n" +
            "  Look at the raw pixel dimensions of the image you received (width × height).\n" +
            "  You MUST include these as \"imageWidth\" and \"imageHeight\" in your JSON response.\n\n";

    private static final String VERIFY_STEP2 =
            "STEP 2 — Verify the condition:\n" +
            "  Carefully examine the screenshot for the described content.\n" +
            "  Consider text, images, UI elements, colors, layout, and any visible state.\n" +
            "  If the described content is found, set \"verified\" to true and provide the bounding box " +
            "of the relevant area so it can be highlighted.\n" +
            "  If the described content is NOT found or the condition is NOT met, set \"verified\" to false.\n\n";

    private static final String VERIFY_OUTPUT_FORMAT =
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
            "If verified (content found / condition met):\n" +
            "  {\"verified\": true, \"x1\": <int>, \"y1\": <int>, \"x2\": <int>, \"y2\": <int>, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<what was found and why it passes>\"}\n" +
            "If NOT verified (content missing / condition not met):\n" +
            "  {\"verified\": false, \"x1\": 0, \"y1\": 0, \"x2\": 0, \"y2\": 0, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<what was expected but not found>\"}\n\n" +
            "VERIFICATION QUERY: ";

    // ── Locate prompts (per platform) ──

    public static final String LOCATE_PROMPT_WEB =
            "You are a UI element locator. Given a web page screenshot, find the EXACT pixel " +
            "bounding box of the requested element.\n\n" +
            LOCATE_STEP1 +
            "STEP 2 — Find the element:\n" +
            "  Locate the element visually on the web page using pixel-level analysis.\n" +
            LOCATE_STEP2_SUFFIX +
            LOCATE_OUTPUT_FORMAT;

    public static final String LOCATE_PROMPT_ANDROID =
            "You are a UI element locator. Given an Android device screenshot, find the EXACT pixel " +
            "bounding box of the requested element.\n\n" +
            LOCATE_STEP1 +
            "STEP 2 — Find the element:\n" +
            "  Locate the element visually on the Android screen using pixel-level analysis.\n" +
            LOCATE_STEP2_SUFFIX +
            LOCATE_OUTPUT_FORMAT;

    public static final String LOCATE_PROMPT_IOS =
            "You are a UI element locator. Given an iOS device screenshot, find the EXACT pixel " +
            "bounding box of the requested element.\n\n" +
            LOCATE_STEP1 +
            "STEP 2 — Find the element:\n" +
            "  Locate the element visually on the iOS screen using pixel-level analysis.\n" +
            LOCATE_STEP2_SUFFIX +
            LOCATE_OUTPUT_FORMAT;

    public static final String LOCATE_PROMPT_MOBILE_WEB =
            "You are a UI element locator. Given a mobile web page screenshot, find the EXACT pixel " +
            "bounding box of the requested element.\n\n" +
            LOCATE_STEP1 +
            "STEP 2 — Find the element:\n" +
            "  Locate the element visually on the mobile web page using pixel-level analysis.\n" +
            LOCATE_STEP2_SUFFIX +
            LOCATE_OUTPUT_FORMAT;

    public static final String LOCATE_PROMPT_SALESFORCE =
            "You are a UI element locator. Given a Salesforce page screenshot, find the EXACT pixel " +
            "bounding box of the requested element.\n\n" +
            LOCATE_STEP1 +
            "STEP 2 — Find the element:\n" +
            "  Locate the element visually on the Salesforce page using pixel-level analysis.\n" +
            LOCATE_STEP2_SUFFIX +
            LOCATE_OUTPUT_FORMAT;

    public static final String LOCATE_PROMPT_DESKTOP =
            "You are a UI element locator. Given a screenshot, find the EXACT pixel " +
            "bounding box of the requested element.\n\n" +
            LOCATE_STEP1 +
            "STEP 2 — Find the element:\n" +
            "  Locate the element visually using pixel-level analysis. If there is an application opened " +
            "consider only the opened application for locating the element.\n" +
            "  For image/graphic elements (text, icons, logos) find the actual picture.\n" +
            "  If there is an application running in windowed mode consider only that application for " +
            "identification task and ignore taskbar and other elements present on the wallpaper.\n" +
            "  For text elements, look for the distinctive font color and style.\n" +
            "  Report the bounding box as pixel coordinates: top-left corner (x1, y1) and bottom-right corner (x2, y2).\n\n" +
            LOCATE_OUTPUT_FORMAT;

    // ── Verify prompts (per platform) ──

    public static final String VERIFY_PROMPT_WEB =
            "You are a UI verification assistant. Given a web page screenshot, determine whether " +
            "the described content or condition is present and visible on the page.\n\n" +
            VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

    public static final String VERIFY_PROMPT_ANDROID =
            "You are a UI verification assistant. Given an Android device screenshot, determine whether " +
            "the described content or condition is present and visible on the screen.\n\n" +
            VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

    public static final String VERIFY_PROMPT_IOS =
            "You are a UI verification assistant. Given an iOS device screenshot, determine whether " +
            "the described content or condition is present and visible on the screen.\n\n" +
            VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

    public static final String VERIFY_PROMPT_MOBILE_WEB =
            "You are a UI verification assistant. Given a mobile web page screenshot, determine whether " +
            "the described content or condition is present and visible on the page.\n\n" +
            VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

    public static final String VERIFY_PROMPT_SALESFORCE =
            "You are a UI verification assistant. Given a Salesforce page screenshot, determine whether " +
            "the described content or condition is present and visible on the page.\n\n" +
            VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

    public static final String VERIFY_PROMPT_DESKTOP =
            "You are a UI verification assistant. Given a desktop application screenshot, determine whether " +
            "the described content or condition is present and visible.\n\n" +
            VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

    // ── Core utilities ──

    /**
     * Converts the image to RGB, writes it as JPEG to a temp file, and returns that file.
     * Always JPEG — the AI service expects image/jpeg.
     */
    public static File captureAsJpeg(BufferedImage image, String prefix, Logger logger) throws Exception {
        File file = File.createTempFile(prefix, ".jpg");
        BufferedImage rgb = image.getType() == BufferedImage.TYPE_INT_RGB ? image : toRgb(image);
        ImageIO.write(rgb, "JPEG", file);
        logger.info(String.format("JPEG written: %s (size=%d bytes, dims=%dx%d)",
                file.getAbsolutePath(), file.length(), image.getWidth(), image.getHeight()));
        return file;
    }

    /** Builds the full AI prompt and invokes the AI service. */
    public static String invokeAi(AI ai, File screenshotFile,
                                   String basePrompt, String query,
                                   Logger logger) throws Exception {
        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(basePrompt + query + CUSTOM_INSTRUCTIONS);
        aiRequest.setModel(AI_MODEL);
        aiRequest.setFiles(List.of(screenshotFile));
        logger.info("Sending AI request...");
        String response = ai.invokeAI(aiRequest);
        logger.info("AI response: " + response);
        return response;
    }

    /** Strips markdown fences and extracts the first JSON object from an AI response. */
    public static JsonNode parseAiJson(String aiResponse, Logger logger) {
        try {
            String json = aiResponse.trim();
            if (json.contains("```")) {
                json = json.replaceAll("(?s)```[a-z]*\\s*", "").replaceAll("```", "").trim();
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

    /**
     * Scales coordinates from the AI's analyzed image space back to the capture (screenshot) pixel space.
     * Returns [capX1, capY1, capX2, capY2].
     */
    public static int[] scaleAiToCapture(int aiX1, int aiY1, int aiX2, int aiY2,
                                          int aiW, int aiH, int capW, int capH) {
        double sx = (double) capW / aiW;
        double sy = (double) capH / aiH;
        return new int[]{
                (int) Math.round(aiX1 * sx),
                (int) Math.round(aiY1 * sy),
                (int) Math.round(aiX2 * sx),
                (int) Math.round(aiY2 * sy)
        };
    }

    /**
     * Converts a physical-pixel center in the screenshot to CSS viewport coordinates.
     * Mirrors the test-engine ImageNLPService.relativeCoOrdinates approach.
     * Returns [cssX, cssY].
     */
    public static int[] toCssCenter(WebDriver driver, int capCX, int capCY,
                                     int capW, int capH, Logger logger) {
        long cssViewportW = capW;
        long cssViewportH = capH;
        try {
            cssViewportW = ((Number) ((JavascriptExecutor) driver)
                    .executeScript("return window.innerWidth")).longValue();
            cssViewportH = ((Number) ((JavascriptExecutor) driver)
                    .executeScript("return window.innerHeight")).longValue();
        } catch (Exception e) {
            logger.info("Could not read window.innerWidth/Height, falling back to screenshot dims: " + e.getMessage());
        }
        logger.info(String.format("Screenshot dims: %dx%d  |  CSS viewport (window.inner): %dx%d",
                capW, capH, cssViewportW, cssViewportH));
        return new int[]{
                (int) (((double) capCX / capW) * cssViewportW),
                (int) (((double) capCY / capH) * cssViewportH)
        };
    }

    /**
     * Annotates a screenshot with a bounding-box rectangle and a center dot.
     * rectColor is Color.MAGENTA for locate/hover actions; Color.GREEN for verify actions.
     */
    public static BufferedImage drawHighlight(BufferedImage original,
                                              int x1, int y1, int x2, int y2,
                                              int cx, int cy, Color rectColor) {
        BufferedImage copy = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);

        int rx1 = Math.max(0, x1);
        int ry1 = Math.max(0, y1);
        int rx2 = Math.min(original.getWidth()  - 1, x2);
        int ry2 = Math.min(original.getHeight() - 1, y2);

        g.setColor(rectColor);
        g.setStroke(new BasicStroke(2));
        g.drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1);

        g.setColor(Color.GREEN);
        g.fillOval(cx - 2, cy - 2, 4, 4);

        g.dispose();
        return copy;
    }

    public static BufferedImage toRgb(BufferedImage src) {
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    public static void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
