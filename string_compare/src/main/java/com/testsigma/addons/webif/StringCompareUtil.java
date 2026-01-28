package com.testsigma.addons.webif;

import lombok.Data;

@Data
public class StringCompareUtil {
    private String errorMessage;
    private String successMessage;

    public boolean performOperation(String string1, String string2, String operation){
        boolean equalsCheck = false;
        boolean containsCheck = false;
        this.errorMessage = "Not a valid operator("+operation+")";

        // Handle null values - treat null as empty string for comparison
        if (string1 == null) {
            string1 = "";
        }
        if (string2 == null) {
            string2 = "";
        }
        if (operation == null) {
            operation = "";
        }

        // Normalize whitespace (treat line breaks, spaces, tabs as equivalent)
        String normalizedStr1 = normalizeWhitespace(string1);
        String normalizedStr2 = normalizeWhitespace(string2);

        // Ensure normalized strings are not null (shouldn't happen, but safety check)
        if (normalizedStr1 == null) {
            normalizedStr1 = "";
        }
        if (normalizedStr2 == null) {
            normalizedStr2 = "";
        }

        switch (operation) {
            case "equals":
                if (normalizedStr1.equals(normalizedStr2)) {
                    equalsCheck = true;
                    this.successMessage = "Both the strings match: " + string1 + " == " + string2;
                } else {
                    this.errorMessage = "Strings do not match. Value1: " + string2 + ", Value2: " + string1;
                }
                break;
            case "equals ignore-case":
                if (normalizedStr1.equalsIgnoreCase(normalizedStr2)) {
                    equalsCheck = true;
                    this.successMessage = "Both the strings match (ignore case): " + string1 + " == " + string2;
                } else {
                    this.errorMessage = "Strings do not match (ignore case). Value1: " + string2 + ", Value2: " + string1;
                }
                break;
            case "contains":
                if (normalizedStr1.contains(normalizedStr2)) {
                    containsCheck = true;
                    this.successMessage =  string1 + " contains " + string2;
                } else {
                    this.errorMessage = "Value1 does not contain Value2. Value1: " + string1 + ", Value2: " + string2;
                }
                break;
            case "contains ignore-case":
                if (normalizedStr1.toLowerCase().contains(normalizedStr2.toLowerCase())) {
                    containsCheck = true;
                    this.successMessage =  string1 + " contains (ignore case) " + string2;
                } else {
                    this.errorMessage = "Value1 does not contain Value2 (ignore case). Value1: " + string1 + ", Value2: " + string2;
                }
                break;
        }
        if(equalsCheck || containsCheck){
            return true;
        } else {
            return false;
        }
    }

    // Normalizes whitespace to handle edge cases: line breaks, multiple spaces, spaces around punctuation, etc.
    private String normalizeWhitespace(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }

        // Replace all Unicode whitespace (spaces, tabs, newlines, etc.) with single space
        String normalized = str.replaceAll("\\s+", " ");

        // Remove spaces before punctuation/symbols (%, $, @, #, &, *, etc.)
        normalized = normalized.replaceAll("\\s+([%$@#&*])", "$1");

        // Remove spaces after opening brackets/parentheses/braces
        normalized = normalized.replaceAll("([\\(\\[\\{])\\s+", "$1");

        // Remove spaces before closing brackets/parentheses/braces
        normalized = normalized.replaceAll("\\s+([\\)\\]\\}])", "$1");

        // Normalize spaces around mathematical operators
        normalized = normalized.replaceAll("\\s+([+=\\<\\>])\\s+", "$1");

        // Remove spaces before periods, commas, colons, semicolons
        normalized = normalized.replaceAll("\\s+([.,:;])", "$1");

        // Clean up any remaining multiple spaces
        normalized = normalized.replaceAll("\\s+", " ");

        // Trim leading and trailing whitespace
        normalized = normalized.trim();

        return normalized;
    }
}
