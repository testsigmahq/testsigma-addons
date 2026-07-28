package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.AI;
import com.testsigma.sdk.AIRequest;
import com.testsigma.sdk.Logger;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * AI-based text helpers — the {@link ApproachConfig.Approach#AI} counterpart to
 * {@link OCRUtils} for "is this text present on screen?" checks.
 *ikj98xzxW@32232XW3
 * Instead of calling the visual-server OCR endpoint and string-matching the
 * returned text points, this asks the LLM (via the {@code @AI} SDK capability)
 * to look at the screenshot and decide whether the target text is visible.
 */
public class AiTextUtils {

    /** Primary model — used for all AI calls in this add-on. */
    public static final String AI_MODEL_PRIMARY = "claude-opus-4-6";

    /** Fallback model — used only if the primary model returns an empty/unparseable response. */
    public static final String AI_MODEL_FALLBACK = "anthropic.claude-opus-4-6";

    private static final String PRESENCE_PROMPT =
            "You are given a screenshot of a desktop application screen. " +
            "Determine whether the following text is visible anywhere in the screenshot. " +
            "Match the text even if it appears as part of a larger string; ignore case and " +
            "surrounding whitespace. Respond in strict JSON only, with no markdown and no extra text: " +
            "{\"isPresent\": <true|false>, \"reason\": \"<short note on where it was or was not found>\"} " +
            "The text to find is: \"";

    private static final String CUSTOM_INSTRUCTIONS_ANTHROPIC =
            "<custom_instructions>\n" +
                    "{\n" +
                    "  \"provider\": \"anthropic\",\n" +
                    "  \"image_detail\": \"high\"\n" +
                    "}\n" +
                    "</custom_instructions>";

    private static final String CUSTOM_INSTRUCTIONS_VERTEX =
            "<custom_instructions>\n" +
                    "{\n" +
                    "  \"provider\": \"vertex-ai\",\n" +
                    "  \"image_detail\": \"high\"\n" +
                    "}\n" +
                    "</custom_instructions>";

    private AiTextUtils() {
    }

    /**
     * Asks the AI whether {@code expectedText} is visible in the given screenshot.
     *
     * @return true if the AI reports the text is present, false otherwise.
     * @throws Exception if the AI call or response parsing fails (so callers can
     *                   treat it the same as an OCR failure).
     */
    public static boolean isTextPresent(AI ai, File screenshotFile, String expectedText, Logger logger)
            throws Exception {
        if (ai == null) {
            throw new IllegalStateException(
                    "AI capability is not available (the @AI field was not injected).");
        }

        String prompt = PRESENCE_PROMPT + expectedText + "\"";

        logger.info("Making AI text-presence call for: '" + expectedText + "'");
        String aiResponse = invokeAiWithFallback(ai, screenshotFile, prompt, logger);

        JsonNode node = parseJson(aiResponse, logger);
        if (node == null) {
            throw new RuntimeException("Failed to parse AI response for text presence check");
        }

        boolean present = node.path("isPresent").asBoolean(false);
        logger.info("AI text-presence result: " + present
                + " | reason: " + node.path("reason").asText(""));
        return present;
    }

    private static JsonNode parseJson(String aiResponse, Logger logger) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(extractJsonObject(aiResponse));
        } catch (Exception e) {
            logger.warn("Failed to parse AI response JSON: " + e.getMessage());
            logger.warn("Raw AI response: " + aiResponse);
            return null;
        }
    }

    /**
     * Some models (notably the reasoning-style fallback) prepend free-text explanation
     * before the JSON answer, sometimes wrapped in a ```json ... ``` fence. Strip that
     * down to the outermost JSON object so callers can feed it straight to Jackson.
     */
    public static String extractJsonObject(String response) {
        if (response == null) {
            return null;
        }
        String text = response.trim();

        int fenceStart = text.indexOf("```");
        if (fenceStart != -1) {
            int contentStart = text.indexOf('\n', fenceStart);
            contentStart = contentStart == -1 ? fenceStart + 3 : contentStart + 1;
            int fenceEnd = text.indexOf("```", contentStart);
            if (fenceEnd != -1) {
                text = text.substring(contentStart, fenceEnd).trim();
            }
        }

        int braceStart = text.indexOf('{');
        int braceEnd = text.lastIndexOf('}');
        if (braceStart != -1 && braceEnd > braceStart) {
            text = text.substring(braceStart, braceEnd + 1);
        }

        return text;
    }

    /**
     * Invokes the AI with a single screenshot using {@link #AI_MODEL_PRIMARY}, falling back to
     * {@link #AI_MODEL_FALLBACK} if the primary model returns a blank/empty response. Shared by
     * every AI call site in this add-on so a provider outage on one model doesn't fail the step.
     */
    public static String invokeAiWithFallback(AI ai, File screenshotFile, String prompt, Logger logger)
            throws Exception {
        File jpegFile = toJpegFile(screenshotFile, logger);
        try {
            AIRequest primaryRequest = new AIRequest();
            primaryRequest.setPrompt(prompt + CUSTOM_INSTRUCTIONS_ANTHROPIC);
            primaryRequest.setFiles(List.of(jpegFile));
            primaryRequest.setModel(AI_MODEL_PRIMARY);

            logger.info("Sending AI request with model=" + AI_MODEL_PRIMARY);
            String response = ai.invokeAI(primaryRequest);
            logger.info("AI response: " + response);

            if (isBlankOrEmptyJson(response)) {
                logger.info("Primary model returned an empty response, falling back to model=" + AI_MODEL_FALLBACK);
                AIRequest fallbackRequest = new AIRequest();
                fallbackRequest.setPrompt(prompt + CUSTOM_INSTRUCTIONS_VERTEX);
                fallbackRequest.setFiles(List.of(jpegFile));
                fallbackRequest.setModel(AI_MODEL_FALLBACK);
                response = ai.invokeAI(fallbackRequest);
                logger.info("Fallback AI response: " + response);
            }
            return response;
        } finally {
            if (jpegFile != screenshotFile) {
                deleteQuietly(jpegFile);
            }
        }
    }

    /**
     * The AI request always declares an image/jpeg media type, so a non-JPEG screenshot
     * (e.g. the PNG files these actions save) must be re-encoded as a real JPEG first —
     * otherwise the provider rejects it with a media-type/content mismatch error.
     */
    private static File toJpegFile(File source, Logger logger) throws Exception {
        String name = source.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return source;
        }
        BufferedImage image = ImageIO.read(source);
        BufferedImage rgb = image.getType() == BufferedImage.TYPE_INT_RGB ? image : toRgb(image);
        File jpegFile = File.createTempFile("ai_request_image", ".jpg");
        ImageIO.write(rgb, "JPEG", jpegFile);
        logger.info("Converted screenshot to JPEG for AI request: " + jpegFile.getAbsolutePath());
        return jpegFile;
    }

    private static BufferedImage toRgb(BufferedImage src) {
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private static void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private static boolean isBlankOrEmptyJson(String response) {
        if (response == null || response.isBlank()) return true;
        String trimmed = response.trim();
        return trimmed.isEmpty() || trimmed.equals("{}") || trimmed.equals("null");
    }
}
