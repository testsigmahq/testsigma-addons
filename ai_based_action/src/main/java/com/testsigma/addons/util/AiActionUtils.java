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

    // ── Store prompts (per platform) ──

    private static final String STORE_STEP2 =
            "STEP 2 — Extract the requested content:\n" +
            "  Carefully examine the screenshot for the content described in the prompt.\n" +
            "  Consider text, numbers, labels, values, dates, and any visible data on the page.\n" +
            "  If the requested content is found, set \"found\" to true, provide the exact extracted " +
            "text or value in \"output\", and provide the bounding box of the relevant area.\n" +
            "  If the content is NOT found, set \"found\" to false and leave \"output\" empty.\n\n";

    private static final String STORE_OUTPUT_FORMAT =
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
            "If found:\n" +
            "  {\"found\": true, \"output\": \"<exact extracted text or value>\", " +
            "\"x1\": <int>, \"y1\": <int>, \"x2\": <int>, \"y2\": <int>, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<what was found and where>\"}\n" +
            "If NOT found:\n" +
            "  {\"found\": false, \"output\": \"\", " +
            "\"x1\": 0, \"y1\": 0, \"x2\": 0, \"y2\": 0, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": 0, \"description\": \"<why the content was not found>\"}\n\n" +
            "EXTRACTION PROMPT: ";

    public static final String STORE_PROMPT_WEB =
            "You are a UI content extraction assistant. Given a web page screenshot, extract the specific " +
            "content or value described in the prompt and return it exactly as it appears on screen.\n\n" +
            LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_ANDROID =
            "You are a UI content extraction assistant. Given an Android device screenshot, extract the specific " +
            "content or value described in the prompt and return it exactly as it appears on screen.\n\n" +
            LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_IOS =
            "You are a UI content extraction assistant. Given an iOS device screenshot, extract the specific " +
            "content or value described in the prompt and return it exactly as it appears on screen.\n\n" +
            LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_MOBILE_WEB =
            "You are a UI content extraction assistant. Given a mobile web page screenshot, extract the specific " +
            "content or value described in the prompt and return it exactly as it appears on screen.\n\n" +
            LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_SALESFORCE =
            "You are a UI content extraction assistant. Given a Salesforce page screenshot, extract the specific " +
            "content or value described in the prompt and return it exactly as it appears on screen.\n\n" +
            LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_DESKTOP =
            "You are a UI content extraction assistant. Given a desktop application screenshot, extract the specific " +
            "content or value described in the prompt and return it exactly as it appears on screen.\n\n" +
            LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

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

    // ── Scroll-verify prompts (multi-screenshot) ──

    private static final String VERIFY_SCROLL_STEP2 =
            "STEP 2 — Verify the condition across all screenshots:\n" +
            "  Carefully examine ALL THREE screenshots for the described content.\n" +
            "  The images are ordered: Image 1 = top of the page, Image 2 = after first scroll, Image 3 = after second scroll.\n" +
            "  Consider text, images, UI elements, colors, layout, and any visible state in each image.\n" +
            "  If the described content is found in any screenshot, set \"verified\" to true, report " +
            "which screenshot it was found in as \"imageIndex\" (1, 2, or 3), and provide the bounding box " +
            "of the relevant area within that screenshot.\n" +
            "  If the described content is NOT found in any of the screenshots, set \"verified\" to false.\n\n";

    private static final String VERIFY_SCROLL_OUTPUT_FORMAT =
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
            "If verified (content found in one of the screenshots):\n" +
            "  {\"verified\": true, \"imageIndex\": <1|2|3>, " +
            "\"x1\": <int>, \"y1\": <int>, \"x2\": <int>, \"y2\": <int>, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<what was found and in which screenshot>\"}\n" +
            "If NOT verified (content not found in any screenshot):\n" +
            "  {\"verified\": false, \"imageIndex\": 0, " +
            "\"x1\": 0, \"y1\": 0, \"x2\": 0, \"y2\": 0, " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<what was expected but not found>\"}\n\n" +
            "VERIFICATION QUERY: ";

    public static final String VERIFY_SCROLL_PROMPT_IOS =
            "You are a UI verification assistant. You are given 3 sequential screenshots of an iOS screen " +
            "taken while scrolling down. Image 1 is the initial view. Image 2 is after the first scroll. " +
            "Image 3 is after the second scroll.\n\n" +
            "STEP 1 — Note the image dimensions:\n" +
            "  All three images share the same pixel dimensions (width × height).\n" +
            "  You MUST include these as \"imageWidth\" and \"imageHeight\" in your JSON response.\n\n" +
            VERIFY_SCROLL_STEP2 + VERIFY_SCROLL_OUTPUT_FORMAT;

    public static final String VERIFY_SCROLL_PROMPT_ANDROID =
            "You are a UI verification assistant. You are given 3 sequential screenshots of an Android screen " +
            "taken while scrolling down. Image 1 is the initial view. Image 2 is after the first scroll. " +
            "Image 3 is after the second scroll.\n\n" +
            "STEP 1 — Note the image dimensions:\n" +
            "  All three images share the same pixel dimensions (width × height).\n" +
            "  You MUST include these as \"imageWidth\" and \"imageHeight\" in your JSON response.\n\n" +
            VERIFY_SCROLL_STEP2 + VERIFY_SCROLL_OUTPUT_FORMAT;

    // ── PDF comparison prompt ──

    public static final String COMPARE_PDF_PROMPT =
            "You are a PDF page comparison assistant. You are given two images of the same PDF page:\n" +
            "Image 1: The BASE (expected/reference) version\n" +
            "Image 2: The ACTUAL (produced/test) version\n\n" +
            "STEP 1 — Note the image dimensions:\n" +
            "  Note the pixel dimensions shared by both images.\n" +
            "  You MUST include these as \"imageWidth\" and \"imageHeight\" in your JSON response.\n\n" +
            "STEP 2 — Compare both pages thoroughly:\n" +
            "  Check for differences in: text content (missing, extra, or changed words), layout and " +
            "positioning, images and graphics, tables (structure and cell content), headers/footers, " +
            "page numbers, fonts, sizes, and text formatting.\n" +
            "  If the user has provided additional instructions (e.g. regions to ignore or aspects to " +
            "focus on), follow them strictly.\n" +
            "  For each difference found, provide a bounding box in the 'regions' array marking the " +
            "area of the difference (x1, y1 = top-left corner, x2, y2 = bottom-right corner).\n\n" +
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
            "If pages match (no meaningful differences):\n" +
            "  {\"match\": true, \"differences\": [], \"regions\": [], " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<brief summary why pages are considered equal>\"}\n" +
            "If pages differ:\n" +
            "  {\"match\": false, \"differences\": [\"<difference 1>\", \"<difference 2>\", ...], " +
            "\"regions\": [{\"x1\": <int>, \"y1\": <int>, \"x2\": <int>, \"y2\": <int>}, ...], " +
            "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
            "\"confidence\": <0-100>, \"description\": \"<concise summary of all differences found>\"}\n\n" +
            "Additional instructions: ";

    // ── File extraction prompt ──

    public static final String EXTRACT_FROM_FILE_PROMPT =
            "You are a file content extraction assistant. You are given a file (which may be a PDF, image, " +
            "or other document). Extract the specific content or value described in the extraction prompt " +
            "and return it exactly as it appears in the file.\n\n" +
            "STEP 1 — Analyze the file:\n" +
            "  Carefully examine all content in the provided file.\n" +
            "  Consider text, numbers, tables, images, headers, footers, and any visible data.\n\n" +
            "STEP 2 — Extract the requested content:\n" +
            "  If the requested content is found, set \"found\" to true and provide the exact extracted " +
            "text or value in \"output\".\n" +
            "  If the content is NOT found, set \"found\" to false and leave \"output\" empty.\n\n" +
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
            "If found:\n" +
            "  {\"found\": true, \"output\": \"<exact extracted text or value>\", " +
            "\"confidence\": <0-100>, \"description\": \"<what was found and where in the file>\"}\n" +
            "If NOT found:\n" +
            "  {\"found\": false, \"output\": \"\", " +
            "\"confidence\": 0, \"description\": \"<why the content was not found>\"}\n\n" +
            "EXTRACTION PROMPT: ";

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

    /** Builds the full AI prompt and invokes the AI service with a single screenshot. */
    public static String invokeAi(AI ai, File screenshotFile,
                                   String basePrompt, String query,
                                   Logger logger) throws Exception {
        return invokeAiWithFiles(ai, List.of(screenshotFile), basePrompt, query, logger);
    }

    /**
     * Variant that sends multiple files in one request — used for two-image verification
     * where Screenshot A (clean) and Screenshot B (annotated) are attached together.
     */
    public static String invokeAiWithFiles(AI ai, List<File> files,
                                            String basePrompt, String query,
                                            Logger logger) throws Exception {
        AIRequest aiRequest = new AIRequest();
        aiRequest.setPrompt(basePrompt + query + CUSTOM_INSTRUCTIONS);
        aiRequest.setModel(AI_MODEL);
        aiRequest.setFiles(files);
        logger.info("Sending AI request with " + files.size() + " file(s)...");
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
     * Reads the CSS viewport dimensions via JavaScript.
     * Falls back to the physical screenshot dimensions if JS fails.
     * Returns [cssW, cssH].
     */
    public static int[] getCssViewport(WebDriver driver, int physicalW, int physicalH, Logger logger) {
        int cssW = physicalW;
        int cssH = physicalH;
        try {
            cssW = ((Number) ((JavascriptExecutor) driver)
                    .executeScript("return window.innerWidth")).intValue();
            cssH = ((Number) ((JavascriptExecutor) driver)
                    .executeScript("return window.innerHeight")).intValue();
        } catch (Exception e) {
            logger.info("Could not read CSS viewport, using physical dims: " + e.getMessage());
        }
        return new int[]{cssW, cssH};
    }

    /**
     * Resizes a BufferedImage to targetW x targetH using bicubic interpolation.
     * Returns the original image unchanged if it already matches the target size.
     */
    public static BufferedImage resizeImage(BufferedImage src, int targetW, int targetH) {
        if (src.getWidth() == targetW && src.getHeight() == targetH) return src;
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();
        return out;
    }

    /**
     * Converts a physical-pixel center in the screenshot to CSS viewport coordinates.
     * Mirrors the test-engine ImageNLPService.relativeCoOrdinates approach.
     * Returns [cssX, cssY].
     */
    public static int[] toCssCenter(WebDriver driver, int capCX, int capCY,
                                     int capW, int capH, Logger logger) {
        int[] css = getCssViewport(driver, capW, capH, logger);
        logger.info(String.format("Screenshot dims: %dx%d  |  CSS viewport (window.inner): %dx%d",
                capW, capH, css[0], css[1]));
        return new int[]{
                (int) Math.round(((double) capCX / capW) * css[0]),
                (int) Math.round(((double) capCY / capH) * css[1])
        };
    }

    /**
     * Annotates a screenshot with a green dot at the click point and a left-pointing arrow
     * whose tip stops at least 20 px to the left of the dot. No bounding box is drawn —
     * the arrow + dot combination is unambiguous for the verification AI and avoids
     * confusing the overlay with actual UI elements.
     *
     * x1/y1/x2/y2 are kept in the signature for API compatibility but are not used.
     * rectColor is used for the arrow (Color.MAGENTA for locate/hover, Color.GREEN for verify).
     */
    public static BufferedImage drawHighlight(BufferedImage original,
                                              int x1, int y1, int x2, int y2,
                                              int cx, int cy, Color rectColor) {
        BufferedImage copy = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, null);

        // ── Arrow from the left, tip ≥ 20 px away from the dot center ─────────
        final int GAP      = 20;  // minimum gap between dot center and arrowhead tip
        final int SHAFT    = 35;  // shaft length
        final int HEAD_LEN = 10;  // arrowhead depth (pointing right →)
        final int HEAD_W   =  7;  // arrowhead half-width

        int tipX  = Math.max(HEAD_LEN, cx - GAP);          // arrowhead tip x
        int tipY  = cy;
        int tailX = Math.max(0, tipX - HEAD_LEN - SHAFT);  // shaft tail x

        g.setColor(rectColor);
        g.setStroke(new BasicStroke(2));

        // Shaft — only drawn when there is room between tail and arrowhead base
        if (tailX < tipX - HEAD_LEN) {
            g.drawLine(tailX, tipY, tipX - HEAD_LEN, tipY);
        }

        // Filled arrowhead triangle pointing right (→)
        int[] arrowX = { tipX, tipX - HEAD_LEN, tipX - HEAD_LEN };
        int[] arrowY = { tipY, tipY - HEAD_W,   tipY + HEAD_W   };
        g.fillPolygon(arrowX, arrowY, 3);

        // ── Green dot at the exact click point ───────────────────────────────
        // Black outline first for contrast on any background colour
        g.setColor(Color.BLACK);
        g.fillOval(cx - 7, cy - 7, 14, 14);
        g.setColor(Color.GREEN);
        g.fillOval(cx - 5, cy - 5, 10, 10);

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
