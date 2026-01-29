package com.testsigma.addons.restapi.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.testsigma.sdk.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class JSONUtilities {
    Logger logger;

    public JSONUtilities(Logger logger) {
        this.logger = logger;
    }

    /**
     * Preprocesses JSON string to remove problematic characters
     *
     * @param jsonString The raw JSON string
     * @param logger     Logger instance for logging
     * @return Cleaned JSON string
     */
    public String preprocessJsonString(String jsonString, Logger logger) {
        if (jsonString == null) {

            logger.warn("JSON string is null, returning null");

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


        logger.info("Preprocessed JSON string: removed problematic characters and normalized whitespace");


        return cleaned;
    }

    /**
     * Reads JSON data from a JSON string using JSON path
     *
     * @param jsonString The JSON string
     * @param jsonPath   The JSON path
     * @param logger     Logger instance for logging
     * @return Extracted JSON data as string
     * @throws PathNotFoundException If JSON path is invalid
     */
    public String readJsonData(String jsonString, String jsonPath, Logger logger) throws PathNotFoundException {

        logger.info("Reading JSON data with path: " + jsonPath);


        jsonString = preprocessJsonString(jsonString, logger);

        if (jsonString == null || jsonString.isEmpty()) {
            String errorMsg = "JSON string is null or empty";

            logger.warn(errorMsg);

            throw new IllegalArgumentException(errorMsg);
        }

        try {
            Object result = JsonPath.read(jsonString, jsonPath);
            String output = result.toString();

            logger.info("Successfully extracted data using JSON path: " + jsonPath);

            return output;
        } catch (PathNotFoundException e) {
            String errorMsg = "Invalid JSON Path: " + jsonPath + ". Path not found in JSON structure.";

            logger.warn(errorMsg);

            throw new PathNotFoundException(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Error reading JSON data with path " + jsonPath + ": " + e.getMessage();
            logger.warn(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Reads JSON data from a file (local or remote URL)
     *
     * @param filePath The file path or URL
     * @param logger   Logger instance for logging
     * @return JSON data as string
     * @throws IOException If file cannot be read
     */
    public String readJsonFromFile(String filePath, Logger logger) throws IOException {

        logger.info("Reading JSON from file: " + filePath);


        File jsonFileToUse = null;

        try {
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {

                logger.info("Downloading JSON file from URL: " + filePath);

                URL url = new URL(filePath);
                String fileName = Paths.get(url.getPath()).getFileName().toString();
                File tempFile = File.createTempFile("downloaded-", fileName);
                try (InputStream in = url.openStream();
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                jsonFileToUse = tempFile;

                logger.info("Successfully downloaded JSON file to temporary location");

            } else {
                jsonFileToUse = new File(filePath);
            }

            if (!jsonFileToUse.exists()) {
                String errorMsg = "File not found: " + filePath;

                logger.warn(errorMsg);

                throw new FileNotFoundException(errorMsg);
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonData;
            try (InputStream inputStream = new FileInputStream(jsonFileToUse)) {

                logger.info("Reading JSON file: " + jsonFileToUse.getAbsolutePath());

                jsonData = mapper.readTree(inputStream).toString();

                logger.info("Successfully read JSON data from file");

            }

            if (jsonData == null || jsonData.isEmpty()) {
                String errorMsg = "JSON data is empty or null in file: " + filePath;

                logger.warn(errorMsg);

                throw new IOException(errorMsg);
            }

            return jsonData;
        } catch (FileNotFoundException e) {
            String errorMsg = "File not found: " + filePath;

            logger.warn(errorMsg);

            throw e;
        } catch (IOException e) {
            String errorMsg = "Error reading JSON file: " + filePath + " - " + e.getMessage();

            logger.warn(errorMsg);

            throw new IOException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error processing JSON file: " + filePath + " - " + e.getMessage();

            logger.warn(errorMsg);
            throw new IOException(errorMsg, e);

        }

    }

    /**
     * Extracts values from JSON using the given path
     *
     * @param jsonString The JSON string
     * @param path       The JSON path
     * @param logger     Logger instance for logging
     * @return List of extracted values
     */
    public List<Object> extractValuesFromJsonPath(String jsonString, String path, Logger logger) {
        List<Object> values = new ArrayList<>();

        try {
            String preprocessedJson = preprocessJsonString(jsonString, logger);
            DocumentContext documentContext = JsonPath.using(Configuration.defaultConfiguration()).parse(preprocessedJson);
            List<Object> pathResults = documentContext.read(path);

            for (Object result : pathResults) {
                if (result != null) {
                    values.add(result);
                }
            }


            logger.info("Extracted " + values.size() + " values from JSON path: " + path);
            logger.info("Extracted values: " + values);


        } catch (Exception e) {
            String errorMsg = "Error extracting values from JSON path: " + path + " - " + e.getMessage();

            logger.warn(errorMsg);

        }

        return values;
    }

    /**
     * Verifies if the list is in the specified order
     *
     * @param values    List of values to check
     * @param orderType Type of order to verify (ascending, descending, alphabetical)
     * @param logger    Logger instance for logging
     * @return true if in order, false otherwise
     */
    public boolean verifyOrder(List<Object> values, String orderType, Logger logger) {
        if (values.size() <= 1) {

            logger.info("List has " + values.size() + " element(s), considered in order");

            return true; // Single or empty list is always in order
        }

        List<Object> sortedValues = new ArrayList<>(values);

        switch (orderType.toLowerCase()) {
            case "ascending":
                sortAscending(sortedValues);
                break;
            case "descending":
                sortDescending(sortedValues);
                break;
            case "alphabetical":
                sortAlphabetical(sortedValues);
                break;
            default:
                String errorMsg = "Invalid order type: " + orderType + ". Must be 'ascending', 'descending', or 'alphabetical'";

                logger.warn(errorMsg);

                return false;
        }

        // Compare original with sorted
        boolean isInOrder = values.equals(sortedValues);


        logger.info("Original values: " + values);
        logger.info("Sorted values: " + sortedValues);
        logger.info("Is in " + orderType + " order: " + isInOrder);


        return isInOrder;
    }

    /**
     * Sorts values in ascending order
     */
    private void sortAscending(List<Object> values) {
        Collections.sort(values, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Number && o2 instanceof Number) {
                    return Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue());
                } else if (o1 instanceof String && o2 instanceof String) {
                    return ((String) o1).compareTo((String) o2);
                } else {
                    return o1.toString().compareTo(o2.toString());
                }
            }
        });
    }

    /**
     * Sorts values in descending order
     */
    private void sortDescending(List<Object> values) {
        Collections.sort(values, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Number && o2 instanceof Number) {
                    return Double.compare(((Number) o2).doubleValue(), ((Number) o1).doubleValue());
                } else if (o1 instanceof String && o2 instanceof String) {
                    return ((String) o2).compareTo((String) o1);
                } else {
                    return o2.toString().compareTo(o1.toString());
                }
            }
        });
    }

    /**
     * Sorts values alphabetically (case-insensitive)
     */
    private void sortAlphabetical(List<Object> values) {
        Collections.sort(values, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            }
        });
    }

    /**
     * Analyzes nodes to find null or blank values
     *
     * @param node   The JSON node to analyze
     * @param path   The JSON path (for logging)
     * @param logger Logger instance for logging
     * @return List of node issues
     */
    public List<NodeIssue> analyzeNodesForNullOrBlank(JsonNode node, String path, Logger logger) {
        List<NodeIssue> issues = new ArrayList<>();

        if (node == null || node.isNull()) {

            logger.warn("Node is null for path: " + path);

            return issues;
        }

        // If the node is an array, analyze each element
        if (node.isArray()) {
            int index = 0;
            for (JsonNode arrayElement : node) {
                if (arrayElement != null && arrayElement.isObject()) {
                    List<FieldIssue> fieldIssues = findNullOrBlankFields(arrayElement, "", logger);
                    if (!fieldIssues.isEmpty()) {
                        issues.add(new NodeIssue(index, fieldIssues));
                    }
                }
                index++;
            }
        } else if (node.isObject()) {
            // If it's a single object, treat it as node 0
            List<FieldIssue> fieldIssues = findNullOrBlankFields(node, "", logger);
            if (!fieldIssues.isEmpty()) {
                issues.add(new NodeIssue(0, fieldIssues));
            }
        }


        logger.info("Found " + issues.size() + " node(s) with null or blank values for path: " + path);


        return issues;
    }

    /**
     * Recursively finds null or blank fields in a JsonNode
     */
    private List<FieldIssue> findNullOrBlankFields(JsonNode node, String parentPath, Logger logger) {
        List<FieldIssue> issues = new ArrayList<>();

        if (node == null || node.isNull()) {
            return issues;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();
                String currentPath = parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName;

                if (fieldValue == null || fieldValue.isNull()) {
                    issues.add(new FieldIssue(currentPath, "null"));
                } else if (fieldValue.isTextual() && (fieldValue.asText().trim().isEmpty())) {
                    issues.add(new FieldIssue(currentPath, "blank"));
                } else if (fieldValue.isObject()) {
                    // Recursively check nested objects
                    issues.addAll(findNullOrBlankFields(fieldValue, currentPath, logger));
                } else if (fieldValue.isArray()) {
                    // Check array elements
                    for (int i = 0; i < fieldValue.size(); i++) {
                        JsonNode arrayElement = fieldValue.get(i);
                        if (arrayElement == null || arrayElement.isNull()) {
                            issues.add(new FieldIssue(currentPath + "[" + i + "]", "null"));
                        } else if (arrayElement.isTextual() && arrayElement.asText().trim().isEmpty()) {
                            issues.add(new FieldIssue(currentPath + "[" + i + "]", "blank"));
                        } else if (arrayElement.isObject()) {
                            issues.addAll(findNullOrBlankFields(arrayElement, currentPath + "[" + i + "]", logger));
                        }
                    }
                }
            }
        }

        return issues;
    }

    /**
     * Formats the output as a readable string
     */
    public String formatOutput(List<NodeIssue> issues, Logger logger) {
        if (issues.isEmpty()) {
            logger.info("No nodes with null or blank values found");
            return "No nodes with null or blank values found.";
        }

        StringBuilder output = new StringBuilder();
        StringBuilder result = new StringBuilder();

        for (NodeIssue issue : issues) {
            // Display node index as 1-based for user-friendly output
            int displayIndex = issue.nodeIndex + 1;
            output.append("Node ").append(displayIndex).append(" has the following issues:\n");
            for (FieldIssue fieldIssue : issue.fieldIssues) {
                result.append(fieldIssue.fieldPath).append(",");
                output.append("  - Field '").append(fieldIssue.fieldPath)
                        .append("' has ").append(fieldIssue.issueType).append(" value\n");
            }
            output.append("\n");
            // remove trailing comma from result
            if (result.length() > 0 && result.charAt(result.length() - 1) == ',') {
                result.setLength(result.length() - 1);
            }
        }
        logger.info("Formatted output for " + issues.size() + " node issue(s)");
        logger.info(output.toString());

        return result.toString().trim();
    }

    /**
     * Inner class to represent a node issue
     */
    public static class NodeIssue {
        public int nodeIndex;
        public List<FieldIssue> fieldIssues;

        public NodeIssue(int nodeIndex, List<FieldIssue> fieldIssues) {
            this.nodeIndex = nodeIndex;
            this.fieldIssues = fieldIssues;
        }
    }

    /**
     * Inner class to represent a field issue
     */
    public static class FieldIssue {
        public String fieldPath;
        public String issueType; // "null" or "blank"

        public FieldIssue(String fieldPath, String issueType) {
            this.fieldPath = fieldPath;
            this.issueType = issueType;
        }
    }
}
