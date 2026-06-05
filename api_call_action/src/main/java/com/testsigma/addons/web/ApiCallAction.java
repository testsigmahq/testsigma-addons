package com.testsigma.addons.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Data
@Action(actionText = "Send api-method request to api-url with headers api-headers and body api-request-body (enter none if not applicable) and store response in runtime variable variable-name",
        description = "Sends an HTTP GET/POST/PUT/DELETE request. Provide headers as a JSON object e.g. {\"Authorization\":\"Bearer token\"} or enter 'none' to skip headers. Provide body as a JSON string for POST/PUT or enter 'none' to skip body.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ApiCallAction extends WebAction {

  @TestData(reference = "api-method",
            allowedValues = {"GET", "POST", "PUT", "DELETE"})
  private com.testsigma.sdk.TestData apiMethod;

  @TestData(reference = "api-url")
  private com.testsigma.sdk.TestData apiUrl;

  @TestData(reference = "api-headers")
  private com.testsigma.sdk.TestData apiHeaders;

  @TestData(reference = "api-request-body")
  private com.testsigma.sdk.TestData apiRequestBody;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    String method = apiMethod.getValue().toString().trim().toUpperCase();
    String url = apiUrl.getValue().toString().trim();
    String rawHeaders = (apiHeaders.getValue() != null) ? apiHeaders.getValue().toString().trim() : "none";
    String headersJson = rawHeaders.equalsIgnoreCase("none") ? "" : rawHeaders;
    String rawBody = (apiRequestBody.getValue() != null) ? apiRequestBody.getValue().toString().trim() : "none";
    String body = rawBody.equalsIgnoreCase("none") ? "" : rawBody;

    logger.info("Sending " + method + " request to: " + url);
    logger.debug("Headers: " + headersJson);
    logger.debug("Body: " + body);

    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = buildRequest(method, url, headersJson, body);
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      String responseBody = response.body();
      int statusCode = response.statusCode();
      logger.info("Response status: " + statusCode);
      logger.debug("Response body: " + responseBody);
      System.out.println("Response body: " + responseBody);

      runTimeData.setKey(variableName.getValue().toString());
      runTimeData.setValue(responseBody);

      if (statusCode >= 200 && statusCode < 300) {
        setSuccessMessage(method + " " + url + " returned " + statusCode + ". Response stored in runtime variable.");
        return com.testsigma.sdk.Result.SUCCESS;
      } else {
        setErrorMessage(method + " " + url + " failed with status " + statusCode + ". Response: " + responseBody);
        return com.testsigma.sdk.Result.FAILED;
      }
    } catch (Exception e) {
      logger.warn(method + " request failed: " + e.getMessage());
      setErrorMessage(method + " request to " + url + " failed: " + e.getMessage());
      return com.testsigma.sdk.Result.FAILED;
    }
  }

  private HttpRequest buildRequest(String method, String url, String headersJson, String body) throws Exception {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json");

    if (!headersJson.isEmpty()) {
      Map<String, String> headers = MAPPER.readValue(headersJson, new TypeReference<Map<String, String>>() {});
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        builder.header(entry.getKey(), entry.getValue());
      }
    }

    switch (method) {
      case "POST":
        return builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
      case "PUT":
        return builder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();
      case "DELETE":
        return builder.DELETE().build();
      default:
        return builder.GET().build();
    }
  }
}
