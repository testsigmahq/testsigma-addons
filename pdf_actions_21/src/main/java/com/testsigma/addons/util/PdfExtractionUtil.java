package com.testsigma.addons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for PDF text extraction with occurrence-based and regex support.
 */
public final class PdfExtractionUtil {

    private PdfExtractionUtil() {
    }

    /**
     * Result of finding a pattern in text: start index (inclusive) and end index (exclusive).
     */
    public static final class MatchResult {
        private final int start;
        private final int end;

        public MatchResult(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getMatchLength() {
            return end - start;
        }
    }

    /**
     * Word with its start (inclusive) and end (exclusive) position in the original text.
     */
    public static final class WordWithPosition {
        private final int start;
        private final int end;
        private final String word;

        public WordWithPosition(int start, int end, String word) {
            this.start = start;
            this.end = end;
            this.word = word;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getWord() {
            return word;
        }
    }

    /**
     * Finds the K-th occurrence (1-based) of the pattern in content.
     *
     * @param content       full text to search in
     * @param pattern       literal text or regex pattern (when useRegex is true)
     * @param occurrence1Based which occurrence to find (1 = first, 2 = second, etc.)
     * @param useRegex      if true, pattern is treated as regex; else literal match
     * @return MatchResult with start (inclusive) and end (exclusive), or null if not found or occurrence out of range
     */
    public static MatchResult findOccurrence(String content, String pattern, int occurrence1Based, boolean useRegex) {
        if (content == null || pattern == null || occurrence1Based < 1) {
            return null;
        }
        if (useRegex) {
            try {
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(content);
                int count = 0;
                while (m.find()) {
                    count++;
                    if (count == occurrence1Based) {
                        return new MatchResult(m.start(), m.end());
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        } else {
            int fromIndex = 0;
            for (int i = 0; i < occurrence1Based; i++) {
                int idx = content.indexOf(pattern, fromIndex);
                if (idx < 0) {
                    return null;
                }
                if (i == occurrence1Based - 1) {
                    return new MatchResult(idx, idx + pattern.length());
                }
                fromIndex = idx + 1;
            }
            return null;
        }
    }

    /**
     * Counts how many times the pattern appears in content (literal or regex).
     */
    public static int countOccurrences(String content, String pattern, boolean useRegex) {
        if (content == null || pattern == null) {
            return 0;
        }
        if (useRegex) {
            try {
                Matcher m = Pattern.compile(pattern).matcher(content);
                int count = 0;
                while (m.find()) {
                    count++;
                }
                return count;
            } catch (Exception e) {
                return 0;
            }
        } else {
            int count = 0;
            int from = 0;
            while (true) {
                int idx = content.indexOf(pattern, from);
                if (idx < 0) break;
                count++;
                from = idx + 1;
            }
            return count;
        }
    }

    /**
     * Returns words with their start/end positions in the text (words = sequences of non-whitespace).
     */
    public static List<WordWithPosition> getWordsWithPositions(String text) {
        List<WordWithPosition> list = new ArrayList<>();
        if (text == null) return list;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\S+");
        java.util.regex.Matcher m = p.matcher(text);
        while (m.find()) {
            list.add(new WordWithPosition(m.start(), m.end(), m.group()));
        }
        return list;
    }

    /**
     * Extracts N characters after the given match position. Uses raw content (no space removal).
     */
    public static String extractCharsAfter(String content, MatchResult match, int numChars) {
        if (content == null || match == null || numChars < 0) return "";
        int from = match.getEnd();
        int to = Math.min(from + numChars, content.length());
        if (from >= content.length() || from >= to) return "";
        return content.substring(from, to);
    }

    /**
     * Extracts N characters before the given match position.
     */
    public static String extractCharsBefore(String content, MatchResult match, int numChars) {
        if (content == null || match == null || numChars < 0) return "";
        int to = match.getStart();
        int from = Math.max(0, to - numChars);
        if (to <= 0 || from >= to) return "";
        return content.substring(from, to);
    }

    /**
     * Extracts N words after the match (words that start at or after match end).
     */
    public static String extractWordsAfter(String content, MatchResult match, int numWords) {
        if (content == null || match == null || numWords <= 0) return "";
        List<WordWithPosition> words = getWordsWithPositions(content);
        int matchEnd = match.getEnd();
        int startWordIdx = -1;
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).getStart() >= matchEnd) {
                startWordIdx = i;
                break;
            }
        }
        if (startWordIdx < 0 || startWordIdx + numWords > words.size()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = startWordIdx; i < startWordIdx + numWords && i < words.size(); i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(words.get(i).getWord());
        }
        return sb.toString();
    }

    /**
     * Extracts N words before the match (words that end at or before match start).
     */
    public static String extractWordsBefore(String content, MatchResult match, int numWords) {
        if (content == null || match == null || numWords <= 0) return "";
        List<WordWithPosition> words = getWordsWithPositions(content);
        int matchStart = match.getStart();
        int endWordIdx = -1;
        for (int i = words.size() - 1; i >= 0; i--) {
            if (words.get(i).getEnd() <= matchStart) {
                endWordIdx = i;
                break;
            }
        }
        if (endWordIdx < 0 || endWordIdx - numWords + 1 < 0) {
            return "";
        }
        int startWordIdx = endWordIdx - numWords + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = startWordIdx; i <= endWordIdx; i++) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(words.get(i).getWord());
        }
        return sb.toString();
    }
}
