package com.testsigma.addons.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestCaseResult;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.testsigma.addons.web.utilities.ResponseDataUtilities.getResponseBody;


@Data
@Action(
        actionText = "Get value of attribute attribute_key from response at occurrence index and store in variable_name",
        description = "Fetches a value from a JSON response by its key, even if nested.",
        applicationType = ApplicationType.WEB
)
public class GetDataFromResponseBody extends WebAction {

    @TestData(reference = "attribute_key")
    private com.testsigma.sdk.TestData attributeValue;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @TestData(reference = "index")
    private com.testsigma.sdk.TestData occurrence;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {
        logger.info("Execution started for action: GetDataFromResponseBody");

        try {
            // Step 1: Robustly fetch the response body
            logger.info("Fetching response body for test case result ID: " + testCaseResult.getId());
            String responseBody;
            try {
                responseBody = getResponseBody(testCaseResult.getId(), logger);
                logger.info("Response body: "+ responseBody);
            } catch (IllegalStateException e) {
                throw new Exception("Failed to retrieve response body. The API response from the preceding step might be missing or empty.", e);
            }

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new Exception("Retrieved response body is empty. Cannot search for attributes.");
            }
            logger.info("Response body successfully retrieved.");

            // Step 2: Parse the entire JSON into a generic object to handle any structure
            logger.info("Parsing response body JSON...");
            ObjectMapper mapper = new ObjectMapper();
            Object parsedJson;
            try {
                parsedJson = mapper.readValue(responseBody, Object.class);
            } catch (JsonProcessingException e) {
                logger.warn("Response body is not valid JSON, falling back to raw body. Error: " + e.getMessage());
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(responseBody);
                String fallbackMessage = String.format(
                        "Response body is not valid JSON; stored the raw response body in runtime variable '%s'.",
                        variableName.getValue());
                setSuccessMessage(fallbackMessage);
                logger.info(fallbackMessage);
                return Result.SUCCESS;
            }

            // Step 3: Get parameters
            String attributeKey = attributeValue.getValue().toString();
            int occurrenceIndex;
            try {
                occurrenceIndex = Integer.parseInt(occurrence.getValue().toString());
                if (occurrenceIndex < 1) {
                    throw new IllegalArgumentException("Occurrence index must be a positive integer (1 or greater).");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid occurrence index provided. It must be an integer.");
            }
            logger.info("Searching for the " + occurrenceIndex + "th occurrence of attribute '" + attributeKey + "' in the response body.");

            // Step 4: Find all occurrences of the attribute using recursion
            List<Object> foundValues = new ArrayList<>();
            findAttributeRecursively(parsedJson, attributeKey, foundValues);

            // Step 5: Validate the result and get the desired occurrence
            if (foundValues.isEmpty()) {
                throw new Exception("Attribute '" + attributeKey + "' was not found anywhere in the response body.");
            }
            if (occurrenceIndex > foundValues.size()) {
                throw new Exception(String.format("Could not find occurrence #%d of attribute '%s'. Only %d occurrence(s) were found.",
                        occurrenceIndex, attributeKey, foundValues.size()));
            }

            // Step 6: Convert the found value to a String and store it
            Object rawValue = foundValues.get(occurrenceIndex - 1);
            String valueToStore = (rawValue == null) ? "null" : rawValue.toString();

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(valueToStore);

            String successMessage = String.format("Successfully found value '%s' for attribute '%s' (occurrence #%d) and stored it in runtime variable '%s'.",
                    valueToStore, attributeKey, occurrenceIndex, variableName.getValue());
            setSuccessMessage(successMessage);
            logger.info(successMessage);
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception occurred during execution: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while fetching data from response body: " + e.getMessage());
            return Result.FAILED;
        }
    }

    /**
     * Recursively traverses any JSON structure (Map or List) and collects all values for a given key.
     */
    private void findAttributeRecursively(Object obj, String key, List<Object> found) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Map) {
            // It's a JSON object, check its keys
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (key.equals(entry.getKey())) {
                    found.add(entry.getValue());
                }
                // Continue searching in the value, which could be another Map or List
                findAttributeRecursively(entry.getValue(), key, found);
            }
        } else if (obj instanceof List) {
            // It's a JSON array, iterate through its items
            List<?> list = (List<?>) obj;
            for (Object item : list) {
                findAttributeRecursively(item, key, found);
            }
        }
    }
}