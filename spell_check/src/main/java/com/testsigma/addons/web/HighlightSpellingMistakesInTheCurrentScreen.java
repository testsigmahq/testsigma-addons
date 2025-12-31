package com.testsigma.addons.web;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.AI;
import com.testsigma.sdk.annotation.OCR;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Highlight the spelling mistakes in the current screen",
        description = "This action highlights the spelling mistakes in the current screen by taking screenshot.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)
public class HighlightSpellingMistakesInTheCurrentScreen extends WebAction {
    @AI
    private com.testsigma.sdk.AI ai;

    @OCR
    private com.testsigma.sdk.OCR ocr;
    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    private final RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(60000)
            .setConnectionRequestTimeout(60000)
            .setSocketTimeout(60000)
            .build();

    // Highlight styling constants
    private static final int HIGHLIGHT_PADDING = 3;
    private static final int MIN_HIGHLIGHT_WIDTH = 30;
    private static final int MIN_HIGHLIGHT_HEIGHT = 15;
    private static final int BORDER_STROKE_WIDTH = 3;
    private static final Color HIGHLIGHT_FILL_COLOR = new Color(255, 0, 0, 80);
    private static final Color HIGHLIGHT_BORDER_COLOR = new Color(255, 0, 0, 255);

    // OCR processing constants
    private static final int VERTICAL_DIFFERENCE_THRESHOLD = 300;
    private static final int MAX_TEXT_POINT_WIDTH = 200;
    private static final int MAX_TEXT_POINT_HEIGHT = 50;

    // Compiled regex patterns for better performance
    private static final Pattern MISSPELLED_PATTERN = Pattern.compile("MISSPELLED_WORDS:\\s*\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern CORRECTIONS_PATTERN = Pattern.compile("corrections?:\\s*\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTE_PATTERN = Pattern.compile("['\"](\\w+)['\"]\\s*(?:should be|->|→|=>)\\s*['\"](\\w+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBERED_PATTERN = Pattern.compile("\\d+\\.\\s*['\"](\\w+)['\"]", Pattern.CASE_INSENSITIVE);

    private final String basePrompt = "You are provided with an image of a web page and OCR-extracted text from it. " +
            "Your task is to perform a PURE SPELLING CHECK - focus ONLY on word spelling, not meaning or context. Follow these steps:\n" +
            "1. Analyze the image and the provided text for spelling mistakes\n" +
            "2. For each word, check if it follows standard English spelling rules\n" +
            "3. Identify words that are misspelled according to standard dictionaries\n" +
            "4. For each spelling mistake found, provide:\n" +
            "   - The misspelled word (exactly as it appears)\n" +
            "   - The correct spelling\n" +
            "5. DO NOT consider:\n" +
            "   - Whether the word makes sense in context\n" +
            "   - Technical terms or brand names (check their spelling too)\n" +
            "   - Grammar or punctuation\n" +
            "   - Intentional misspellings or creative writing\n\n" +
            "6. If no spelling mistakes are found, respond with 'PASS: No spelling mistakes detected'\n\n" +
            "CRITICAL RULES:\n" +
            "- This is a pure spelling validation. If you see 'recieve' instead of 'receive', 'seperate' instead of 'separate', etc., mark them as spelling errors regardless of context.\n" +
            "- DO NOT flag correctly spelled words. If a word is spelled correctly (e.g., 'Ethnicity', 'Gender', 'Address'), do NOT include it in the misspelled words list.\n" +
            "- The correction MUST be different from the original word. If the correction is the same as the original, the word is correctly spelled and should not be flagged.\n\n" +
            "Respond with either:\n" +
            "- 'PASS: No spelling mistakes detected' if no errors found\n" +
            "- 'FAIL: Found [X] spelling mistakes: MISSPELLED_WORDS: [word1, word2, word3] with corrections: [correction1, correction2, correction3]' if errors are found\n" +
            "IMPORTANT: Always include the MISSPELLED_WORDS: [...] section with the exact misspelled words in a comma-separated list inside brackets, and ensure each correction is different from its corresponding misspelled word.\n\n";

    @Override
    public Result execute() {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            if (screenshot == null || !screenshot.exists()) {
                setErrorMessage("Failed to capture screenshot");
                return Result.FAILED;
            }
            logger.info("Screenshot taken: " + screenshot.getAbsolutePath());

            List<OCRTextPoint> textPoints = extractTextPoints(screenshot);
            if (textPoints == null || textPoints.isEmpty()) {
                setSuccessMessage("No text found in the screenshot to check for spelling.");
                return Result.SUCCESS;
            }

            String finalText = extractTextForSpellCheck(textPoints);
            if (finalText.isEmpty()) {
                setSuccessMessage("No text found in the screenshot to check for spelling.");
                return Result.SUCCESS;
            }

            String aiResponse = invokeAIForSpellCheck(screenshot, finalText);
            return processAIResponse(aiResponse, textPoints, screenshot);
        } catch (Exception e) {
            logger.warn("Exception during spelling check: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error during spelling check: " + e.getMessage());
            return Result.FAILED;
        }
    }

    private List<OCRTextPoint> extractTextPoints(File screenshot) {
        OCRImage imageObj = new OCRImage();
        imageObj.setOcrImageFile(screenshot);
        List<OCRTextPoint> textPoints = ocr.extractTextFromImage(imageObj);
        logger.info("Extracted text from image: " + textPoints);
        return textPoints;
    }

    private String extractTextForSpellCheck(List<OCRTextPoint> textPoints) {
        Set<String> uniqueTexts = new LinkedHashSet<>();
        int previousY1 = -1;
        int maxOCRSectionCount = 0;

        for (OCRTextPoint textPoint : textPoints) {
            String text = textPoint.getText();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }
            uniqueTexts.add(text.trim());
            int currentY1 = textPoint.getY1();

            if (previousY1 != -1) {
                int verticalDifference = Math.abs(currentY1 - previousY1);
                if (currentY1 < previousY1 && verticalDifference > VERTICAL_DIFFERENCE_THRESHOLD && maxOCRSectionCount >= 1) {
                    logger.info("Stopping text extraction at y1=" + currentY1 + " (previous y1=" + previousY1 +
                            " vertical difference=" + verticalDifference + ") - likely new section or column");
                    break;
                }
                if (verticalDifference > VERTICAL_DIFFERENCE_THRESHOLD) {
                    maxOCRSectionCount++;
                }
            }
            previousY1 = currentY1;
        }

        String finalText = String.join(" ", uniqueTexts);
        logger.info("All extracted text combined: '" + finalText + "'");
        return finalText;
    }

    private String invokeAIForSpellCheck(File screenshot, String finalText) {
        AIRequest aiRequest = new AIRequest();
        String fullPrompt = basePrompt + "Text to check: " + finalText;
        aiRequest.setPrompt(fullPrompt);
        aiRequest.setModel("gpt-4o");

        List<File> files = new ArrayList<>();
        files.add(screenshot);
        aiRequest.setFiles(files);

        logger.info("Sending AI prompt with screenshot:\n" + fullPrompt);
        String aiResponse = ai.invokeAI(aiRequest);
        logger.info("AI response: " + aiResponse);
        return aiResponse;
    }

    private Result processAIResponse(String aiResponse, List<OCRTextPoint> textPoints, File screenshot) {
        String response = aiResponse.trim();

        if (response.toUpperCase().contains("PASS")) {
            logger.info("AI spelling check passed - no spelling mistakes detected");
            setSuccessMessage("No spelling mistakes found " + aiResponse);
            return Result.SUCCESS;
        }

        if (response.toUpperCase().contains("FAIL")) {
            return handleSpellingMistakes(aiResponse, textPoints, screenshot);
        }

        logger.warn("Unexpected AI response format: " + aiResponse);
        setErrorMessage("Unexpected response format: " + aiResponse);
        return Result.FAILED;
    }

    private Result handleSpellingMistakes(String aiResponse, List<OCRTextPoint> textPoints, File screenshot) {
        logger.warn("AI spelling check failed - spelling mistakes detected");

        List<String> misspelledWords = extractMisspelledWords(aiResponse);
        logger.info("Extracted misspelled words: " + misspelledWords);

        if (misspelledWords.isEmpty()) {
            setErrorMessage("Found spelling mistakes : " + aiResponse);
            return Result.FAILED;
        }

        List<WordBoundingBox> wordBoundingBoxes = findMisspelledWordBoundingBoxes(textPoints, misspelledWords);
        logger.info("Found " + wordBoundingBoxes.size() + " word bounding boxes for misspelled words");

        if (!wordBoundingBoxes.isEmpty()) {
            File highlightedScreenshot = highlightMisspelledWords(screenshot, wordBoundingBoxes);
            uploadHighlightedScreenshot(highlightedScreenshot);
        }

        setErrorMessage("Found spelling mistakes : " + aiResponse);
        return Result.FAILED;
    }

    private void uploadHighlightedScreenshot(File highlightedScreenshot) {
        if (highlightedScreenshot == null) {
            return;
        }

        String s3SignedURL = testStepResult.getScreenshotUrl();
        if (s3SignedURL == null || s3SignedURL.isEmpty()) {
            logger.warn("Screenshot upload URL is not available");
            return;
        }

        boolean uploaded = uploadFile(s3SignedURL, highlightedScreenshot.getAbsolutePath());
        if (uploaded) {
            logger.info("Highlighted screenshot uploaded successfully");
        } else {
            logger.warn("Failed to upload highlighted screenshot");
        }
    }

    /**
     * Extract misspelled words from AI response, filtering out false positives
     */
    private List<String> extractMisspelledWords(String aiResponse) {
        List<String> misspelledWords = new ArrayList<>();
        Matcher misspelledMatcher = MISSPELLED_PATTERN.matcher(aiResponse);
        Matcher correctionsMatcher = CORRECTIONS_PATTERN.matcher(aiResponse);

        if (misspelledMatcher.find()) {
            String[] words = misspelledMatcher.group(1).split(",");
            List<String> corrections = extractCorrections(correctionsMatcher);

            for (int i = 0; i < words.length; i++) {
                String word = words[i].trim().replaceAll("[\"']", "");
                if (word.isEmpty()) {
                    continue;
                }

                String lowerWord = word.toLowerCase();
                if (i < corrections.size() && lowerWord.equals(corrections.get(i))) {
                    logger.info("Filtered out false positive: '" + word + "' (correction is the same)");
                    continue;
                }

                misspelledWords.add(lowerWord);
                if (i < corrections.size()) {
                    logger.info("Added misspelled word: '" + word + "' (correction: '" + corrections.get(i) + "')");
                }
            }
        } else {
            extractFromFallbackPatterns(aiResponse, misspelledWords);
        }

        return misspelledWords;
    }

    private List<String> extractCorrections(Matcher correctionsMatcher) {
        List<String> corrections = new ArrayList<>();
        if (correctionsMatcher.find()) {
            String[] correctionArray = correctionsMatcher.group(1).split(",");
            for (String correction : correctionArray) {
                String cleanCorrection = correction.trim().replaceAll("[\"']", "");
                if (!cleanCorrection.isEmpty()) {
                    corrections.add(cleanCorrection.toLowerCase());
                }
            }
        }
        return corrections;
    }

    private void extractFromFallbackPatterns(String aiResponse, List<String> misspelledWords) {
        Matcher quoteMatcher = QUOTE_PATTERN.matcher(aiResponse);
        while (quoteMatcher.find()) {
            String misspelled = quoteMatcher.group(1).toLowerCase();
            String correction = quoteMatcher.group(2).toLowerCase();
            if (!misspelled.equals(correction)) {
                misspelledWords.add(misspelled);
            }
        }

        Matcher numberedMatcher = NUMBERED_PATTERN.matcher(aiResponse);
        while (numberedMatcher.find()) {
            String word = numberedMatcher.group(1).toLowerCase();
            if (!misspelledWords.contains(word)) {
                misspelledWords.add(word);
            }
        }
    }

    /**
     * Helper class to store word bounding box information
     */
    private static class WordBoundingBox {
        int x1, y1, x2, y2;
        String word;

        WordBoundingBox(int x1, int y1, int x2, int y2, String word) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.word = word;
        }
    }

    /**
     * Find precise bounding boxes for misspelled words within OCR text points.
     * Prioritizes EXACT matches over substring matches to ensure accurate highlighting.
     */
    private List<WordBoundingBox> findMisspelledWordBoundingBoxes(List<OCRTextPoint> allTextPoints, List<String> misspelledWords) {
        List<WordBoundingBox> wordBoundingBoxes = new ArrayList<>();
        Set<String> foundMisspelledWords = new HashSet<>();

        for (String misspelledWord : misspelledWords) {
            if (foundMisspelledWords.contains(misspelledWord.toLowerCase())) {
                continue;
            }

            WordBoundingBox wordBox = findExactMatch(allTextPoints, misspelledWord);
            if (wordBox != null) {
                wordBoundingBoxes.add(wordBox);
                foundMisspelledWords.add(misspelledWord.toLowerCase());
                continue;
            }

            wordBox = findPartialMatch(allTextPoints, misspelledWord);
            if (wordBox != null) {
                wordBoundingBoxes.add(wordBox);
                foundMisspelledWords.add(misspelledWord.toLowerCase());
            }
        }

        return wordBoundingBoxes;
    }

    private WordBoundingBox findExactMatch(List<OCRTextPoint> textPoints, String misspelledWord) {
        String lowerMisspelled = misspelledWord.toLowerCase();

        for (OCRTextPoint textPoint : textPoints) {
            String text = textPoint.getText();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }

            String lowerText = text.toLowerCase().trim();
            if (lowerText.equals(lowerMisspelled)) {
                logger.info("EXACT match found for '" + misspelledWord + "' at OCR text: '" + text +
                        "' coords: x1=" + textPoint.getX1() + ", y1=" + textPoint.getY1() +
                        ", x2=" + textPoint.getX2() + ", y2=" + textPoint.getY2());
                return new WordBoundingBox(
                        textPoint.getX1(), textPoint.getY1(),
                        textPoint.getX2(), textPoint.getY2(),
                        misspelledWord
                );
            }
        }
        return null;
    }

    private WordBoundingBox findPartialMatch(List<OCRTextPoint> textPoints, String misspelledWord) {
        String lowerMisspelled = misspelledWord.toLowerCase();

        for (OCRTextPoint textPoint : textPoints) {
            String text = textPoint.getText();
            if (text == null || text.trim().isEmpty() || text.length() > misspelledWord.length() * 2) {
                continue;
            }

            int textPointWidth = textPoint.getX2() - textPoint.getX1();
            int textPointHeight = textPoint.getY2() - textPoint.getY1();
            if (textPointWidth > MAX_TEXT_POINT_WIDTH || textPointHeight > MAX_TEXT_POINT_HEIGHT) {
                continue;
            }

            String lowerText = text.toLowerCase().trim();
            int wordIndex = lowerText.indexOf(lowerMisspelled);
            if (wordIndex >= 0) {
                WordBoundingBox wordBox = calculateWordBoundingBox(textPoint, text, misspelledWord, wordIndex);
                if (wordBox != null) {
                    logger.info("Close match found for '" + misspelledWord + "' in text: '" + text +
                            "' coords: x1=" + textPoint.getX1() + ", y1=" + textPoint.getY1());
                    return wordBox;
                }
            }
        }
        return null;
    }

    /**
     * Calculate approximate bounding box for a specific word within a text block
     */
    private WordBoundingBox calculateWordBoundingBox(OCRTextPoint textPoint, String fullText, String word, int wordStartIndex) {
        int x1 = textPoint.getX1();
        int y1 = textPoint.getY1();
        int x2 = textPoint.getX2();
        int y2 = textPoint.getY2();

        int totalWidth = x2 - x1;
        int textLength = fullText.length();

        if (textLength == 0) {
            return null;
        }

        if (word.length() >= textLength - 2) {
            return new WordBoundingBox(x1, y1, x2, y2, word);
        }

        double charWidth = (double) totalWidth / textLength;
        int wordX1 = x1 + (int) (wordStartIndex * charWidth);
        int wordX2 = wordX1 + (int) (word.length() * charWidth);

        wordX1 = Math.max(x1, wordX1);
        wordX2 = Math.min(x2, wordX2);

        if (wordX2 - wordX1 < MIN_HIGHLIGHT_WIDTH) {
            int center = (wordX1 + wordX2) / 2;
            wordX1 = Math.max(x1, center - MIN_HIGHLIGHT_WIDTH / 2);
            wordX2 = Math.min(x2, center + MIN_HIGHLIGHT_WIDTH / 2);
        }

        return new WordBoundingBox(wordX1, y1, wordX2, y2, word);
    }

    /**
     * Convert window coordinates to screenshot coordinates using the same calculation as ClickOnImage
     * @return array [x1, y1, width, height] in screenshot coordinates with validation
     */
    private int[] convertWindowToScreenshotCoordinates(int ocrX1, int ocrY1, int ocrX2, int ocrY2,
                                                       int windowWidth, int windowHeight,
                                                       int screenshotWidth, int screenshotHeight) {
        if (windowWidth <= 0 || windowHeight <= 0 || screenshotWidth <= 0 || screenshotHeight <= 0) {
            logger.warn("Invalid dimensions for coordinate conversion");
            return new int[]{0, 0, 0, 0};
        }

        double x1Relative = (double) ocrX1 / windowWidth;
        double y1Relative = (double) ocrY1 / windowHeight;
        double x2Relative = (double) ocrX2 / windowWidth;
        double y2Relative = (double) ocrY2 / windowHeight;

        int x1 = (int) (x1Relative * screenshotWidth);
        int y1 = (int) (y1Relative * screenshotHeight);
        int x2 = (int) (x2Relative * screenshotWidth);
        int y2 = (int) (y2Relative * screenshotHeight);

        int width = Math.max(x2 - x1, MIN_HIGHLIGHT_WIDTH);
        int height = Math.max(y2 - y1, MIN_HIGHLIGHT_HEIGHT);

        x1 = Math.max(0, x1 - HIGHLIGHT_PADDING);
        y1 = Math.max(0, y1 - HIGHLIGHT_PADDING);
        width = width + (2 * HIGHLIGHT_PADDING);
        height = height + (2 * HIGHLIGHT_PADDING);

        x1 = Math.min(x1, screenshotWidth - 1);
        y1 = Math.min(y1, screenshotHeight - 1);
        width = Math.min(width, screenshotWidth - x1);
        height = Math.min(height, screenshotHeight - y1);

        return new int[]{x1, y1, width, height};
    }

    /**
     * Highlight misspelled words on the screenshot by drawing rounded rectangles with enhanced visibility
     */
    private File highlightMisspelledWords(File screenshotFile, List<WordBoundingBox> wordBoundingBoxes) {
        if (wordBoundingBoxes == null || wordBoundingBoxes.isEmpty()) {
            logger.warn("No word bounding boxes provided for highlighting");
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(screenshotFile);
            if (image == null) {
                logger.warn("Failed to read screenshot image");
                return null;
            }

            int screenshotWidth = image.getWidth();
            int screenshotHeight = image.getHeight();
            logger.info("Screenshot dimensions - Width: " + screenshotWidth + ", Height: " + screenshotHeight);

            long innerHeight = (Long) ((JavascriptExecutor) driver).executeScript("return window.innerHeight;");
            long innerWidth = (Long) ((JavascriptExecutor) driver).executeScript("return window.innerWidth;");
            int windowHeight = (int) innerHeight;
            int windowWidth = (int) innerWidth;
            logger.info("Window inner dimensions - Width: " + windowWidth + ", Height: " + windowHeight);

            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setStroke(new BasicStroke(BORDER_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (WordBoundingBox wordBox : wordBoundingBoxes) {
                int[] coords = convertWindowToScreenshotCoordinates(
                        wordBox.x1, wordBox.y1, wordBox.x2, wordBox.y2,
                        windowWidth, windowHeight, screenshotWidth, screenshotHeight);

                int x1 = coords[0];
                int y1 = coords[1];
                int width = coords[2];
                int height = coords[3];

                if (width <= 0 || height <= 0) {
                    logger.warn("Skipping invalid highlight dimensions for word: " + wordBox.word);
                    continue;
                }

                int arcWidth = Math.min(8, width / 4);
                int arcHeight = Math.min(8, height / 4);

                g2d.setColor(HIGHLIGHT_FILL_COLOR);
                g2d.fillRoundRect(x1, y1, width, height, arcWidth, arcHeight);

                g2d.setColor(HIGHLIGHT_BORDER_COLOR);
                g2d.drawRoundRect(x1, y1, width, height, arcWidth, arcHeight);

                logger.info("Drew highlight for word '" + wordBox.word + "' at: x=" + x1 + ", y=" + y1 + ", w=" + width + ", h=" + height);
            }

            g2d.dispose();

            File highlightedFile = new File(screenshotFile.getParent(), "highlighted_" + screenshotFile.getName());
            String formatName = screenshotFile.getName().toLowerCase().endsWith(".jpg") ||
                    screenshotFile.getName().toLowerCase().endsWith(".jpeg") ? "jpg" : "png";
            ImageIO.write(image, formatName, highlightedFile);
            logger.info("Highlighted screenshot saved to: " + highlightedFile.getAbsolutePath());

            return highlightedFile;
        } catch (Exception e) {
            logger.warn("Error highlighting misspelled words: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private boolean uploadFile(String s3SignedURL, String localPath) {
        logger.debug("s3SignedURL - " + s3SignedURL);
        logger.debug("localPath - " + localPath);
        boolean localUrlExists = new File(localPath).exists();
        if (localUrlExists) {
            logger.info(String.format("Uploading test asset to storage, presigned-URL:%s, localFilePath:%s", s3SignedURL, localPath));
            try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
                HttpPut httpPut = new HttpPut(s3SignedURL);

                File file = new File(localPath);
                FileEntity entity = new FileEntity(file);
                httpPut.setEntity(entity);
                HttpResponse response = httpclient.execute(httpPut);
                logger.info("Response from s3: " + response.getStatusLine().getStatusCode());
                logger.info("Upload completed");
                return true;
            } catch (Exception e) {
                logger.info("Exception while uploading custom screenshot to s3: " + ExceptionUtils.getStackTrace(e));
                return false;
            }
        } else {
            logger.info("Local path does not exist");
            return false;
        }
    }
}

