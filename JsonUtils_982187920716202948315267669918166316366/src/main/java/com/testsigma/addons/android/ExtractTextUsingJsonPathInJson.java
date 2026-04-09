package com.testsigma.addons.android;

import com.jayway.jsonpath.PathNotFoundException;
import com.testsigma.addons.utils.JSONUtilities;
import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(
        actionText = "Extract text from a JSON text test-data1 using the specified JSON path test-data2 and store it in a runtime variable variable_name",
        description = "Extract required json data using JSON string and JSON path, store data into a runtime variable",
        applicationType = ApplicationType.ANDROID
)
public class ExtractTextUsingJsonPathInJson extends AndroidAction {

    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData jsonText;

    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData jsonpath;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable_name;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {

        try {
            String jsonString = jsonText.getValue().toString();
            String jsonPath = jsonpath.getValue().toString();

            logger.info("JSON Data: " + jsonString);
            logger.info("JSON Path: " + jsonPath);

            JSONUtilities jsonUtilities = new JSONUtilities(logger);
            String output;

            try {
                output = jsonUtilities.readJsonData(jsonString, jsonPath, logger);
            } catch (PathNotFoundException e) {
                String errorMsg = "Invalid JSON Path: " + jsonPath + ". Please verify the path.";
                logger.warn(errorMsg);
                setErrorMessage(errorMsg);
                return Result.FAILED;
            }

            // Extra safety (optional but good)
            if (output == null || output.trim().isEmpty()) {
                output = "null";
            }

            // Store runtime variable
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variable_name.getValue().toString());
            runTimeData.setValue(output);

            logger.info("Extracted Output: " + output);

            setSuccessMessage(
                    "Stored the desired data into runtime variable. " +
                            variable_name.getValue().toString() + " = " + output
            );

            return Result.SUCCESS;

        } catch (NullPointerException e) {
            String errorMsg = "Null value encountered in test data or JSON path. Please verify inputs.";
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
            return Result.FAILED;

        } catch (Exception e) {
            String errorMsg = "An unexpected error occurred: " + ExceptionUtils.getMessage(e);
            logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(errorMsg);
            return Result.FAILED;
        }
    }
}