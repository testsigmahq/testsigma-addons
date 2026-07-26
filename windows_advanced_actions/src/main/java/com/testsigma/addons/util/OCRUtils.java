package com.testsigma.addons.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.testsigma.sdk.Logger;

/**
 * Utility class for OCR (Optical Character Recognition) operations
 * This is a simplified implementation that can be enhanced with actual OCR libraries
 * For now, it provides a basic structure that can be easily replaced with Tesseract or other OCR engines
 */
public class OCRUtils {

    /**
     * Finds the matching text in the list of text points with improved positioning logic
     */
    public OCRTextPoint findMatchingText(List<OCRTextPoint> textPoints, String targetText, Logger logger) {
        logger.info("Searching for text: '" + targetText + "'");

        // Create HashMap to store all exact word matches with their coordinates (multiple occurrences)
        Map<String, List<OCRTextPoint>> exactWordMatches = new HashMap<>();

        // First pass: collect all exact word matches in HashMap (storing all occurrences)
        for (OCRTextPoint textPoint : textPoints) {
            String text = textPoint.getText().trim();
            if (!text.isEmpty()) {
                String lowerText = text.toLowerCase();
                exactWordMatches.computeIfAbsent(lowerText, k -> new ArrayList<>()).add(textPoint);
                if (text.equals(targetText)) {
                    logger.info("Found exact match: " + textPoint.getText());
                    return textPoint;
                }
            }
        }

        // Second pass: try case-insensitive exact match
        for (OCRTextPoint textPoint : textPoints) {
            if (textPoint.getText().equalsIgnoreCase(targetText)) {
                logger.info("Found case-insensitive match: " + textPoint.getText());
                return textPoint;
            }
        }

        // Third pass: try contains match with improved positioning
        for (OCRTextPoint textPoint : textPoints) {
            if (textPoint.getText().toLowerCase().contains(targetText.toLowerCase())) {
                logger.info("Found contains match in sentence: " + textPoint.getText());
                logger.info("Text point coordinates: x1=" + textPoint.getX1() + ", x2=" + textPoint.getX2() +
                        ", y1=" + textPoint.getY1() + ", y2=" + textPoint.getY2());

                // Try to find the exact position within the sentence using HashMap approach
                OCRTextPoint positionedTextPoint = findExactPositionInSentence(textPoint, targetText, exactWordMatches, logger);
                if (positionedTextPoint != null) {
                    return positionedTextPoint;
                }

                // Fallback to get the location based on the position by approximation
                logger.info("Using sentence center as fallback");
                return textPoint;
            }
        }

        logger.info("No matching text found for: '" + targetText + "'");
        logger.info("Available text elements:");
        for (OCRTextPoint textPoint : textPoints) {
            logger.info("  - '" + textPoint.getText() + "'");
        }

        return null;
    }

    /**
     * Finds the exact position of target text within a sentence by analyzing individual words
     */
    private OCRTextPoint findExactPositionInSentence(OCRTextPoint sentencePoint, String targetText, Map<String, List<OCRTextPoint>> exactWordMatches, Logger logger) {
        logger.info("Attempting to find exact position for: '" + targetText + "' in sentence: '" + sentencePoint.getText() + "'");

        // Split target text into words
        String[] targetWords = targetText.toLowerCase().trim().split("\\s+");
        if (targetWords.length == 0) {
            return null;
        }

        // Find the first and last words of target text in the exact matches
        OCRTextPoint firstWordPoint = null;
        OCRTextPoint lastWordPoint = null;
        double lastWordX2 = Double.NEGATIVE_INFINITY; // Track the end position of the previous word

        for (String word : targetWords) {
            List<OCRTextPoint> wordPoints = exactWordMatches.get(word);
            if (wordPoints != null && !wordPoints.isEmpty()) {
                logger.info("Found " + wordPoints.size() + " occurrences of word '" + word + "'");
                
                // Search through all occurrences of this word to find one within sentence boundaries and in correct sequence
                for (OCRTextPoint wordPoint : wordPoints) {
                    // Check if this word point falls within the sentence boundaries
                    if (isPointWithinSentence(wordPoint, sentencePoint, logger)) {
                        // Check if this word comes after the previous word (sequential validation)
                        boolean isSequential = (firstWordPoint == null) || (wordPoint.getX1() > lastWordX2);
                        
                        if (isSequential) {
                            if (firstWordPoint == null) {
                                firstWordPoint = wordPoint;
                            }
                            lastWordPoint = wordPoint;
                            lastWordX2 = wordPoint.getX2(); // Update the end position for next word
                            logger.info("Found word '" + word + "' at coordinates: x1=" + wordPoint.getX1() + ", x2=" + wordPoint.getX2());
                            break; // Use the first valid occurrence within sentence boundaries and in correct sequence
                        } else {
                            logger.info("Word '" + word + "' found within sentence bounds but not in correct sequence");
                            logger.info("Word x1=" + wordPoint.getX1() + " should be > previous word x2=" + lastWordX2);
                        }
                    } else {
                        logger.info("Word '" + word + "' occurrence found but outside sentence boundaries");
                        logger.info("Word point coordinates: x1=" + wordPoint.getX1() + ", x2=" + wordPoint.getX2() +
                                ", y1=" + wordPoint.getY1() + ", y2=" + wordPoint.getY2());
                        logger.info("Sentence point coordinates: x1=" + sentencePoint.getX1() + ", x2=" + sentencePoint.getX2() +
                                ", y1=" + sentencePoint.getY1() + ", y2=" + sentencePoint.getY2());
                    }
                }
            } else {
                logger.info("Word '" + word + "' not found in exact matches");
            }
        }

        // If we found both first and last words, calculate the target position
        if (firstWordPoint != null && lastWordPoint != null) {
            double targetX1 = Math.min(firstWordPoint.getX1(), lastWordPoint.getX1());
            double targetX2 = Math.max(firstWordPoint.getX2(), lastWordPoint.getX2());
            double targetY1 = Math.min(firstWordPoint.getY1(), lastWordPoint.getY1());
            double targetY2 = Math.max(firstWordPoint.getY2(), lastWordPoint.getY2());
            logger.info("Both first and last words found. Calculated coordinates: x1=" + targetX1 + ", x2=" + targetX2 +
                    ", y1=" + targetY1 + ", y2=" + targetY2);

            // Create a new OCRTextPoint with the calculated coordinates
            OCRTextPoint targetPoint = new OCRTextPoint();
            targetPoint.setText(targetText);
            targetPoint.setX1(targetX1);
            targetPoint.setX2(targetX2);
            targetPoint.setY1(targetY1);
            targetPoint.setY2(targetY2);

            logger.info("Calculated target position: x1=" + targetX1 + ", x2=" + targetX2 + ", y1=" + targetY1 + ", y2=" + targetY2);
            logger.info("Target center: x=" + targetPoint.getCenterX() + ", y=" + targetPoint.getCenterY());

            return targetPoint;
        } else if (firstWordPoint != null) {
            // If only first word found, use it as the target
            logger.info("Using first word position as target");
            return firstWordPoint;
        } else if (lastWordPoint != null) {
            // If only last word found, use it as the target
            logger.info("Using last word position as target");
            return lastWordPoint;
        }

        logger.info("Could not find exact word positions within sentence boundaries");
        return null;
    }

    /**
     * Checks if a word point falls within the boundaries of a sentence point
     */
    private boolean isPointWithinSentence(OCRTextPoint wordPoint, OCRTextPoint sentencePoint, Logger logger) {
        // Check if word coordinates are within sentence boundaries with some tolerance
        double tolerance = 15.0; // Allow some tolerance for OCR variations
        // since in backend we are storing the center of x1,x2 i am ignoring the check for x1,x2
        boolean withinBounds = (wordPoint.getY1() >= sentencePoint.getY1() - tolerance) &&
                (wordPoint.getY2() <= sentencePoint.getY2() + tolerance);
        logger.info("Word point within sentence bounds: " + withinBounds +
                " (word: x1=" + wordPoint.getX1() + ", x2=" + wordPoint.getX2() +
                ", sentence: x1=" + sentencePoint.getX1() + ", x2=" + sentencePoint.getX2() + ")");
        return withinBounds;
    }

    /**
     * Finds matching text for a sentence by breaking it into words and checking positioning constraints.
     * This method searches for sentences by validating that consecutive words are positioned correctly
     * relative to each other (next word's x1 should be less than previous word's x2 + 20, 
     * and y1 difference should be less than 10).
     * 
     * @param textPoints List of OCR text points containing individual words
     * @param targetSentence The sentence to search for
     * @param logger The logger instance
     * @return OCRTextPoint representing the found sentence, or null if not found
     */
    public OCRTextPoint findMatchingTextForSentence(List<OCRTextPoint> textPoints, String targetSentence, Logger logger) {
        logger.info("Searching for sentence: '" + targetSentence + "'");
        
        if (targetSentence == null || targetSentence.trim().isEmpty()) {
            logger.info("Target sentence is null or empty");
            return null;
        }
        
        // Split the target sentence into individual words
        String[] targetWords = targetSentence.trim().split("\\s+");
        if (targetWords.length == 0) {
            logger.info("No words found in target sentence");
            return null;
        }
        
        logger.info("Target sentence has " + targetWords.length + " words: "
                + String.join(", ", targetWords));
        
        // Filter text points to only include single words (exclude multi-word entries)
        List<OCRTextPoint> singleWordPoints = new ArrayList<>();
        for (OCRTextPoint point : textPoints) {
            String text = point.getText().trim();
            // Consider it a single word if it doesn't contain spaces and is not empty
            if (!text.isEmpty() && !text.contains(" ") && !text.contains("\n")) {
                singleWordPoints.add(point);
            }
        }
        
        logger.info("Filtered to " + singleWordPoints.size() + " single word text points");
        
        // Search for the sentence by finding consecutive words
        for (int i = 0; i < singleWordPoints.size(); i++) {
            OCRTextPoint firstWordPoint = singleWordPoints.get(i);
            String firstWord = firstWordPoint.getText().trim();
            
            // Check if this point matches the first word of our target sentence
            if (firstWord.equalsIgnoreCase(targetWords[0])) {
                logger.info("Found potential first word '" + firstWord + "' at index " + i + 
                           " with coordinates: x1=" + firstWordPoint.getX1() + ", x2=" + firstWordPoint.getX2() + 
                           ", y1=" + firstWordPoint.getY1() + ", y2=" + firstWordPoint.getY2());
                
                // Try to find the complete sentence starting from this word
                OCRTextPoint sentenceResult = findCompleteSentence(singleWordPoints, i, targetWords, logger);
                if (sentenceResult != null) {
                    logger.info("Successfully found complete sentence: '" + targetSentence + "'");
                    return sentenceResult;
                }
            }
        }
        
        logger.info("Sentence '" + targetSentence + "' not found in text points");
        
        // Fallback: Try breaking the sentence based on camelCase
        logger.info("Attempting fallback: breaking sentence based on camelCase");
        return findMatchingTextForSentenceWithCamelCaseFallback(textPoints, targetSentence, logger);
    }
    
    /**
     * Attempts to find a complete sentence starting from a given index in the text points.
     * Validates that consecutive words meet the positioning constraints.
     * 
     * @param textPoints List of single word text points
     * @param startIndex Index to start searching from
     * @param targetWords Array of words that make up the target sentence
     * @param logger The logger instance
     * @return OCRTextPoint representing the complete sentence, or null if not found
     */
    private OCRTextPoint findCompleteSentence(List<OCRTextPoint> textPoints, int startIndex, String[] targetWords, Logger logger) {
        if (targetWords.length == 1) {
            // Single word sentence - return the first word point
            return textPoints.get(startIndex);
        }
        
        List<OCRTextPoint> foundWords = new ArrayList<>();
        foundWords.add(textPoints.get(startIndex));
        
        int currentIndex = startIndex;
        
        // Search for each subsequent word
        for (int wordIndex = 1; wordIndex < targetWords.length; wordIndex++) {
            String targetWord = targetWords[wordIndex];
            OCRTextPoint previousWord = foundWords.get(foundWords.size() - 1);
            
            logger.info("Searching for word " + (wordIndex + 1) + "/" + targetWords.length + 
                       ": '" + targetWord + "' after word '" + previousWord.getText() + "'");
            
            // Look for the next word starting from the current position
            boolean foundNextWord = false;
            for (int j = currentIndex + 1; j < textPoints.size(); j++) {
                OCRTextPoint candidatePoint = textPoints.get(j);
                String candidateText = candidatePoint.getText().trim();
                
                if (candidateText.equalsIgnoreCase(targetWord)) {
                    // Check positioning constraints
                    if (isWordPositionValid(previousWord, candidatePoint, logger)) {
                        logger.info("Found valid word '" + targetWord + "' at index " + j + 
                                   " with coordinates: x1=" + candidatePoint.getX1() + ", x2=" + candidatePoint.getX2() + 
                                   ", y1=" + candidatePoint.getY1() + ", y2=" + candidatePoint.getY2());
                        
                        foundWords.add(candidatePoint);
                        currentIndex = j;
                        foundNextWord = true;
                        break;
                    } else {
                        logger.info("Word '" + targetWord + "' found at index " + j + 
                                   " but positioning constraints not met");
                    }
                }
            }
            
            if (!foundNextWord) {
                logger.info("Could not find valid word '" + targetWord + "' after word '" + previousWord.getText() + "'");
                return null;
            }
        }
        
        // All words found - create a combined OCRTextPoint for the sentence
        return createSentenceTextPoint(foundWords, targetWords, logger);
    }
    
    /**
     * Validates that a word's position meets the constraints relative to the previous word.
     * Constraints: next word's x1 should be less than previous word's x2 + 20,
     * and y1 difference should be less than 10.
     * 
     * @param previousWord The previous word's text point
     * @param currentWord The current word's text point
     * @param logger The logger instance
     * @return true if positioning constraints are met, false otherwise
     */
    private boolean isWordPositionValid(OCRTextPoint previousWord, OCRTextPoint currentWord, Logger logger) {
        // Constraint 1: next word's x1 should be less than previous word's x2 + 20
        boolean xConstraint = currentWord.getX1() <= (previousWord.getX2() + 20);
        
        // Constraint 2: y1 difference should be less than 10
        boolean yConstraint = Math.abs(currentWord.getY1() - previousWord.getY1()) < 10;
        
        logger.info("Position validation for words '" + previousWord.getText() + "' -> '" + currentWord.getText() + "':");
        logger.info("  X constraint: " + xConstraint + " (current x1=" + currentWord.getX1() + 
                   " <= previous x2+20=" + (previousWord.getX2() + 20) + ")");
        logger.info("  Y constraint: " + yConstraint + " (y1 diff=" + Math.abs(currentWord.getY1() - previousWord.getY1()) + " < 10)");
        
        return xConstraint && yConstraint;
    }
    
    /**
     * Creates a combined OCRTextPoint representing the complete sentence from individual word points.
     * 
     * @param wordPoints List of individual word text points
     * @param targetWords Array of target words
     * @param logger The logger instance
     * @return Combined OCRTextPoint representing the sentence
     */
    private OCRTextPoint createSentenceTextPoint(List<OCRTextPoint> wordPoints, String[] targetWords, Logger logger) {
        if (wordPoints.isEmpty()) {
            return null;
        }
        
        // Calculate bounding box for the entire sentence
        double minX1 = wordPoints.get(0).getX1();
        double maxX2 = wordPoints.get(0).getX2();
        double minY1 = wordPoints.get(0).getY1();
        double maxY2 = wordPoints.get(0).getY2();
        
        for (OCRTextPoint point : wordPoints) {
            minX1 = Math.min(minX1, point.getX1());
            maxX2 = Math.max(maxX2, point.getX2());
            minY1 = Math.min(minY1, point.getY1());
            maxY2 = Math.max(maxY2, point.getY2());
        }
        
        // Create the combined text point
        OCRTextPoint sentencePoint = new OCRTextPoint();
        sentencePoint.setText(String.join(" ", targetWords));
        sentencePoint.setX1(minX1);
        sentencePoint.setX2(maxX2);
        sentencePoint.setY1(minY1);
        sentencePoint.setY2(maxY2);
        
        logger.info("Created sentence text point: '" + sentencePoint.getText() + 
                   "' with coordinates: x1=" + minX1 + ", x2=" + maxX2 + 
                   ", y1=" + minY1 + ", y2=" + maxY2);
        logger.info("Sentence center: x=" + sentencePoint.getCenterX() + ", y=" + sentencePoint.getCenterY());
        
        return sentencePoint;
    }

    /**
     * Fallback method that breaks the target sentence based on camelCase and searches again.
     * This handles cases where the sentence might be written as one word but should be treated as multiple words.
     * 
     * @param textPoints List of OCR text points containing individual words
     * @param targetSentence The sentence to search for
     * @param logger The logger instance
     * @return OCRTextPoint representing the found sentence, or null if not found
     */
    private OCRTextPoint findMatchingTextForSentenceWithCamelCaseFallback(List<OCRTextPoint> textPoints, String targetSentence, Logger logger) {
        logger.info("Fallback: Breaking sentence '" + targetSentence + "' based on camelCase");
        
        // Break the sentence into words based on camelCase
        String[] camelCaseWords = breakCamelCase(targetSentence, logger);
        
        if (camelCaseWords.length <= 1) {
            logger.info("No camelCase breaking possible for sentence: '" + targetSentence + "'");
            return null;
        }
        
        logger.info("CamelCase broken into " + camelCaseWords.length + " words: " + String.join(", ", camelCaseWords));
        
        // Filter text points to only include single words (exclude multi-word entries)
        List<OCRTextPoint> singleWordPoints = new ArrayList<>();
        for (OCRTextPoint point : textPoints) {
            String text = point.getText().trim();
            // Consider it a single word if it doesn't contain spaces and is not empty
            if (!text.isEmpty() && !text.contains(" ") && !text.contains("\n")) {
                singleWordPoints.add(point);
            }
        }
        
        logger.info("Filtered to " + singleWordPoints.size() + " single word text points for camelCase search");
        
        // Search for the sentence by finding consecutive words using camelCase broken words
        for (int i = 0; i < singleWordPoints.size(); i++) {
            OCRTextPoint firstWordPoint = singleWordPoints.get(i);
            String firstWord = firstWordPoint.getText().trim();
            
            // Check if this point matches the first word of our camelCase broken sentence
            if (firstWord.equalsIgnoreCase(camelCaseWords[0])) {
                logger.info("Found potential first camelCase word '" + firstWord + "' at index " + i + 
                           " with coordinates: x1=" + firstWordPoint.getX1() + ", x2=" + firstWordPoint.getX2() + 
                           ", y1=" + firstWordPoint.getY1() + ", y2=" + firstWordPoint.getY2());
                
                // Try to find the complete sentence starting from this word using camelCase words
                OCRTextPoint sentenceResult = findCompleteSentence(singleWordPoints, i, camelCaseWords, logger);
                if (sentenceResult != null) {
                    logger.info("Successfully found complete sentence using camelCase fallback: '" + targetSentence + "'");
                    // Update the text to match the original target sentence
                    sentenceResult.setText(targetSentence);
                    return sentenceResult;
                }
            }
        }
        
        logger.info("Sentence '" + targetSentence + "' not found even with camelCase fallback");
        return null;
    }
    
    /**
     * Breaks a string into words based on camelCase patterns.
     * For example: "SystemData" -> ["System", "Data"]
     * 
     * @param text The text to break into camelCase words
     * @param logger The logger instance
     * @return Array of words broken from camelCase
     */
    private String[] breakCamelCase(String text, Logger logger) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0];
        }
        
        // First, try to break on existing spaces
        String[] spaceWords = text.trim().split("\\s+");
        if (spaceWords.length > 1) {
            // If there are already spaces, return as is
            return spaceWords;
        }
        
        // If it's a single word, try to break on camelCase
        String singleWord = spaceWords[0];
        List<String> words = new ArrayList<>();
        
        if (singleWord.length() <= 1) {
            return new String[]{singleWord};
        }
        
        StringBuilder currentWord = new StringBuilder();
        currentWord.append(singleWord.charAt(0));
        
        for (int i = 1; i < singleWord.length(); i++) {
            char currentChar = singleWord.charAt(i);
            char previousChar = singleWord.charAt(i - 1);
            
            // Check if this is a camelCase boundary
            // Boundary conditions:
            // 1. Current char is uppercase and previous char is lowercase
            // 2. Current char is uppercase and previous char is uppercase but next char is lowercase (if exists)
            // 3. Current char is digit and previous char is letter
            // 4. Current char is letter and previous char is digit
            
            boolean isBoundary = false;
            
            if (Character.isUpperCase(currentChar) && Character.isLowerCase(previousChar)) {
                // camelCase boundary: lowercase followed by uppercase
                isBoundary = true;
            } else if (Character.isUpperCase(currentChar) && Character.isUpperCase(previousChar) && 
                      i + 1 < singleWord.length() && Character.isLowerCase(singleWord.charAt(i + 1))) {
                // Acronym boundary: uppercase followed by uppercase followed by lowercase
                isBoundary = true;
            } else if (Character.isDigit(currentChar) && Character.isLetter(previousChar)) {
                // Letter-digit boundary
                isBoundary = true;
            } else if (Character.isLetter(currentChar) && Character.isDigit(previousChar)) {
                // Digit-letter boundary
                isBoundary = true;
            }
            
            if (isBoundary) {
                // Save current word and start new one
                if (currentWord.length() > 0) {
                    words.add(currentWord.toString());
                    currentWord = new StringBuilder();
                }
            }
            
            currentWord.append(currentChar);
        }
        
        // Add the last word
        if (currentWord.length() > 0) {
            words.add(currentWord.toString());
        }
        
        String[] result = words.toArray(new String[0]);
        logger.info("CamelCase breaking result for '" + text + "': " + String.join(", ", result));
        
        return result;
    }

    /**
     * Extracts text points from a screenshot file by calling the OCR API.
     */
    public static List<OCRTextPoint> extractTextPoints(File screenshotFile, Logger logger) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("ocrImageFile", screenshotFile.getName(),
                            RequestBody.create(screenshotFile, MediaType.parse("image/png")))
                    .build();

            Request request = new Request.Builder()
                    .url(Constants.VISUAL_SERVER_OCR_TEXT_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                    .build();

            logger.info("Making OCR API call");
            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                logger.info("OCR Response body: " + responseBody);

                OCRResponse ocrResponse = mapper.readValue(responseBody, OCRResponse.class);

                if (ocrResponse.hasError()) {
                    throw new RuntimeException("OCR API returned error: " + ocrResponse.getError());
                }
                if (!ocrResponse.hasText()) {
                    throw new RuntimeException("No text found in the image");
                }
                return ocrResponse.getText();
            } else {
                throw new RuntimeException("OCR API call failed with status: " + (response.code()));
            }
        } catch (IOException e) {
            logger.info("Exception during OCR API call: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Error during OCR API call: " + e.getMessage());
        }
    }

    /**
     * Searches for the target text in the list of OCR text points using
     * exact match, case-insensitive match, contains match, and full-text concatenation.
     */
    public static boolean searchForText(List<OCRTextPoint> textPoints, String targetText, Logger logger) {
        for (OCRTextPoint tp : textPoints) {
            if (tp.getText().equals(targetText)) return true;
        }
        for (OCRTextPoint tp : textPoints) {
            if (tp.getText().equalsIgnoreCase(targetText)) return true;
        }
        for (OCRTextPoint tp : textPoints) {
            if (tp.getText().toLowerCase().contains(targetText.toLowerCase())) return true;
        }
        StringBuilder sb = new StringBuilder();
        for (OCRTextPoint tp : textPoints) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(tp.getText());
        }
        return sb.toString().toLowerCase().contains(targetText.toLowerCase());
    }

}
