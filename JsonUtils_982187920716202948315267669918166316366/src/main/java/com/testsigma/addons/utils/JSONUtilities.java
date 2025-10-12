package com.testsigma.addons.utils;

import com.jayway.jsonpath.JsonPath;

public class JSONUtilities {

    public static String readJsonData(String jsonString, String jsonPath){
        jsonString = preprocessJsonString(jsonString);
        return JsonPath.read(jsonString, jsonPath).toString();
    }
    /**
     * Preprocesses JSON string to remove problematic characters
     * @param jsonString The raw JSON string
     * @return Cleaned JSON string
     */
    private static String preprocessJsonString(String jsonString) {
        if (jsonString == null) {
            return null;
        }

        // Remove non-breaking spaces (character code 160) and other problematic whitespace
        String cleaned = jsonString
                .replaceAll("\\u00A0", " ")  // Replace non-breaking spaces with regular spaces
                .replaceAll("\\u2007", " ")  // Replace figure spaces
                .replaceAll("\\u202F", " ")  // Replace narrow no-break spaces
                .replaceAll("\\u2060", "")   // Remove word joiners
                .replaceAll("\\uFEFF", "")   // Remove byte order marks
                .trim();                     // Remove leading/trailing whitespace

        // Normalize multiple consecutive spaces to single spaces
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned;
    }

}
