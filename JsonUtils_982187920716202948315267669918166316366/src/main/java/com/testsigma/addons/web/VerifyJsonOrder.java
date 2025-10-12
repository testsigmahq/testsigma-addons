package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Action(actionText = "verify if the json string json-string with json path json-path is in order-type order",
        description = "Verifies if a JSON array is sorted in the specified order based on the given JSON path",
        applicationType = ApplicationType.WEB)
public class VerifyJsonOrder extends WebAction {

    @TestData(reference = "json-string")
    private com.testsigma.sdk.TestData jsonString;

    @TestData(reference = "json-path")
    private com.testsigma.sdk.TestData jsonPath;

    @TestData(reference = "order-type", allowedValues = {"ascending", "descending", "alphabetical"})
    private com.testsigma.sdk.TestData orderType;

    @Override
    public com.testsigma.sdk.Result execute() {
        try {
            String jsonStr = jsonString.getValue().toString();
            String path = jsonPath.getValue().toString();
            String order = orderType.getValue().toString().toLowerCase();

            logger.info("Verifying JSON order for path: " + path + " with order type: " + order);

            logger.info("Preprocessed JSON string for parsing");

            // Parse JSON string
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonStr);

            if (!jsonNode.isArray()) {
                setErrorMessage("JSON string must be an array for order verification");
                logger.info("JSON string is not an array");
                return com.testsigma.sdk.Result.FAILED;
            }

            // Extract values using JSON path
            List<Object> extractedValues = extractValuesFromJsonPath(jsonStr, path);

            if (extractedValues.isEmpty()) {
                setErrorMessage("No values found for the given JSON path: " + path);
                logger.info("No values found for JSON path: " + path);
                return com.testsigma.sdk.Result.FAILED;
            }

            // Verify order
            boolean isInOrder = verifyOrder(extractedValues, order);

            if (isInOrder) {
                setSuccessMessage("JSON array is in " + order + " order for path: " + path);
                logger.info("JSON array is in " + order + " order");
                return com.testsigma.sdk.Result.SUCCESS;
            } else {
                setErrorMessage("JSON array is NOT in " + order + " order for path: " + path);
                logger.info("JSON array is NOT in " + order + " order");
                return com.testsigma.sdk.Result.FAILED;
            }

        } catch (Exception e) {
            setErrorMessage("Error verifying JSON order: " + e.getMessage());
            logger.info("Error verifying JSON order: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
    }

    /**
     * Extracts values from JSON using the given path
     * @param jsonString The JSON string
     * @param path The JSON path
     * @return List of extracted values
     */
    private List<Object> extractValuesFromJsonPath(String jsonString, String path) {
        List<Object> values = new ArrayList<>();

        try {
            DocumentContext documentContext = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
            List<Object> pathResults = documentContext.read(path);

            for (Object result : pathResults) {
                if (result != null) {
                    values.add(result);
                }
            }

            logger.info("Extracted " + values.size() + " values from JSON path: " + path);
            logger.info("Extracted values: " + values);

        } catch (Exception e) {
            logger.info("Error extracting values from JSON path: " + e.getMessage());
        }

        return values;
    }

    /**
     * Verifies if the list is in the specified order
     * @param values List of values to check
     * @param orderType Type of order to verify
     * @return true if in order, false otherwise
     */
    private boolean verifyOrder(List<Object> values, String orderType) {
        if (values.size() <= 1) {
            return true; // Single or empty list is always in order
        }

        List<Object> sortedValues = new ArrayList<>(values);

        switch (orderType) {
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
                setErrorMessage("Invalid order type. Must be 'ascending', 'descending', or 'alphabetical'");
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
}

