package com.testsigma.addons.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.testsigma.addons.restapi.utils.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "store node details with null or blank values from JSON text test-data1 using JSON path " +
        " test-data2 and store it in runtime variable variable_name",
        description = "Captures details of nodes (array elements) that have null or blank values, " +
                "including node index and field names",
        applicationType = ApplicationType.REST_API)
public class CaptureNodesWithNullOrBlankValues extends RestApiAction {

    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData jsonText;

    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData jsonPath;

    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable_name;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        try {
            Result result = Result.SUCCESS;
            String jsonString = jsonText.getValue().toString();
            logger.info("JSON Data: " + jsonString);

            String path = jsonPath != null && jsonPath.getValue() != null ? jsonPath.getValue().toString() : "$";
            logger.info("JSON Path: " + path);

            try {
                JSONUtilities jsonUtilities = new JSONUtilities(logger);
                // Preprocess JSON string
                jsonString = jsonUtilities.preprocessJsonString(jsonString, logger);

                // Parse JSON using Jackson
                ObjectMapper mapper = new ObjectMapper();

                // Get the target node using JSONPath
                Object pathResult = JsonPath.read(jsonString, path);

                // Convert to JsonNode
                JsonNode targetNode;
                String resultJson = mapper.writeValueAsString(pathResult);
                targetNode = mapper.readTree(resultJson);

                // Analyze nodes for null/blank values using utils
                List<JSONUtilities.NodeIssue> issues = jsonUtilities.analyzeNodesForNullOrBlank(targetNode, path, logger);

                // Format output using utils
                String output = jsonUtilities.formatOutput(issues, logger);
                
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(output);
                runTimeData.setKey(variable_name.getValue().toString());
                
                if (issues.isEmpty()) {
                    logger.info("No nodes with null or blank values found.");
                    setSuccessMessage("No nodes with null or blank values found. All nodes are valid.");
                } else {
                    int total = 0;
                    for (JSONUtilities.NodeIssue issue : issues) {
                        total += issue.fieldIssues.size();
                    }
                    setErrorMessage("Found <b>" + total + "</b> node(s) with null or blank values." +
                            " Stored details in runtime variable: <b>" +
                            variable_name.getValue().toString() + " = " + output + "</b>");
                    return Result.FAILED;
                }
                logger.info("Output: " + output);
                
            } catch (com.jayway.jsonpath.PathNotFoundException e) {
                String errorMsg = "Invalid JSON Path: " + path + ". Please verify the path.";
                setErrorMessage(errorMsg);
                logger.warn(errorMsg);
                return Result.FAILED;
            } catch (Exception e) {
                String errorMsg = "An unexpected error occurred while processing JSON: " + ExceptionUtils.getMessage(e);
                setErrorMessage(errorMsg);
                logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
                return Result.FAILED;
            }
            return result;
        } catch (NullPointerException e) {
            String errorMsg = "Null value encountered in test data or JSON path. Please verify all required fields are provided.";
            logger.warn(errorMsg);
            setErrorMessage(errorMsg);
            return Result.FAILED;
        } catch (Exception e) {
            String errorMsg = "An unexpected error occurred: " + ExceptionUtils.getMessage(e);
            setErrorMessage(errorMsg);
            logger.warn("Error details: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}
