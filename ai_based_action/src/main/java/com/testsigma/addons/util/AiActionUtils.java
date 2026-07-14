package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.AI;
import com.testsigma.sdk.AIRequest;
import com.testsigma.sdk.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AiActionUtils {

    /** Used for click / locate actions (Vertex AI). */
    public static final String AI_MODEL = "anthropic.claude-opus-4-6";

    /** Used for verify and store (data extraction) actions (Anthropic direct). */
    public static final String AI_MODEL_ANTHROPIC = "claude-opus-4-8";

    /** Claude's real vision-input limits — sending anything larger makes it silently downscale
     *  internally, so callers should resize to this via {@link #fitWithinAiLimits} first and
     *  reason in that known pixel space rather than trusting whatever it echoes back. */
    public static final int MAX_IMAGE_EDGE = 1536;
    public static final long MAX_IMAGE_AREA = 1_150_000L;

    private static final String CUSTOM_INSTRUCTIONS =
            "<custom_instructions>\n" +
                    "{\n" +
                    "  \"provider\": \"vertex-ai\",\n" +
                    "  \"image_detail\": \"high\"\n" +
                    "}\n" +
                    "</custom_instructions>";

    private static final String CUSTOM_INSTRUCTIONS_ANTHROPIC =
            "<custom_instructions>\n" +
                    "{\n" +
                    "  \"provider\": \"anthropic\",\n" +
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
                    "\"cx\": <int>, \"cy\": <int>, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": <0-100>, \"description\": \"<what you found>\"}\n" +
                    "  where cx/cy is the VISUAL CENTER of the element (not simply (x1+x2)/2 — " +
                    "account for padding, text centering, and visual weight).\n" +
                    "If not found:\n" +
                    "  {\"found\": false, \"x1\": 0, \"y1\": 0, \"x2\": 0, \"y2\": 0, " +
                    "\"cx\": 0, \"cy\": 0, " +
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
            "STEP 2 — Produce the requested output:\n" +
                    "  The screenshot is OPTIONAL context — the prompt may or may not be about it. Always answer " +
                    "the prompt to the best of your ability. Never refuse just because the prompt is unrelated to " +
                    "the screenshot or because the screenshot is blank or empty.\n" +
                    "  If the answer is visible on screen, extract it exactly as it appears, set \"found\" to true, " +
                    "put the value in \"output\", and provide the bounding box of the relevant area.\n" +
                    "  If the answer is NOT on the screen (the screenshot is blank, empty, or simply not relevant), " +
                    "still answer the prompt directly — for example a math calculation, a unit/format conversion, a " +
                    "data transformation, generating a value, or a general question answerable from the prompt text " +
                    "or your own knowledge. In that case set \"found\" to true, put the result in \"output\", and " +
                    "use 0 for all bounding box coordinates (x1, y1, x2, y2) since there is nothing on screen to " +
                    "highlight.\n" +
                    "  Only set \"found\" to false in the rare case where the prompt asks for specific page content " +
                    "that is missing AND there is genuinely no way to compute or answer it; then leave \"output\" " +
                    "empty.\n\n";

    private static final String STORE_OUTPUT_FORMAT =
            "OUTPUT FORMAT — strict JSON only, no markdown, no explanation:\n" +
                    "If you have an answer (read from the screenshot OR computed/derived/answered from the prompt):\n" +
                    "  {\"found\": true, \"output\": \"<the extracted, computed, or derived answer>\", " +
                    "\"x1\": <int>, \"y1\": <int>, \"x2\": <int>, \"y2\": <int>, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": <0-100>, \"description\": \"<what was found, or how it was computed>\"}\n" +
                    "  (When the answer was computed/derived/answered rather than read from the screenshot, set " +
                    "x1=y1=x2=y2=0.)\n" +
                    "If there is genuinely no answer:\n" +
                    "  {\"found\": false, \"output\": \"\", " +
                    "\"x1\": 0, \"y1\": 0, \"x2\": 0, \"y2\": 0, " +
                    "\"imageWidth\": <int>, \"imageHeight\": <int>, " +
                    "\"confidence\": 0, \"description\": \"<why no answer could be produced>\"}\n\n" +
                    "EXTRACTION PROMPT: ";

    private static final String STORE_INTRO_SUFFIX =
            " A screenshot is provided as optional context. If the prompt asks about something visible in it, " +
                    "extract that exactly as it appears; otherwise (including when the screenshot is blank or " +
                    "unrelated) answer the prompt directly. Always produce an answer when one can reasonably be " +
                    "given.\n\n";

    public static final String STORE_PROMPT_WEB =
            "You are an assistant that answers the user's prompt and stores the result into a variable." +
                    STORE_INTRO_SUFFIX +
                    LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_ANDROID =
            "You are an assistant that answers the user's prompt and stores the result into a variable." +
                    STORE_INTRO_SUFFIX +
                    LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_IOS =
            "You are an assistant that answers the user's prompt and stores the result into a variable." +
                    STORE_INTRO_SUFFIX +
                    LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_MOBILE_WEB =
            "You are an assistant that answers the user's prompt and stores the result into a variable." +
                    STORE_INTRO_SUFFIX +
                    LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_SALESFORCE =
            "You are an assistant that answers the user's prompt and stores the result into a variable." +
                    STORE_INTRO_SUFFIX +
                    LOCATE_STEP1 + STORE_STEP2 + STORE_OUTPUT_FORMAT;

    public static final String STORE_PROMPT_DESKTOP =
            "You are an assistant that answers the user's prompt and stores the result into a variable." +
                    STORE_INTRO_SUFFIX +
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

    public static final String VERIFY_PROMPT_PDF =
            "You are a document verification assistant. Given an image of a single PDF page, determine whether " +
                    "the described content or condition is present and visible on that page.\n\n" +
                    VERIFY_STEP1 + VERIFY_STEP2 + VERIFY_OUTPUT_FORMAT;

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

    /**
     * Same as {@link #captureAsJpeg(BufferedImage, String, Logger)} but writes at an explicit
     * JPEG compression quality (0.0–1.0). The default ImageIO JPEG writer settings noticeably
     * soften small icons/text, which matters for zoomed-crop refine/verify passes that need the
     * AI to read fine detail precisely.
     */
    public static File captureAsJpeg(BufferedImage image, String prefix, float quality, Logger logger) throws Exception {
        BufferedImage rgb = image.getType() == BufferedImage.TYPE_INT_RGB ? image : toRgb(image);
        File file = File.createTempFile(prefix, ".jpg");
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
        logger.info(String.format("JPEG written (quality=%.2f): %s (size=%d bytes, dims=%dx%d)",
                quality, file.getAbsolutePath(), file.length(), image.getWidth(), image.getHeight()));
        return file;
    }

    /**
     * Reads the current screenshot, tolerating iOS Appium/WDA combos where BYTES capture is
     * unreliable by falling back to BASE64.
     */
    public static BufferedImage captureScreenshotWithFallback(TakesScreenshot driver, Logger logger) throws Exception {
        byte[] bytes;
        try {
            bytes = driver.getScreenshotAs(OutputType.BYTES);
        } catch (Exception ex) {
            logger.info("BYTES screenshot capture failed (" + ex.getMessage() + "); retrying with BASE64.");
            String base64 = driver.getScreenshotAs(OutputType.BASE64);
            bytes = Base64.getDecoder().decode(base64);
        }
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    /** Largest size within Claude's real vision limits ({@link #MAX_IMAGE_EDGE}/{@link #MAX_IMAGE_AREA}) that preserves aspect ratio. */
    public static int[] fitWithinAiLimits(int w, int h) {
        double scale = 1.0;
        int longEdge = Math.max(w, h);
        if (longEdge > MAX_IMAGE_EDGE) {
            scale = (double) MAX_IMAGE_EDGE / longEdge;
        }
        double area = (w * scale) * (h * scale);
        if (area > MAX_IMAGE_AREA) {
            scale *= Math.sqrt(MAX_IMAGE_AREA / area);
        }
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        return new int[]{nw, nh};
    }

    /** Builds the full AI prompt and invokes the AI service with a single screenshot. */
    public static String invokeAi(AI ai, File screenshotFile,
                                  String basePrompt, String query,
                                  Logger logger) throws Exception {
        return invokeAiWithFiles(ai, List.of(screenshotFile), basePrompt, query, logger);
    }

    /** Builds the full AI prompt and invokes the AI service with a single screenshot (Anthropic-direct first). */
    public static String invokeAiAnthropicFirst(AI ai, File screenshotFile,
                                                String basePrompt, String query,
                                                Logger logger) throws Exception {
        return invokeAiWithFilesAnthropicFirst(ai, List.of(screenshotFile), basePrompt, query, logger);
    }

    /**
     * Same as {@link #invokeAiAnthropicFirst(AI, File, String, String, Logger)} but states the
     * attached image's exact pixel dimensions up front in the prompt, so the model doesn't have
     * to (mis-)measure them itself — needed whenever the caller already resized the image and
     * must reason about the AI's returned coordinates in that exact known pixel space.
     */
    public static String invokeAiAnthropicFirst(AI ai, File screenshotFile,
                                                String basePrompt, String query,
                                                int imageWidth, int imageHeight,
                                                Logger logger) throws Exception {
        return invokeAiWithFilesAnthropicFirst(ai, List.of(screenshotFile), basePrompt, query, imageWidth, imageHeight, logger);
    }

    /**
     * Same as invokeAiWithFiles but with model priority reversed:
     * tries AI_MODEL_ANTHROPIC (anthropic direct) first, falls back to AI_MODEL (vertex-ai).
     */
    public static String invokeAiWithFilesAnthropicFirst(AI ai, List<File> files,
                                                         String basePrompt, String query,
                                                         Logger logger) throws Exception {
        // Primary: anthropic direct
        AIRequest primary = new AIRequest();
        primary.setPrompt(basePrompt + query + CUSTOM_INSTRUCTIONS_ANTHROPIC);
        primary.setModel(AI_MODEL_ANTHROPIC);
        primary.setFiles(files);
        logger.info("Sending AI request with model=" + AI_MODEL_ANTHROPIC + ", files=" + files.size() + "...");
        String response = ai.invokeAI(primary);
        logger.info("AI response: " + response);

        if (isBlankOrEmptyJson(response)) {
            // Fallback: vertex-ai
            logger.info("Primary model returned empty response, falling back to model=" + AI_MODEL);
            AIRequest fallback = new AIRequest();
            fallback.setPrompt(basePrompt + query + CUSTOM_INSTRUCTIONS);
            fallback.setModel(AI_MODEL);
            fallback.setFiles(files);
            response = ai.invokeAI(fallback);
            logger.info("Fallback AI response: " + response);
        }
        return response;
    }

    /** Same as {@link #invokeAiWithFilesAnthropicFirst(AI, List, String, String, Logger)} but states
     *  the attached images' exact known pixel dimensions up front in the prompt (see
     *  {@link #invokeAiAnthropicFirst(AI, File, String, String, int, int, Logger)}). */
    public static String invokeAiWithFilesAnthropicFirst(AI ai, List<File> files,
                                                         String basePrompt, String query,
                                                         int imageWidth, int imageHeight,
                                                         Logger logger) throws Exception {
        return invokeAiWithFilesAnthropicFirst(ai, files, withKnownDimensions(basePrompt, imageWidth, imageHeight), query, logger);
    }

    private static String withKnownDimensions(String basePrompt, int width, int height) {
        return String.format("IMAGE DIMENSIONS: this image is exactly %dx%d pixels — use these " +
                "exact values, do not re-measure or guess them.\n\n", width, height) + basePrompt;
    }

    /**
     * Sends multiple files in one request. Tries AI_MODEL (vertex-ai) first;
     * if the response is null or empty falls back to AI_MODEL_ANTHROPIC (anthropic direct).
     */
    public static String invokeAiWithFiles(AI ai, List<File> files,
                                           String basePrompt, String query,
                                           Logger logger) throws Exception {
        // Primary: vertex-ai
        AIRequest primary = new AIRequest();
        primary.setPrompt(basePrompt + query + CUSTOM_INSTRUCTIONS);
        primary.setModel(AI_MODEL);
        primary.setFiles(files);
        logger.info("Sending AI request with model=" + AI_MODEL + ", files=" + files.size() + "...");
        String response = ai.invokeAI(primary);
        logger.info("AI response: " + response);

        if (isBlankOrEmptyJson(response)) {
            // Fallback: anthropic direct
            logger.info("Primary model returned empty response, falling back to model=" + AI_MODEL_ANTHROPIC);
            AIRequest fallback = new AIRequest();
            fallback.setPrompt(basePrompt + query + CUSTOM_INSTRUCTIONS_ANTHROPIC);
            fallback.setModel(AI_MODEL_ANTHROPIC);
            fallback.setFiles(files);
            response = ai.invokeAI(fallback);
            logger.info("Fallback AI response: " + response);
        }
        return response;
    }

    private static boolean isBlankOrEmptyJson(String response) {
        if (response == null || response.isBlank()) return true;
        String trimmed = response.trim();
        return trimmed.isEmpty() || trimmed.equals("{}") || trimmed.equals("null");
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

    /**
     * Draws the final human-readable annotation: a bounding box rectangle + crosshair + green dot.
     * This makes it visually obvious that the dot is at the exact center of the detected element.
     * Only used for the result image shown to the user — NOT for the intermediate image sent to the AI.
     */
    public static BufferedImage drawFinalAnnotation(BufferedImage original,
                                                    int x1, int y1, int x2, int y2,
                                                    int cx, int cy) {
        BufferedImage copy = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, null);

        // ── Bounding box: scale inset and arc relative to element height so the
        //    annotation looks correct at any screen/viewport resolution.
        int elemH = y2 - y1;
        int INSET = Math.max(2, elemH / 12);   // ~8% of element height
        int ARC   = Math.max(6, elemH / 3);    // ~33% of element height (pill shape)
        int bx = x1 + INSET, by = y1 + INSET;
        int bw = (x2 - x1) - 2 * INSET, bh = elemH - 2 * INSET;
        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(bx, by, bw, bh, ARC, ARC);

        // ── Crosshair at click center ─────────────────────────────────────────
        final int ARM = Math.max(8, elemH / 3);
        g.setColor(Color.MAGENTA);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - ARM, cy, cx + ARM, cy);
        g.drawLine(cx, cy - ARM, cx, cy + ARM);

        // ── Green dot at the exact click point ───────────────────────────────
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

    /**
     * If the file is a PDF, flattens its AcroForm fields so that widget values stored only in
     * /V (but without an appearance stream /AP) are baked into visible page content before the
     * file is sent to Anthropic. Anthropic's renderer relies on appearance streams; without this
     * step, form fields whose /AP is empty appear blank even though the data exists in /V.
     *
     * Non-PDF files are returned unchanged. The flattened PDF is written to a temp file added to
     * tempFiles so it is cleaned up by the caller's finally block.
     */
    public static File flattenPdf(File file, Set<File> tempFiles, Logger logger) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".pdf")) {
            return file;
        }
        try {
            try (PDDocument doc = Loader.loadPDF(file)) {
                PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
                if (form != null && !form.getFields().isEmpty()) {
                    form.flatten();
                    logger.info("PDF AcroForm flattened: " + file.getName());
                }
                File flat = File.createTempFile("flat_", ".pdf");
                doc.save(flat);
                tempFiles.add(flat);
                logger.info("Flattened PDF written to: " + flat.getAbsolutePath());
                return flat;
            }
        } catch (Exception e) {
            logger.info("PDF flatten failed (using original): " + e.getMessage());
            return file;
        }
    }

    public static void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
