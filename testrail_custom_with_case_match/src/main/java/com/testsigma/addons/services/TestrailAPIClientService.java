package com.testsigma.addons.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.testsigma.addons.constants.URLConstants;
import com.testsigma.addons.http.HttpClient;
import com.testsigma.addons.http.HttpResponse;
import com.testsigma.sdk.runners.CICDCredentials;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class TestrailAPIClientService {

  public String getProjectName(CICDCredentials cicdCredentials, String testrailRunId) {
    String testRailUrl = fetchProjectIdFromRunIdUrl(cicdCredentials.getUrl(), testrailRunId);
    String encodedAuth = getEncodedAuth(cicdCredentials);
    List<Header> headers = Lists.newArrayList(
      new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
      new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
    );

    HttpClient httpClient = new HttpClient();
    HttpResponse<JsonNode> response = null;

    try {
      response = httpClient.get(testRailUrl, headers, new TypeReference<>() {
      });

      if (response.getStatusCode() == 200) {
        String responseBody = response.getResponseText();
        JsonNode responseJson = new ObjectMapper().readTree(responseBody);
        JsonNode projectId = responseJson.get("project_id");

        HttpResponse<JsonNode> responseForProjectName = null;
        responseForProjectName = httpClient.get(fetchProjectDetailsFromProjectIdUrl(cicdCredentials.getUrl(),
          String.valueOf(projectId)), headers, new TypeReference<>() {
        });
        if (responseForProjectName.getStatusCode() == 200) {
          String responseBodyForProjectName = responseForProjectName.getResponseText();
          JsonNode responseJsonforProjectName = new ObjectMapper().readTree(responseBodyForProjectName);
          return responseJsonforProjectName.get("name").asText();
        }
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to fetch Project details from TestRail", e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private String fetchProjectIdFromRunIdUrl(@NotNull String url, String testrailRunId) {
    url = (url.endsWith("/") ? url : url + "/");
    return url + URLConstants.TESTRAIL_GET_PROJECT_ID_FROM_RUN_ID + "/" + testrailRunId;
  }

  private String getEncodedAuth(CICDCredentials cicdCredentials) {
    String auth = cicdCredentials.getUsername() + ":" + cicdCredentials.getPassword();
    return new String(Base64.encodeBase64(auth.getBytes()));
  }

  private String fetchProjectDetailsFromProjectIdUrl(@NotNull String url, String projectId) {
    url = (url.endsWith("/")
      ? url + URLConstants.TESTRAIL_GET_PROJECT_DETAILS_FROM_PROJECT_ID
      : url + "/" + URLConstants.TESTRAIL_GET_PROJECT_DETAILS_FROM_PROJECT_ID);
    return url + "/" + projectId;
  }

}
