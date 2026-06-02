package com.testsigma.addons.ios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestMachineResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.devtools.v135.io.IO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Data
@Action(actionText = "Store environment value using Testsigma API key api-key in runtime variable variable-name",
        description = "Stores environment value in runtime variable",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class StoreEnvironmentValue extends IOSAction {

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;
  
  @TestData(reference = "token")
  private com.testsigma.sdk.TestData bearerToken;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @TestMachineResult
  private com.testsigma.sdk.TestMachineResult testMachineResult;

  @Override
  public Result execute() throws NoSuchElementException {
    Result result = Result.SUCCESS;
    logger.info("Initiating execution");
    try {
      Long testCaseId = null;
      if (testMachineResult != null && testMachineResult.getSuiteResults() != null && !testMachineResult.getSuiteResults().isEmpty()) {
        com.testsigma.sdk.SuiteResult suiteResult = testMachineResult.getSuiteResults().get(0);
        if (suiteResult.getCaseResults() != null && !suiteResult.getCaseResults().isEmpty()) {
          com.testsigma.sdk.TestCaseResult caseResult = suiteResult.getCaseResults().get(0);
          testCaseId = caseResult.getTestCaseId();
          logger.info("Extracted testCaseId: " + testCaseId);
        }
      }

      if (testCaseId != null) {
        String url = "https://app.testsigma.com/api/v1/test_case_results?query=testCaseId%3A" + testCaseId + "&sort=createdDate%2Cdesc&page=0&size=1";
        logger.info("Calling API: " + url);
        
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + bearerToken.getValue())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
          ObjectMapper mapper = new ObjectMapper();
          JsonNode rootNode = mapper.readTree(response.body());
          JsonNode contentNode = rootNode.path("content");
          
          if (contentNode.isArray() && contentNode.size() > 0) {
            JsonNode envNode = contentNode.get(0)
                    .path("environmentResult")
                    .path("executionResult")
                    .path("environment");
            
            String envName = envNode.path("name").asText();
            logger.info("Extracted environment name: " + envName);
            
            runTimeData.setValue(envName);
            runTimeData.setKey(variableName.getValue().toString());
            setSuccessMessage("Successfully stored environment name: '" + envName + "' in runtime variable: " + variableName.getValue());
          } else {
            logger.warn("No content found in API response");
            setErrorMessage("No content found in API response");
            result = Result.FAILED;
          }
        } else {
          logger.warn("API call failed with status code: " + response.statusCode());
          setErrorMessage("API call failed with status code: " + response.statusCode());
          result = Result.FAILED;
        }
      } else {
        logger.warn("testCaseId not found");
        setErrorMessage("testCaseId not found");
        result = Result.FAILED;
      }
    } catch (Exception e) {
      logger.warn("Error Occurred: " + ExceptionUtils.getStackTrace(e));
      setErrorMessage("Error Occured: " + ExceptionUtils.getMessage(e));
      result = Result.FAILED;
    }
    return result;
  }
}