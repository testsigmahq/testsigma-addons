package com.testsigma.addons.utils;

import com.testsigma.sdk.Logger;

import java.io.File;
import java.util.regex.Pattern;

public class TextExtractionHelper {

    /**
     * Extracts text found between startWord and endWord in the given file or URL.
     * Supports local file paths and HTTP/HTTPS URLs. Strips HTML tags for .html/.htm files.
     * Boundary search is case-insensitive; returned text preserves original casing.
     * Returns null if either word is not found or an error occurs.
     */
    public static String extractTextBetweenWords(Logger logger, String filePathOrUrl, String startWord, String endWord) {
        try {
            String fileName = FileHelper.extractFileName(filePathOrUrl);
            File file = FileHelper.urlToFileConverter(logger, fileName, filePathOrUrl);

            if (!file.exists()) {
                logger.warn("File does not exist: " + filePathOrUrl);
                return null;
            }

            String content = FileHelper.readFileContent(file);

            if (FileHelper.isHtmlFile(fileName)) {
                logger.debug("HTML file detected, stripping tags before extraction");
                content = FileHelper.stripHtmlTags(content);
            }

            String lowerContent = content.toLowerCase();
            int startIndex = lowerContent.indexOf(startWord.toLowerCase());
            if (startIndex == -1) {
                logger.warn("Start word '" + startWord + "' not found in file: " + filePathOrUrl);
                return null;
            }

            int afterStart = startIndex + startWord.length();
            int endIndex = lowerContent.indexOf(endWord.toLowerCase(), afterStart);
            if (endIndex == -1) {
                logger.warn("End word '" + endWord + "' not found after start word in file: " + filePathOrUrl);
                return null;
            }

            String extracted = content.substring(afterStart, endIndex).trim();
            logger.info("Extracted text between '" + startWord + "' and '" + endWord + "': " + extracted);
            return extracted;

        } catch (Exception e) {
            logger.warn("Error extracting text between words from: " + filePathOrUrl+e);
            return null;
        }
    }

    /**
     * Splits the file content by the resolved delimiter and returns the token at the given position.
     * position=0 returns the first token (before any delimiter); position=N returns the token after
     * the Nth occurrence of the delimiter.
     * delimiterType accepts: "," | "." | "\t" | "tab" | " " | "space"
     */
    public static String extractWordAtDelimiterPosition(Logger logger, String filePathOrUrl, String delimiterType, int position) {
        try {
            String fileName = FileHelper.extractFileName(filePathOrUrl);
            File file = FileHelper.urlToFileConverter(logger, fileName, filePathOrUrl);

            if (!file.exists()) {
                logger.warn("File does not exist: " + filePathOrUrl);
                return null;
            }

            String content = FileHelper.readFileContent(file);
            logger.warn("File content read successfully. Length: " + content.length() + " characters");
            logger.info("content " + content);

            if (FileHelper.isHtmlFile(fileName)) {
                logger.debug("HTML file detected, stripping tags before extraction");
                content = FileHelper.stripHtmlTags(content);
            }

            String delimiter = resolveDelimiter(delimiterType);
            if (delimiter == null) {
                logger.warn("Unknown delimiter type: '" + delimiterType + "'. Accepted values: , . \\t tab space multi-space");
                return null;
            }

            // multi-space is a regex pattern — don't quote it
            String splitPattern = isRegexDelimiter(delimiterType) ? delimiter : Pattern.quote(delimiter);
            String[] tokens = content.split(splitPattern, -1);
            logger.info("Total tokens after splitting by '" + delimiterType + "': " + tokens.length);

            if (position < 0 || position >= tokens.length) {
                logger.warn("Position " + position + " is out of range. Total tokens: " + tokens.length);
                return null;
            }

            String result = tokens[position].trim();
            logger.info("Extracted word at position " + position + ": " + result);
            return result;

        } catch (Exception e) {
            logger.warn("Error extracting word at delimiter position from: " + filePathOrUrl + e);
            return null;
        }
    }

    public static String resolveDelimiter(String delimiterType) {
        if (delimiterType == null) return null;
        // Check for literal space before trimming, since trim() would erase it
        if (delimiterType.equals(" ")) return " ";
        switch (delimiterType.trim().toLowerCase()) {
            case "comma":
            case ",":               return ",";
            case "period":
            case ".":               return ".";
            case "\\t":
            case "tab":
            case "\t":              return "\t";
            case "space":           return " ";
            case "multi-space":
            case "multispace":      return "\\s{2,}";
            default:                return null;
        }
    }

    public static boolean isRegexDelimiter(String delimiterType) {
        if (delimiterType == null) return false;
        switch (delimiterType.trim().toLowerCase()) {
            case "multi-space":
            case "multispace":  return true;
            default:            return false;
        }
    }
}
