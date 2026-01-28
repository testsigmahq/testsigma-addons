package com.testsigma.addons.windows;

import com.testsigma.addons.utils.JSONUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Verify if the json string json-string with json path json-path is in order-type order",
        description = "Verifies if a JSON array is sorted in the specified order based on the given JSON path",
        applicationType = ApplicationType.WINDOWS)
public class VerifyJsonOrder extends WindowsAction {

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

            JSONUtilities jsonUtilities = new JSONUtilities(logger);
            // Preprocess JSON string
            String preprocessedJson = jsonUtilities.preprocessJsonString(jsonStr, logger);
            logger.info("Preprocessed JSON string for parsing");

            // Parse JSON string
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(preprocessedJson);

            if (!jsonNode.isArray()) {
                String errorMsg = "JSON string must be an array for order verification";
                setErrorMessage(errorMsg);
                logger.warn(errorMsg);
                return com.testsigma.sdk.Result.FAILED;
            }

            // Extract values using JSON path via utils
            List<Object> extractedValues = jsonUtilities.extractValuesFromJsonPath(preprocessedJson, path, logger);

            if (extractedValues.isEmpty()) {
                String errorMsg = "No values found for the given JSON path: " + path;
                setErrorMessage(errorMsg);
                logger.warn(errorMsg);
                return com.testsigma.sdk.Result.FAILED;
            }

            // Verify order using utils
            boolean isInOrder = jsonUtilities.verifyOrder(extractedValues, order, logger);

            if (isInOrder) {
                setSuccessMessage("JSON array is in " + order + " order for path: " + path);
                logger.info("JSON array is in " + order + " order");
                return com.testsigma.sdk.Result.SUCCESS;
            } else {
                String errorMsg = "JSON array is NOT in " + order + " order for path: " + path;
                setErrorMessage(errorMsg);
                logger.warn(errorMsg);
                return com.testsigma.sdk.Result.FAILED;
            }

        } catch (Exception e) {
            String errorMsg = "Error verifying JSON order: " + ExceptionUtils.getMessage(e);
            setErrorMessage(errorMsg);
            logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}

