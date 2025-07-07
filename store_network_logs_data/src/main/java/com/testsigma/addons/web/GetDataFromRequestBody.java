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



import static com.testsigma.addons.web.utilities.ResponseDataUtilities.getRequestBody;

@Data
@Action(actionText = "get value of the request header from the attribute header_key  " +
        "and store the value in runtime variable variable_name",
        description = "Extracts header value from network request and stores it in runtime variable, please make " +
                "sure that the request type is POST or PUT.",
        applicationType = ApplicationType.WEB)
public class GetDataFromRequestBody extends WebAction {

    @TestData(reference = "url_value")
    private com.testsigma.sdk.TestData urlValue;

    @TestData(reference = "method_value")
    private com.testsigma.sdk.TestData methodValue;

    @TestData(reference = "header_key")
    private com.testsigma.sdk.TestData headerKey;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @TestCaseResult
    private com.testsigma.sdk.TestCaseResult testCaseResult;

    @Override
    public Result execute() {
        logger.info("Execution started for action: GetDataFromrequestBody");

        try {
            // Step 1: Robustly fetch the request body
            logger.info("Fetching request body for test case result ID: " + testCaseResult.getId());
            String requestBody;
            try {
                requestBody = getRequestBody(testCaseResult.getId(), logger);
            } catch (IllegalStateException e) {
                throw new Exception("Failed to retrieve request body. The API response from" +
                        " the preceding step might be missing or empty.", e);
            }

            if (requestBody == null || requestBody.trim().isEmpty()) {
                throw new Exception("Retrieved request body is empty. Cannot search for attributes.");
            }
            logger.info("request body successfully retrieved.");

            // Step 2: Parse the entire JSON into a generic object to handle any structure
            logger.info("Parsing request body JSON...");
            ObjectMapper mapper = new ObjectMapper();
            Object parsedJson;
            try {
                parsedJson = mapper.readValue(requestBody, Object.class);
            } catch (JsonProcessingException e) {
                logger.info("request body is not a valid JSON. Error: " + e.getMessage());
                throw new Exception("request body is not a valid JSON. Error: " + e.getMessage());
            }

            // Step 3: Get parameters
            String attributeKey = headerKey.getValue().toString();

            // Step 4: Find all occurrences of the attribute using recursion
            List<Object> foundValues = new ArrayList<>();
            findAttributeRecursively(parsedJson, attributeKey, foundValues);

            // Step 5: Validate the result and get the desired occurrence
            if (foundValues.isEmpty()) {
                throw new Exception("Attribute '" + attributeKey + "' was not found anywhere in the request body.");
            }

            // Step 6: Convert the found value to a String and store it
            Object rawValue = foundValues.get(0);
            String valueToStore = (rawValue == null) ? "null" : rawValue.toString();

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(valueToStore);

            logger.info("Successfully extracted header value and stored it in runtime variable: " + runTimeData.getValue());
            setSuccessMessage("Header value fetched and stored in runtime variable: " + runTimeData.getValue());

        } catch (Exception e) {
            logger.warn("Exception occurred during execution: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred while fetching data from request body: " + e.getMessage());
            return Result.FAILED;
        }
        return Result.SUCCESS;

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


