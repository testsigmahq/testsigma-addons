package com.testsigma.addons.hook;

import com.google.gson.*;
import com.testsigma.sdk.Hook;
import com.testsigma.sdk.HookType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.RunResult;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestPlanHook;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestPlanHook(name = "Create Jira Tickets for Failed Test Case Results Test Plan", type = HookType.AFTER)
public class SendTestCaseResultsToJiraHook extends Hook {

    private static final String TESTSIGMA_API_BASE_URL = "https://app.testsigma.com/api/v1";

    @TestData(reference = "{JIRA URL}")
    private com.testsigma.sdk.TestData jiraUrl;

    @TestData(reference = "{JIRA USERNAME}")
    private com.testsigma.sdk.TestData jiraUsername;

    @TestData(reference = "{JIRA API TOKEN}")
    private com.testsigma.sdk.TestData jiraApiToken;

    @TestData(reference = "{JIRA PROJECT KEY}")
    private com.testsigma.sdk.TestData jiraProjectKey;

    @TestData(reference = "{API KEY}")
    private com.testsigma.sdk.TestData apiKey;

    @TestData(reference = "{JIRA ISSUE TYPE}")
    private com.testsigma.sdk.TestData jiraIssueType;

    @RunResult
    private com.testsigma.sdk.RunResult runresult;

    @Override
    protected Result execute() {
        log("==========================================");
        log("Jira Test Case Hook Execution Started");
        log("Timestamp: " + new java.util.Date());
        log("==========================================");

        com.testsigma.sdk.RunResult rootRunResult = runresult;
        int rerunLevel = 0;
        while (rootRunResult.getReRunParentResult() != null) {
            rootRunResult = rootRunResult.getReRunParentResult();
            rerunLevel++;
        }
        Long reportingId = rootRunResult.getId();
        log("Execution Run ID: " + reportingId);
        if (rerunLevel > 0) {
            log("Re-run level: " + rerunLevel);
        }

        String tsApiToken = apiKey.getValue().toString().trim();
        String jUrl = jiraUrl.getValue().toString().trim();
        if (jUrl.endsWith("/")) {
            jUrl = jUrl.substring(0, jUrl.length() - 1);
        }
        String jUser = jiraUsername.getValue().toString().trim();
        String jToken = jiraApiToken.getValue().toString().trim();
        String jProject = jiraProjectKey.getValue().toString().trim();
        String jIssueType = null;
        if (jiraIssueType != null && jiraIssueType.getValue() != null) {
            jIssueType = jiraIssueType.getValue().toString().trim();
        }

        log("Jira Configuration:");
        log("  URL: " + jUrl);
        log("  Username: " + jUser);
        log("  Project Key: " + jProject);
        log("Testsigma API Base URL: " + TESTSIGMA_API_BASE_URL);

        if (jUrl.isEmpty()) {
            setErrorMessage("Jira URL is required. Please set the JIRA URL test data.");
            return Result.FAILED;
        }
        if (jUser.isEmpty()) {
            setErrorMessage("Jira username is required. Please set the JIRA USERNAME test data.");
            return Result.FAILED;
        }
        if (jToken.isEmpty()) {
            setErrorMessage("Jira API token is required. Please set the JIRA API TOKEN test data.");
            return Result.FAILED;
        }
        if (jProject.isEmpty()) {
            setErrorMessage("Jira project key is required. Please set the JIRA PROJECT KEY test data.");
            return Result.FAILED;
        }
        if (tsApiToken.isEmpty()) {
            setErrorMessage("Testsigma API key is required. Please set the API KEY test data.");
            return Result.FAILED;
        }

        try {
            log("------------------------------------------");
            log("Step 1: Fetching failed test cases from Testsigma API");
            log("------------------------------------------");
            JsonArray failedTestCases = fetchFailedTestCases(reportingId, tsApiToken);
            log("Total failed test cases found: " + failedTestCases.size());

            if (failedTestCases.size() == 0) {
                log("No failed test cases found. No Jira tickets created.");
                setSuccessMessage("No failed test cases found. No Jira tickets created.");
                return Result.SUCCESS;
            }

            log("------------------------------------------");
            log("Step 2: Creating Jira tickets for failed test cases");
            log("------------------------------------------");
            int ticketsCreated = 0;
            int duplicatesSkipped = 0;
            int errors = 0;
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < failedTestCases.size(); i++) {
                JsonObject testCase = failedTestCases.get(i).getAsJsonObject();
                log("Processing test case " + (i + 1) + " of " + failedTestCases.size());
                if (testCase.has("result") && !"FAILURE".equalsIgnoreCase(testCase.get("result").getAsString())) {
                    continue;
                }
                try {
                    if (createJiraTicket(testCase, jUrl, jUser, jToken, jProject, reportingId, jIssueType)) {
                        ticketsCreated++;
                    } else {
                        duplicatesSkipped++;
                    }
                } catch (Exception e) {
                    errors++;
                    String msg = e.getMessage();
                    if (e.getCause() != null && e.getCause().getMessage() != null) {
                        msg = msg + " Cause: " + e.getCause().getMessage();
                    }
                    errorMessages.add((msg != null ? msg : e.getClass().getSimpleName()) + "\n" + ExceptionUtils.getStackTrace(e));
                    log("ERROR creating ticket: " + msg);
                    log("Stack trace: " + ExceptionUtils.getStackTrace(e));
                }
            }

            log("------------------------------------------");
            log("Summary:");
            log("  Total failed test cases: " + failedTestCases.size());
            log("  Tickets created: " + ticketsCreated);
            log("  Duplicates skipped: " + duplicatesSkipped);
            log("  Errors: " + errors);
            log("------------------------------------------");

            if (errors > 0 && ticketsCreated == 0) {
                String errDetail = errorMessages.isEmpty() ? "" : " First error: " + errorMessages.get(0);
                setErrorMessage("Jira ticket creation failed for all " + failedTestCases.size() + " failed test case(s). Errors: " + errors + "." + errDetail);
                return Result.FAILED;
            }
            if (errors > 0 && ticketsCreated > 0) {
                String errDetail = errorMessages.isEmpty() ? "" : " First error: " + errorMessages.get(0);
                setErrorMessage("Only " + ticketsCreated + " Jira ticket(s) created. " + errors + " ticket(s) failed to create." + errDetail);
                return Result.FAILED;
            }
            if (ticketsCreated == 0) {
                setErrorMessage("No Jira tickets were created for " + failedTestCases.size() + " failed test case(s). All were skipped as duplicates. Verify Jira project key, issue type, and that no open ticket with the same summary already exists.");
                return Result.FAILED;
            }
            String message = "Created " + ticketsCreated + " Jira ticket(s) for failed test cases.";
            if (duplicatesSkipped > 0) {
                message += " Skipped " + duplicatesSkipped + " duplicate tickets.";
            }
            setSuccessMessage(message);
            return Result.SUCCESS;

        } catch (Exception e) {
            log("==========================================");
            log("ERROR OCCURRED!");
            log("==========================================");
            log("Error Type: " + e.getClass().getName());
            log("Error Message: " + e.getMessage());
            log(ExceptionUtils.getStackTrace(e));
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errMsg = errMsg + " Cause: " + e.getCause().getMessage();
            }
            setErrorMessage("Jira Test Case Hook failed: " + errMsg + "\n" + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    private void log(String message) {
        logger.info(message);
    }

    private JsonArray fetchFailedTestCases(Long runId, String apiToken) throws Exception {
        JsonArray allFailed = new JsonArray();
        int page = 0;
        int pageSize = 20;
        boolean hasMore = true;

        log("  API Request Configuration:");
        log("    Base URL: " + TESTSIGMA_API_BASE_URL);
        log("    Run ID: " + runId);
        log("    Page Size: " + pageSize);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        while (hasMore) {
            String query = "iteration:null,executionResultId:" + runId
                    + ",isStepGroup:false,afterTestParentResultId:null,dependentPrerequisiteCaseResultId:null,result:FAILURE";
            String url = TESTSIGMA_API_BASE_URL + "/test_case_results?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&size=" + pageSize + "&page=" + page;

            log("  Fetching page " + page + "...");
            log("  URL: " + url.replace(apiToken, "***REDACTED***"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log("  Response Status Code: " + response.statusCode());

            if (response.statusCode() != 200) {
                String body = response.body();
                String apiMsg = parseApiErrorResponse(body);
                String detail = apiMsg != null ? apiMsg : (body != null && body.length() > 300 ? body.substring(0, 300) + "..." : body);
                throw new Exception("Testsigma API error: failed to fetch test case results. Status " + response.statusCode() + ". " + detail);
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray currentPageData = new JsonArray();

            if (jsonResponse.has("data") && jsonResponse.get("data").isJsonArray()) {
                currentPageData = jsonResponse.getAsJsonArray("data");
            } else if (jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
                JsonObject dataObj = jsonResponse.getAsJsonObject("data");
                if (dataObj.has("content") && dataObj.get("content").isJsonArray()) {
                    currentPageData = dataObj.getAsJsonArray("content");
                }
            } else if (jsonResponse.has("content") && jsonResponse.get("content").isJsonArray()) {
                currentPageData = jsonResponse.getAsJsonArray("content");
            }

            log("  Items in current page: " + currentPageData.size());

            JsonArray failedInPage = new JsonArray();
            for (JsonElement element : currentPageData) {
                JsonObject testCase = element.getAsJsonObject();
                if (testCase.has("result") && "FAILURE".equalsIgnoreCase(testCase.get("result").getAsString())) {
                    failedInPage.add(testCase);
                }
            }
            log("  Failed cases in current page: " + failedInPage.size());

            allFailed.addAll(failedInPage);

            if (currentPageData.size() == 0) {
                hasMore = false;
            } else if (currentPageData.size() < pageSize) {
                hasMore = false;
            } else {
                page++;
            }
        }
        log("  Total failed test cases fetched: " + allFailed.size());
        return allFailed;
    }

    private Map<String, Object> plainTextToAtlassianDocumentFormatWithLink(String bodyText, String linkUrl) {
        List<Map<String, Object>> paragraphContent = new ArrayList<>();
        Map<String, Object> bodyNode = new HashMap<>();
        bodyNode.put("type", "text");
        bodyNode.put("text", bodyText != null ? bodyText : "");
        paragraphContent.add(bodyNode);

        Map<String, Object> linkAttrs = new HashMap<>();
        linkAttrs.put("href", linkUrl != null ? linkUrl : "");
        Map<String, Object> linkMark = new HashMap<>();
        linkMark.put("type", "link");
        linkMark.put("attrs", linkAttrs);
        List<Map<String, Object>> marks = new ArrayList<>();
        marks.add(linkMark);
        Map<String, Object> linkTextNode = new HashMap<>();
        linkTextNode.put("type", "text");
        linkTextNode.put("text", linkUrl != null ? linkUrl : "");
        linkTextNode.put("marks", marks);
        paragraphContent.add(linkTextNode);

        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("type", "paragraph");
        paragraph.put("content", paragraphContent);
        List<Map<String, Object>> docContent = new ArrayList<>();
        docContent.add(paragraph);
        Map<String, Object> doc = new HashMap<>();
        doc.put("version", 1);
        doc.put("type", "doc");
        doc.put("content", docContent);
        return doc;
    }

    private boolean createJiraTicket(JsonObject testCaseResult, String jiraUrl, String username, String apiToken,
            String projectKey, Long runId, String issueTypeConfig) throws Exception {

        String testCaseName = "Unknown Test Case";
        Long testCaseId = null;

        if (testCaseResult.has("testCaseName")) {
            testCaseName = testCaseResult.get("testCaseName").getAsString();
        } else if (testCaseResult.has("testCaseDetails") && testCaseResult.get("testCaseDetails").isJsonObject()) {
            JsonObject testCaseDetails = testCaseResult.getAsJsonObject("testCaseDetails");
            if (testCaseDetails.has("name")) {
                testCaseName = testCaseDetails.get("name").getAsString();
            }
        } else if (testCaseResult.has("testCase") && testCaseResult.get("testCase").isJsonObject()) {
            JsonObject testCase = testCaseResult.getAsJsonObject("testCase");
            if (testCase.has("name")) {
                testCaseName = testCase.get("name").getAsString();
            }
        }

        if (testCaseResult.has("testCaseId")) {
            testCaseId = testCaseResult.get("testCaseId").getAsLong();
        } else if (testCaseResult.has("testCase") && testCaseResult.get("testCase").isJsonObject()) {
            JsonObject testCase = testCaseResult.getAsJsonObject("testCase");
            if (testCase.has("id")) {
                testCaseId = testCase.get("id").getAsLong();
            }
        }

        String environmentName = "Unknown Environment";
        if (testCaseResult.has("environmentSettings") && !testCaseResult.get("environmentSettings").isJsonNull()) {
            JsonObject envSettings = testCaseResult.getAsJsonObject("environmentSettings");
            if (envSettings.has("title")) {
                environmentName = envSettings.get("title").getAsString();
            }
        } else if (testCaseResult.has("environmentResult") && !testCaseResult.get("environmentResult").isJsonNull()) {
            JsonObject envResult = testCaseResult.getAsJsonObject("environmentResult");
            if (envResult.has("environmentSettings") && envResult.get("environmentSettings").isJsonObject()) {
                JsonObject envSettings = envResult.getAsJsonObject("environmentSettings");
                if (envSettings.has("title")) {
                    environmentName = envSettings.get("title").getAsString();
                }
            } else if (envResult.has("executionEnvironment") && envResult.get("executionEnvironment").isJsonObject()) {
                JsonObject execEnv = envResult.getAsJsonObject("executionEnvironment");
                if (execEnv.has("title")) {
                    environmentName = execEnv.get("title").getAsString();
                }
            }
        }

        String testDataSetName = "N/A";
        if (testCaseResult.has("testDataSetName") && !testCaseResult.get("testDataSetName").isJsonNull()) {
            testDataSetName = testCaseResult.get("testDataSetName").getAsString();
        }

        String summary = "[Testsigma] Failure: " + testCaseName;

        if (issueAlreadyExists(jiraUrl, username, apiToken, projectKey, summary)) {
            log("Duplicate ticket found. Skipping creation.");
            return false;
        }

        String runLinkUrl = "https://app.testsigma.com/ui/td/runs/" + runId;
        String message = testCaseResult.has("message") && !testCaseResult.get("message").isJsonNull()
                ? testCaseResult.get("message").getAsString()
                : "No error message provided.";
        String descriptionBody = "Test Case Failed.\n" +
                "Name: " + testCaseName + "\n" +
                "Environment: " + environmentName + "\n" +
                "Test Data Set: " + testDataSetName + "\n" +
                "Testsigma Run ID: " + runId + "\n" +
                (testCaseId != null ? "Test Case ID: " + testCaseId + "\n" : "") +
                "Result Message: " + message + "\n\n" +
                "Link to Run: ";

        Map<String, Object> descriptionAdf = plainTextToAtlassianDocumentFormatWithLink(descriptionBody, runLinkUrl);

        Map<String, Object> fields = new HashMap<>();
        Map<String, String> project = new HashMap<>();
        project.put("key", projectKey);
        fields.put("project", project);
        fields.put("summary", summary);
        fields.put("description", descriptionAdf);

        Map<String, Object> issuetype = getIssueType(jiraUrl, username, apiToken, projectKey, issueTypeConfig);
        fields.put("issuetype", issuetype);

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("fields", fields);
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(payloadMap);

        String authString = username + ":" + apiToken;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jiraUrl + "/rest/api/3/issue"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log("Jira API Response Status: " + response.statusCode());

        if (response.statusCode() != 201) {
            String body = response.body();
            String jiraError = parseJiraErrorResponse(body);
            String errorMsg = "Jira API returned " + response.statusCode()
                    + (jiraError != null ? ". " + jiraError : ". Response: " + (body != null && body.length() > 200 ? body.substring(0, 200) + "..." : body));
            log("Jira API error: " + errorMsg);
            throw new Exception(errorMsg);
        }
        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String ticketKey = responseJson.has("key") ? responseJson.get("key").getAsString() : "Unknown";
        log("Ticket created: " + ticketKey);
        return true;
    }

    private String parseApiErrorResponse(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (json.has("message")) return json.get("message").getAsString();
            if (json.has("error")) return json.get("error").getAsString();
            if (json.has("error_description")) return json.get("error_description").getAsString();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String parseJiraErrorResponse(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            StringBuilder sb = new StringBuilder();
            if (json.has("errorMessages") && json.get("errorMessages").isJsonArray()) {
                for (JsonElement e : json.getAsJsonArray("errorMessages")) {
                    if (sb.length() > 0) sb.append("; ");
                    sb.append(e.getAsString());
                }
            }
            if (json.has("errors") && json.get("errors").isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("errors").entrySet()) {
                    if (sb.length() > 0) sb.append("; ");
                    sb.append(entry.getKey()).append(": ").append(entry.getValue().getAsString());
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean issueAlreadyExists(String jiraUrl, String username, String apiToken, String projectKey,
            String summary) {
        try {
            String jql = "project = " + projectKey + " AND summary ~ \"\\\"" + summary.replace("\"", "\\\\\"")
                    + "\\\"\" AND statusCategory != Done";
            String searchUrl = jiraUrl + "/rest/api/3/search/jql?jql="
                    + URLEncoder.encode(jql, StandardCharsets.UTF_8) + "&maxResults=1";

            String authString = username + ":" + apiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .header("Authorization", authHeader)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("total")) {
                    int total = json.get("total").getAsInt();
                    return total > 0;
                }
            }
        } catch (Exception e) {
            log("Duplicate check failed: " + e.getMessage());
        }
        return false;
    }

    private Map<String, Object> getIssueType(String jiraUrl, String username, String apiToken, String projectKey,
            String issueTypeConfig) {
        if (issueTypeConfig != null && !issueTypeConfig.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            String type = issueTypeConfig.trim();
            if (type.matches("\\d+")) {
                result.put("id", type);
            } else {
                result.put("name", type);
            }
            return result;
        }
        try {
            String projectUrl = jiraUrl + "/rest/api/3/project/" + projectKey;
            String authString = username + ":" + apiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(projectUrl))
                    .header("Authorization", authHeader)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject projectJson = JsonParser.parseString(response.body()).getAsJsonObject();
                if (projectJson.has("issueTypes") && projectJson.get("issueTypes").isJsonArray()) {
                    JsonArray issueTypes = projectJson.getAsJsonArray("issueTypes");
                    for (JsonElement element : issueTypes) {
                        JsonObject it = element.getAsJsonObject();
                        if (it.has("name") && "Bug".equalsIgnoreCase(it.get("name").getAsString())) {
                            Map<String, Object> r = new HashMap<>();
                            if (it.has("id")) r.put("id", it.get("id").getAsString());
                            else r.put("name", "Bug");
                            return r;
                        }
                    }
                    for (JsonElement element : issueTypes) {
                        JsonObject it = element.getAsJsonObject();
                        if (it.has("name") && "Task".equalsIgnoreCase(it.get("name").getAsString())) {
                            Map<String, Object> r = new HashMap<>();
                            if (it.has("id")) r.put("id", it.get("id").getAsString());
                            else r.put("name", "Task");
                            return r;
                        }
                    }
                    if (issueTypes.size() > 0) {
                        JsonObject first = issueTypes.get(0).getAsJsonObject();
                        Map<String, Object> r = new HashMap<>();
                        if (first.has("id")) r.put("id", first.get("id").getAsString());
                        else if (first.has("name")) r.put("name", first.get("name").getAsString());
                        return r;
                    }
                }
            } else {
                return getIssueTypesFromCreatemeta(jiraUrl, username, apiToken, projectKey);
            }
        } catch (Exception e) {
            logger.warn("getIssueType: " + e.getMessage());
            try {
                return getIssueTypesFromCreatemeta(jiraUrl, username, apiToken, projectKey);
            } catch (Exception ex) {
                logger.warn("getIssueTypesFromCreatemeta: " + ex.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("name", "Task");
        return result;
    }

    private Map<String, Object> getIssueTypesFromCreatemeta(String jiraUrl, String username, String apiToken,
            String projectKey) throws Exception {
        String metaUrl = jiraUrl + "/rest/api/3/issue/createmeta?projectKeys=" + projectKey
                + "&expand=projects.issuetypes.fields";
        String authString = username + ":" + apiToken;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(metaUrl))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            if (json.has("projects") && json.get("projects").isJsonArray()) {
                JsonArray projects = json.getAsJsonArray("projects");
                if (projects.size() > 0) {
                    JsonObject project = projects.get(0).getAsJsonObject();
                    if (project.has("issuetypes") && project.get("issuetypes").isJsonArray()) {
                        JsonArray issueTypes = project.getAsJsonArray("issuetypes");
                        Map<String, String> availableTypes = new HashMap<>();
                        for (JsonElement element : issueTypes) {
                            JsonObject type = element.getAsJsonObject();
                            String name = type.get("name").getAsString();
                            String id = type.get("id").getAsString();
                            availableTypes.put(name, id);
                        }
                        if (availableTypes.containsKey("Bug")) {
                            Map<String, Object> r = new HashMap<>();
                            r.put("id", availableTypes.get("Bug"));
                            return r;
                        }
                        if (availableTypes.containsKey("Task")) {
                            Map<String, Object> r = new HashMap<>();
                            r.put("id", availableTypes.get("Task"));
                            return r;
                        }
                        if (issueTypes.size() > 0) {
                            JsonObject first = issueTypes.get(0).getAsJsonObject();
                            Map<String, Object> r = new HashMap<>();
                            r.put("id", first.get("id").getAsString());
                            return r;
                        }
                    }
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("name", "Bug");
        return result;
    }
}
