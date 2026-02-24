package com.testsigma.addons.hook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.testsigma.sdk.Hook;
import com.testsigma.sdk.HookType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.RunResult;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestPlanHook;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;


@TestPlanHook(name = "Send Test Plan Results to Email", type = HookType.AFTER)
public class SendTestPlanResultsToEmail extends Hook {

    private static final String TESTSIGMA_API_BASE_URL = "https://app-in.testsigma.com/api/v1";
    private static final String TESTSIGMA_APP_BASE_URL = "https://app-in.testsigma.com";
    private static final int API_TIMEOUT_SEC = 30;
    private static final int PAGE_SIZE = 100;

    private static final String TEST_CASE_RESULTS_QUERY_BASE =
            "iteration:null,executionResultId:%d,isStepGroup:false,afterTestParentResultId:null,dependentPrerequisiteCaseResultId:null";

    private static final String DEFAULT_FROM_EMAIL = "ent_support@testsigma.com";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "ent_support@testsigma.com";
    private static final String SMTP_PASSWORD = "asla sdqa frwa uynz";

    @TestData(reference = "{API KEY}")
    private com.testsigma.sdk.TestData apiKey;

    @TestData(reference = "{TO EMAIL}")
    private com.testsigma.sdk.TestData toEmail;

    @RunResult
    private com.testsigma.sdk.RunResult runresult;

    @Override
    protected Result execute() {
        List<Long> runIdsInOrder = buildRunIdsChain();
        Long runId = runresult.getId();

        String tsApiToken = apiKey.getValue().toString().trim();
        String toRaw = toEmail != null && toEmail.getValue() != null ? toEmail.getValue().toString().trim() : "";
        List<String> toList = parseCommaSeparatedEmails(toRaw);
        String to = String.join(", ", toList);
        String host = SMTP_HOST != null ? SMTP_HOST.trim() : "";
        String port = SMTP_PORT != null && !SMTP_PORT.isEmpty() ? SMTP_PORT.trim() : "587";
        String smtpUserVal = SMTP_USER != null ? SMTP_USER.trim() : "";
        String smtpPass = SMTP_PASSWORD != null ? SMTP_PASSWORD.trim().replaceAll("\\s+", "") : "";

        if (tsApiToken.isEmpty()) {
            setErrorMessage("API KEY is required. Please set the API KEY test data.");
            return Result.FAILED;
        }
        if (toList.isEmpty()) {
            setErrorMessage("TO EMAIL is required. Please set the TO EMAIL test data (comma-separated for multiple recipients).");
            return Result.FAILED;
        }
        if (host.isEmpty()) {
            setErrorMessage("SMTP HOST is required. Please set SMTP_HOST constant in code.");
            return Result.FAILED;
        }

        String from = (DEFAULT_FROM_EMAIL != null && !DEFAULT_FROM_EMAIL.isEmpty()) ? DEFAULT_FROM_EMAIL.trim() : toList.get(0);

        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(API_TIMEOUT_SEC)).build();

            List<JsonObject> allRunDetails = fetchRunDetailsChain(runIdsInOrder, tsApiToken, httpClient);
            JsonObject runDetails = allRunDetails.isEmpty() ? new JsonObject() : allRunDetails.get(0);
            JsonObject currentRunDetails = allRunDetails.isEmpty() ? null : allRunDetails.get(allRunDetails.size() - 1);

            Boolean isFirstRunFromRd = isFirstRunFromRunDetails(allRunDetails, currentRunDetails);
            Long runIdForFirstRunCheck = runIdsInOrder.size() <= 1 ? runId : runIdsInOrder.get(0);
            TestCaseResultsData resultsData = fetchTestCaseResultsData(runId, runIdForFirstRunCheck, tsApiToken, httpClient);

            boolean isFirstRun = isFirstRunFromRd != null
                    ? isFirstRunFromRd
                    : (resultsData.isFirstRun != null ? resultsData.isFirstRun : isFirstRunFromTestCaseResults(runIdForFirstRunCheck, tsApiToken, httpClient));

            // Prefer counts from execution_result (current run details) to avoid extra API call
            Map<String, Integer> counts = getCountsFromRunDetails(currentRunDetails);
            if (counts == null && !isFirstRun && !allRunDetails.isEmpty()) {
                Long lastRunId = getLastRunIdFromRunDetails(allRunDetails.get(0));
                if (lastRunId != null)
                    counts = fetchPlanCountsFromTestCaseResultsByExecutionResultId(lastRunId, tsApiToken, httpClient);
            }
            if (counts == null && resultsData.planCountsFromApi != null) counts = resultsData.planCountsFromApi;
            if (counts == null) counts = defaultCounts();
            JsonArray failedTestCases = resultsData.failedCases;
            String testPlanName = getTestPlanNameFromRunDetails(runDetails);
            String subject = "'" + testPlanName + "' Test Plan has been Completed";
            String htmlBody = buildEmailHtml(runId, allRunDetails, counts, failedTestCases, isFirstRun);
            sendEmail(host, port, smtpUserVal, smtpPass, from, to, subject, htmlBody);

            setSuccessMessage(toList.size() == 1
                    ? "Test plan results email sent successfully to " + to
                    : "Test plan results email sent successfully to " + toList.size() + " recipients: " + to);
            return Result.SUCCESS;

        } catch (Exception e) {
            log("SendTestPlanResultsToEmail failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            log(ExceptionUtils.getStackTrace(e));
            String errMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errMsg = errMsg + " Cause: " + e.getCause().getMessage();
            }
            if (errMsg.contains("535") && host.toLowerCase().contains("gmail")) {
                errMsg = "SMTP login rejected (535). If using @gmail.com: use an App Password (Google Account → Security → App passwords). If using @testsigma.com with smtp.gmail.com, your company may use Google Workspace—use the App Password for that account. Otherwise use your organization's SMTP server (e.g. smtp.office365.com) instead of smtp.gmail.com. " + errMsg;
            }
            setErrorMessage("Send Test Plan Results to Email failed: " + errMsg);
            return Result.FAILED;
        }
    }

    /** Builds [root, rerun1, ..., current] from SDK run result chain. */
    private List<Long> buildRunIdsChain() {
        List<Long> ids = new ArrayList<>();
        com.testsigma.sdk.RunResult r = runresult;
        while (r != null) {
            ids.add(r.getId());
            r = r.getReRunParentResult();
        }
        Collections.reverse(ids);
        return ids;
    }

    private List<JsonObject> fetchRunDetailsChain(List<Long> runIdsInOrder, String apiToken, HttpClient httpClient) throws Exception {
        List<JsonObject> list = new ArrayList<>(runIdsInOrder.size());
        for (Long id : runIdsInOrder) {
            list.add(fetchExecutionResult(id, apiToken, httpClient));
        }
        return list;
    }

    /**
     * Fetches failed test cases and optionally isFirstRun from first page when runId == runIdForFirstRunCheck.
     * Counts are not computed here; use getCountsFromRunDetails (testPlanResultMetric) instead.
     */
    private TestCaseResultsData fetchTestCaseResultsData(Long runId, Long runIdForFirstRunCheck, String apiToken, HttpClient httpClient) throws Exception {
        JsonArray failedCases = new JsonArray();
        Boolean isFirstRunFromFirstPage = null;
        Map<String, Integer> planCountsFromApi = null;
        int page = 0;
        boolean hasMore = true;
        while (hasMore) {
            String query = String.format(TEST_CASE_RESULTS_QUERY_BASE, runId);
            String url = TESTSIGMA_API_BASE_URL + "/test_case_results?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&size=" + PAGE_SIZE + "&page=" + page;
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                            .header("Authorization", "Bearer " + apiToken)
                            .header("Accept", "application/json")
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() != 200) {
                String apiMsg = parseApiErrorResponse(body);
                throw new Exception("Testsigma API error. Status " + response.statusCode() + ". " + (apiMsg != null ? apiMsg : body));
            }
            JsonObject pageResponse = JsonParser.parseString(body).getAsJsonObject();
            JsonArray pageData = extractContentArray(pageResponse);
            if (page == 0) {
                if (runId.equals(runIdForFirstRunCheck)) {
                    isFirstRunFromFirstPage = computeIsFirstRunFromContent(pageData);
                }
                for (JsonElement el : pageData) {
                    planCountsFromApi = getPlanCountsFromTestCaseResultContent(el.getAsJsonObject());
                    if (planCountsFromApi != null) break;
                }
            }
            for (JsonElement el : pageData) {
                JsonObject tc = el.getAsJsonObject();
                if (tc.has("result") && !tc.get("result").isJsonNull()
                        && tc.get("result").getAsString().toLowerCase().contains("fail")) {
                    failedCases.add(tc);
                }
            }
            hasMore = pageData.size() >= PAGE_SIZE;
            page++;
        }
        return new TestCaseResultsData(failedCases, isFirstRunFromFirstPage, planCountsFromApi);
    }

    /** True if no content item has a meaningful lastReRunResult (fresh run). */
    private boolean computeIsFirstRunFromContent(JsonArray content) {
        if (content == null || content.isEmpty()) return true;
        for (int i = 0; i < content.size(); i++) {
            JsonObject item = content.get(i).getAsJsonObject();
            if (item.has("lastReRunResult") && isMeaningfulLastReRunResult(item.get("lastReRunResult")))
                return false;
            if (item.has("environmentResult") && item.get("environmentResult").isJsonObject()) {
                JsonObject envResult = item.getAsJsonObject("environmentResult");
                if (envResult.has("executionResult") && envResult.get("executionResult").isJsonObject()) {
                    JsonObject execResult = envResult.getAsJsonObject("executionResult");
                    if (execResult.has("lastReRunResult") && isMeaningfulLastReRunResult(execResult.get("lastReRunResult")))
                        return false;
                }
            }
        }
        return true;
    }

    private static class TestCaseResultsData {
        final JsonArray failedCases;
        final Boolean isFirstRun;
        /** Counts from content[].environmentResult.executionResult.testPlanResultMetric when present. */
        final Map<String, Integer> planCountsFromApi;

        TestCaseResultsData(JsonArray failedCases, Boolean isFirstRun, Map<String, Integer> planCountsFromApi) {
            this.failedCases = failedCases;
            this.isFirstRun = isFirstRun;
            this.planCountsFromApi = planCountsFromApi;
        }
    }

    /**
     * Extracts plan counts from test_case_results content item path:
     * content[].environmentResult.executionResult.testPlanResultMetric
     * Uses only consolidated plan counts (consolidatedPlanTotalCount, etc.).
     */
    private Map<String, Integer> getPlanCountsFromTestCaseResultContent(JsonObject contentItem) {
        if (contentItem == null) return null;
        if (!contentItem.has("environmentResult") || !contentItem.get("environmentResult").isJsonObject()) return null;
        JsonObject envResult = contentItem.getAsJsonObject("environmentResult");
        if (!envResult.has("executionResult") || !envResult.get("executionResult").isJsonObject()) return null;
        JsonObject execResult = envResult.getAsJsonObject("executionResult");
        if (!execResult.has("testPlanResultMetric") || !execResult.get("testPlanResultMetric").isJsonObject()) return null;
        JsonObject metric = execResult.getAsJsonObject("testPlanResultMetric");
        return getCountsFromTestPlanResultMetric(metric);
    }

    /**
     * Builds counts map from testPlanResultMetric using only consolidated plan fields.
     */
    private Map<String, Integer> getCountsFromTestPlanResultMetric(JsonObject metric) {
        if (metric == null) return null;
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("total", getInt(metric, "consolidatedPlanTotalCount", null, 0));
        counts.put("passed", getInt(metric, "consolidatedPlanPassedCount", null, 0));
        counts.put("failure", getInt(metric, "consolidatedPlanFailedCount", null, 0));
        counts.put("stopped", getInt(metric, "consolidatedPlanStoppedCount", null, 0));
        counts.put("not_executed", getInt(metric, "consolidatedPlanNotExecutedCount", null, 0));
        counts.put("queued", getInt(metric, "consolidatedPlanQueuedCount", null, 0));
        counts.put("running", getInt(metric, "consolidatedPlanRunningCount", null, 0));
        return counts;
    }

    private static Map<String, Integer> defaultCounts() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("total", 0);
        m.put("passed", 0);
        m.put("failure", 0);
        m.put("queued", 0);
        m.put("running", 0);
        m.put("stopped", 0);
        m.put("not_executed", 0);
        return m;
    }

    private JsonObject fetchExecutionResult(Long runId, String apiToken, HttpClient httpClient) throws Exception {
        String url = TESTSIGMA_API_BASE_URL + "/execution_results/" + runId;
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("Accept", "application/json")
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (response.statusCode() != 200) {
            String msg = parseApiErrorResponse(body);
            throw new Exception("Failed to fetch execution result. Status " + response.statusCode() + ". " + (msg != null ? msg : body));
        }
        if (body == null || body.isEmpty()) return new JsonObject();
        JsonElement el = JsonParser.parseString(body);
        return el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
    }

    /**
     * True if lastReRunResult is a non-null object with id or executionResultId (indicates a rerun exists).
     */
    private boolean isMeaningfulLastReRunResult(JsonElement v) {
        if (v == null || v.isJsonNull()) return false;
        if (!v.isJsonObject()) return false;
        JsonObject o = v.getAsJsonObject();
        return (o.has("id") && !o.get("id").isJsonNull())
                || (o.has("executionResultId") && !o.get("executionResultId").isJsonNull());
    }

    /** Used only when runIdForFirstRunCheck != current runId (e.g. chain: need root run's first page). */
    private boolean isFirstRunFromTestCaseResults(Long runId, String apiToken, HttpClient httpClient) {
        try {
            String query = String.format(TEST_CASE_RESULTS_QUERY_BASE, runId);
            String url = TESTSIGMA_API_BASE_URL + "/test_case_results?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&size=" + PAGE_SIZE + "&page=0";
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                            .header("Authorization", "Bearer " + apiToken)
                            .header("Accept", "application/json")
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() != 200) return true;
            JsonArray content = extractContentArray(JsonParser.parseString(body).getAsJsonObject());
            return computeIsFirstRunFromContent(content);
        } catch (Exception e) {
            log("First-run detection failed: " + e.getMessage());
            return true;
        }
    }

    private JsonArray extractContentArray(JsonObject jsonResponse) {
        if (jsonResponse.has("content") && jsonResponse.get("content").isJsonArray())
            return jsonResponse.getAsJsonArray("content");
        if (jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
            JsonObject dataObj = jsonResponse.get("data").getAsJsonObject();
            if (dataObj.has("content") && dataObj.get("content").isJsonArray())
                return dataObj.getAsJsonArray("content");
        }
        if (jsonResponse.has("data") && jsonResponse.get("data").isJsonArray())
            return jsonResponse.getAsJsonArray("data");
        return new JsonArray();
    }

    /**
     * Fetches test_case_results with executionResultId = lastRunId (one page), extracts Total, Passed, Failure, etc.
     * from content[].environmentResult.executionResult.testPlanResultMetric (totalCount, passedCount, failedCount, ...).
     */
    private Map<String, Integer> fetchPlanCountsFromTestCaseResultsByExecutionResultId(Long executionResultId, String apiToken, HttpClient httpClient) {
        try {
            String query = String.format(TEST_CASE_RESULTS_QUERY_BASE, executionResultId);
            String url = TESTSIGMA_API_BASE_URL + "/test_case_results?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&size=" + PAGE_SIZE + "&page=0";
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                            .header("Authorization", "Bearer " + apiToken)
                            .header("Accept", "application/json")
                            .GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() != 200) return null;
            JsonObject pageResponse = JsonParser.parseString(body).getAsJsonObject();
            JsonArray content = extractContentArray(pageResponse);
            for (JsonElement el : content) {
                Map<String, Integer> counts = getPlanCountsFromTestCaseResultContent(el.getAsJsonObject());
                if (counts != null) return counts;
            }
        } catch (Exception e) {
            log("Failed to fetch plan counts from test_case_results for executionResultId " + executionResultId + ": " + e.getMessage());
        }
        return null;
    }

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US).withZone(ZoneOffset.UTC);

    /** Format timestamp from API: epoch ms (or seconds if value < 1e12) -> "yyyy-MM-dd HH:mm:ss UTC". */
    private String formatTimestamp(JsonObject rd, String key, String fallbackKey) {
        JsonElement el = null;
        if (rd.has(key) && !rd.get(key).isJsonNull()) el = rd.get(key);
        else if (fallbackKey != null && rd.has(fallbackKey) && !rd.get(fallbackKey).isJsonNull()) el = rd.get(fallbackKey);
        if (el == null) return "N/A";
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
            long v = el.getAsLong();
            long ms = (v > 0 && v < 1_000_000_000_000L) ? v * 1000 : v; // seconds vs milliseconds
            return TIMESTAMP_FORMAT.format(Instant.ofEpochMilli(ms)) + " UTC";
        }
        String s = el.getAsString();
        return (s == null || s.isEmpty()) ? "N/A" : (s.contains("UTC") ? s.trim() : s.trim() + " UTC");
    }

    private JsonObject unwrapRunDetails(JsonObject runDetails) {
        if (runDetails == null) return null;
        if (runDetails.has("data") && runDetails.get("data").isJsonObject())
            return runDetails.getAsJsonObject("data");
        return runDetails;
    }

    /**
     * Reads consolidated counts from testPlanResultMetric in run details (execution_results API).
     * Looks at rd.testPlanResultMetric or rd.run.testPlanResultMetric.
     * Returns null if not present (fall back to counting from test case results).
     */
    private Map<String, Integer> getCountsFromRunDetails(JsonObject runDetailsRaw) {
        JsonObject rd = unwrapRunDetails(runDetailsRaw);
        if (rd == null) return null;
        JsonObject metric = null;
        if (rd.has("testPlanResultMetric") && rd.get("testPlanResultMetric").isJsonObject()) {
            metric = rd.getAsJsonObject("testPlanResultMetric");
        }
        if (metric == null && rd.has("run") && rd.get("run").isJsonObject()) {
            JsonObject run = rd.getAsJsonObject("run");
            if (run.has("testPlanResultMetric") && run.get("testPlanResultMetric").isJsonObject()) {
                metric = run.getAsJsonObject("testPlanResultMetric");
            }
        }
        if (metric == null) return null;
        return getCountsFromTestPlanResultMetric(metric);
    }

    private int getInt(JsonObject obj, String primaryKey, String fallbackKey, int defaultValue) {
        if (obj.has(primaryKey) && !obj.get(primaryKey).isJsonNull()) return obj.get(primaryKey).getAsInt();
        if (fallbackKey != null && obj.has(fallbackKey) && !obj.get(fallbackKey).isJsonNull()) return obj.get(fallbackKey).getAsInt();
        return defaultValue;
    }

    /** First run from run details: single run with no reRunParentId. Returns null if unknown. */
    private Boolean isFirstRunFromRunDetails(List<JsonObject> allRunDetails, JsonObject currentRunDetailsRaw) {
        if (allRunDetails == null || allRunDetails.isEmpty()) return null;
        if (allRunDetails.size() > 1) return false;
        JsonObject rd = unwrapRunDetails(currentRunDetailsRaw);
        if (rd == null) return null;
        Long parentId = null;
        if (rd.has("reRunParentId") && !rd.get("reRunParentId").isJsonNull())
            parentId = rd.get("reRunParentId").getAsLong();
        if (parentId == null && rd.has("execution") && rd.get("execution").isJsonObject()) {
            JsonObject exec = rd.getAsJsonObject("execution");
            if (exec.has("reRunParentId") && !exec.get("reRunParentId").isJsonNull())
                parentId = exec.get("reRunParentId").getAsLong();
        }
        return parentId == null || parentId == 0;
    }

    private String getTestPlanNameFromRunDetails(JsonObject runDetails) {
        JsonObject obj = unwrapRunDetails(runDetails);
        if (obj == null) return "Test Plan";
        // API returns test plan name under execution.name
        if (obj.has("execution") && obj.get("execution").isJsonObject()) {
            JsonObject execution = obj.getAsJsonObject("execution");
            if (execution.has("name") && !execution.get("name").isJsonNull()) return execution.get("name").getAsString();
        }
        if (obj.has("name") && !obj.get("name").isJsonNull()) return obj.get("name").getAsString();
        if (obj.has("testPlanName") && !obj.get("testPlanName").isJsonNull()) return obj.get("testPlanName").getAsString();
        if (obj.has("testPlan") && obj.get("testPlan").isJsonObject()) {
            JsonObject tp = obj.getAsJsonObject("testPlan");
            if (tp.has("name")) return tp.get("name").getAsString();
        }
        return "Test Plan";
    }

    /** lastRunId from run details (execution.lastRunId or rd.lastRunId). For rerun, use this as the execution link RunId. */
    private Long getLastRunIdFromRunDetails(JsonObject runDetailsRaw) {
        JsonObject rd = unwrapRunDetails(runDetailsRaw);
        if (rd == null) return null;
        if (rd.has("execution") && rd.get("execution").isJsonObject()) {
            JsonObject exec = rd.getAsJsonObject("execution");
            if (exec.has("lastRunId") && !exec.get("lastRunId").isJsonNull()) return exec.get("lastRunId").getAsLong();
        }
        if (rd.has("lastRunId") && !rd.get("lastRunId").isJsonNull()) return rd.get("lastRunId").getAsLong();
        return null;
    }

    private String buildEmailHtml(Long runId, List<JsonObject> allRunDetails, Map<String, Integer> counts, JsonArray failedTestCases, boolean isFirstRun) {
        JsonObject firstRunRaw = allRunDetails != null && !allRunDetails.isEmpty() ? allRunDetails.get(0) : null;
        JsonObject firstRun = unwrapRunDetails(firstRunRaw);
        // Resolve test plan name: try current run (last) first, then first, then any run that has execution.name
        String testPlanName = "Test Plan";
        if (allRunDetails != null && !allRunDetails.isEmpty()) {
            int last = allRunDetails.size() - 1;
            for (int idx : new int[]{ last, 0 }) {
                String name = getTestPlanNameFromRunDetails(allRunDetails.get(idx));
                if (name != null && !name.isEmpty() && !"Test Plan".equals(name)) {
                    testPlanName = name;
                    break;
                }
            }
            if ("Test Plan".equals(testPlanName)) {
                for (JsonObject raw : allRunDetails) {
                    String name = getTestPlanNameFromRunDetails(raw);
                    if (name != null && !name.isEmpty() && !"Test Plan".equals(name)) {
                        testPlanName = name;
                        break;
                    }
                }
            }
        }
        String description = "";
        if (firstRun != null) {
            if (firstRun.has("description") && !firstRun.get("description").isJsonNull()) {
                description = firstRun.get("description").getAsString();
            }
            if (description.isEmpty() && firstRun.has("execution") && firstRun.get("execution").isJsonObject()) {
                JsonObject execution = firstRun.getAsJsonObject("execution");
                if (execution.has("description") && !execution.get("description").isJsonNull()) {
                    description = execution.get("description").getAsString();
                }
            }
        }
        if (description == null) description = "";

        int total = counts.getOrDefault("total", 0);
        int passed = counts.getOrDefault("passed", 0);
        int failure = counts.getOrDefault("failure", 0);
        int queued = counts.getOrDefault("queued", 0);
        int running = counts.getOrDefault("running", 0);
        int stopped = counts.getOrDefault("stopped", 0);
        int notExecuted = counts.getOrDefault("not_executed", 0);

        // For rerun use lastRunId from execution for the view-execution link; for first run use runId
        long runIdForViewLink = runId;
        if (!isFirstRun && firstRunRaw != null) {
            Long lastRunId = getLastRunIdFromRunDetails(firstRunRaw);
            if (lastRunId != null) runIdForViewLink = lastRunId;
        }
        String liveRunUrl = TESTSIGMA_APP_BASE_URL + "/ui/td/runs/" + runIdForViewLink;
        // XLS URL uses execution.id (e.g. 13140) and run id (e.g. 300575)
        long executionResultId = runId;
        if (firstRun != null && firstRun.has("execution") && firstRun.get("execution").isJsonObject()) {
            JsonObject execution = firstRun.getAsJsonObject("execution");
            if (execution.has("id") && !execution.get("id").isJsonNull()) executionResultId = execution.get("id").getAsLong();
        } else if (firstRun != null && firstRun.has("id") && !firstRun.get("id").isJsonNull()) {
            executionResultId = firstRun.get("id").getAsLong();
        }
        String xlsReportUrl = TESTSIGMA_APP_BASE_URL + "/ui/td/execution_results/generate_xls/" + executionResultId + "/runs/" + runId;

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; font-size: 14px; color: #333; line-height: 1.5;\">");
        sb.append("<p>Hi,</p>");
        sb.append("<p>Greetings from Testsigma.</p>");
        sb.append("<p>Status : '").append(escapeHtml(testPlanName)).append("' Test Plan has been Completed</p>");
        sb.append("<p>Description :</p>");
        sb.append("<p>").append(description.isEmpty() ? "" : escapeHtml(description)).append("</p>");

        // Run Result and Re-Run #1, Re-Run #2, ... (indentation: heading left; Start Time, End Time indented one level)
        if (allRunDetails != null && !allRunDetails.isEmpty()) {
            for (int i = 0; i < allRunDetails.size(); i++) {
                if (i > 0) sb.append("<p style=\"margin-top: 0.5em;\"></p>"); // blank line between sections
                JsonObject rd = unwrapRunDetails(allRunDetails.get(i));
                if (rd == null) continue;
                String startTime = formatTimestamp(rd, "startTime", "createdDate");
                String endTime = formatTimestamp(rd, "endTime", "lastRunOn");

                String sectionTitle = (i == 0) ? "Run Result :" : "Re-Run #" + i + " :";
                sb.append("<p style=\"margin-bottom: 0.25em;\"><strong>").append(sectionTitle).append("</strong></p>");
                sb.append("<p style=\"margin-left: 1.5em; margin-top: 0.25em; margin-bottom: 0.25em;\"><strong>Status:</strong> ").append("Completed").append("</p>");
                sb.append("<p style=\"margin-left: 1.5em; margin-top: 0.25em; margin-bottom: 0.25em;\"><strong>Start Time:</strong> ").append(escapeHtml(startTime)).append("</p>");
                sb.append("<p style=\"margin-left: 1.5em; margin-top: 0.25em; margin-bottom: 0.25em;\"><strong>End Time:</strong> ").append(escapeHtml(endTime)).append("</p>");
            }
        }

        // Full mail for both first run and rerun; only difference is Run Result vs Re-Run #1, #2, ... in the run section above
        sb.append("<p><strong>Consolidated Test Cases Result Summary</strong></p>");
        sb.append("<ul>");
        sb.append("<li>Total : ").append(total).append("</li>");
        sb.append("<li>Passed : ").append(passed).append("</li>");
        sb.append("<li>Failure : ").append(failure).append("</li>");
        sb.append("<li>Queued : ").append(queued).append("</li>");
        sb.append("<li>Running : ").append(running).append("</li>");
        sb.append("<li>Stopped : ").append(stopped).append("</li>");
        sb.append("<li>Not Executed : ").append(notExecuted).append("</li>");
        sb.append("</ul>");

        if (failedTestCases != null && failedTestCases.size() > 0) {
            sb.append("<p><strong>Failed Test Cases Details</strong></p>");
            sb.append("<table border=\"1\" cellpadding=\"8\" cellspacing=\"0\" style=\"border-collapse: collapse; width: 100%; border: 1px solid #000;\">");
            sb.append("<thead><tr style=\"background: #f5f5f5; font-weight: bold;\">");
            sb.append("<th style=\"border: 1px solid #000; text-align: left;\">Name</th>");
            sb.append("<th style=\"border: 1px solid #000; text-align: left;\">Test Environment</th>");
            sb.append("<th style=\"border: 1px solid #000; text-align: left;\">Test Suite Name</th>");
            sb.append("<th style=\"border: 1px solid #000; text-align: left;\">Result</th>");
            sb.append("<th style=\"border: 1px solid #000; text-align: left;\">Message</th>");
            sb.append("<th style=\"border: 1px solid #000; text-align: left;\"></th>");
            sb.append("</tr></thead><tbody>");
            for (JsonElement el : failedTestCases) {
                JsonObject tc = el.getAsJsonObject();
                String name = getTestCaseName(tc);
                String env = getEnvironmentName(tc);
                String suiteName = getTestSuiteName(tc);
                String resultRaw = tc.has("result") && !tc.get("result").isJsonNull() ? tc.get("result").getAsString() : "";
                String resultDisplay = getResultDisplayLabel(resultRaw);
                String message = tc.has("message") && !tc.get("message").isJsonNull() ? tc.get("message").getAsString() : "";
                Long tcResultId = tc.has("id") && !tc.get("id").isJsonNull() ? tc.get("id").getAsLong() : null;
                String detailLink = tcResultId != null ? TESTSIGMA_APP_BASE_URL + "/ui/td/test_case_results/" + tcResultId : liveRunUrl;
                sb.append("<tr>");
                sb.append("<td style=\"border: 1px solid #000;\">").append(escapeHtml(name)).append("</td>");
                sb.append("<td style=\"border: 1px solid #000;\">").append(escapeHtml(env)).append("</td>");
                sb.append("<td style=\"border: 1px solid #000;\">").append(escapeHtml(suiteName)).append("</td>");
                sb.append("<td style=\"border: 1px solid #000;\">").append(escapeHtml(resultDisplay)).append("</td>");
                sb.append("<td style=\"border: 1px solid #000;\">").append(escapeHtml(message)).append("</td>");
                sb.append("<td style=\"border: 1px solid #000;\"><a href=\"").append(escapeHtml(detailLink)).append("\">click here to see</a></td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        if (isFirstRun) {
            sb.append("<p>To view Execution with RunId:").append(runId).append(" in live, <a href=\"").append(escapeHtml(liveRunUrl)).append("\">click here</a>.</p>");
        } else {
            sb.append("<p>To view Execution with RunId:").append(runIdForViewLink).append(" in live, <a href=\"").append(escapeHtml(liveRunUrl)).append("\">click here</a>.</p>");
        }
        sb.append("<p>Please click this <a href=\"").append(escapeHtml(xlsReportUrl)).append("\">url</a> to generate xls execution report.</p>");
        sb.append("<p>If you face any issues, feel free to contact us at <a href=\"mailto:support@testsigma.com\">support@testsigma.com</a> or use the Instant Chat support available at Testsigma Portal.</p>");

        sb.append("<p>Happy Automation!</p>");
        sb.append("<p>Regards,<br/><strong>Team Testsigma</strong></p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    /** Maps API result value (e.g. FAILURE, SUCCESS, STOPPED) to display label for the table. */
    private String getResultDisplayLabel(String resultRaw) {
        if (resultRaw == null || resultRaw.isEmpty()) return "Failed";
        String u = resultRaw.toUpperCase().trim();
        if (u.startsWith("FAIL")) return "Failed";
        if (u.startsWith("SUCCESS") || u.startsWith("PASS")) return "Passed";
        if (u.startsWith("STOP")) return "Stopped";
        if (u.contains("NOT_EXECUTED") || u.contains("NOTEXECUTED") || u.contains("NOT EXECUTED")) return "Not Executed";
        if (u.startsWith("QUEUED")) return "Queued";
        if (u.startsWith("RUN")) return "Running";
        return resultRaw;
    }

    private String getTestCaseName(JsonObject tc) {
        if (tc.has("testCaseName") && !tc.get("testCaseName").isJsonNull()) return tc.get("testCaseName").getAsString();
        if (tc.has("testCaseDetails") && tc.get("testCaseDetails").isJsonObject()) {
            JsonObject d = tc.getAsJsonObject("testCaseDetails");
            if (d.has("name")) return d.get("name").getAsString();
        }
        if (tc.has("testCase") && tc.get("testCase").isJsonObject()) {
            JsonObject c = tc.getAsJsonObject("testCase");
            if (c.has("name")) return c.get("name").getAsString();
        }
        return "Unknown";
    }

    /** Environment display name (e.g. "Windows Chrome (Real Device)"). Prefers executionEnvironment then environmentSettings. */
    private String getEnvironmentName(JsonObject tc) {
        if (tc.has("environmentResult") && tc.get("environmentResult").isJsonObject()) {
            JsonObject er = tc.getAsJsonObject("environmentResult");
            if (er.has("executionEnvironment") && er.get("executionEnvironment").isJsonObject()) {
                JsonObject ee = er.getAsJsonObject("executionEnvironment");
                if (ee.has("name") && !ee.get("name").isJsonNull()) return ee.get("name").getAsString();
                if (ee.has("title") && !ee.get("title").isJsonNull()) return ee.get("title").getAsString();
            }
            if (er.has("environmentSettings") && er.get("environmentSettings").isJsonObject()) {
                JsonObject es = er.getAsJsonObject("environmentSettings");
                if (es.has("title") && !es.get("title").isJsonNull()) return es.get("title").getAsString();
            }
        }
        if (tc.has("environmentSettings") && tc.get("environmentSettings").isJsonObject()) {
            JsonObject e = tc.getAsJsonObject("environmentSettings");
            if (e.has("title") && !e.get("title").isJsonNull()) return e.get("title").getAsString();
        }
        return "N/A";
    }

    private String getTestSuiteName(JsonObject tc) {
        if (tc.has("testSuiteName") && !tc.get("testSuiteName").isJsonNull()) return tc.get("testSuiteName").getAsString();
        if (tc.has("testSuite") && tc.get("testSuite").isJsonObject()) {
            JsonObject s = tc.getAsJsonObject("testSuite");
            if (s.has("name")) return s.get("name").getAsString();
        }
        return "N/A";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Parses TO EMAIL test data: comma-separated list of addresses, trimmed and empty ones removed. */
    private List<String> parseCommaSeparatedEmails(String toRaw) {
        if (toRaw == null || toRaw.isEmpty()) return Collections.emptyList();
        List<String> list = new ArrayList<>();
        for (String part : toRaw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    private void sendEmail(String host, String port, String user, String password, String from, String to, String subject, String htmlBody) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", String.valueOf(user != null && !user.isEmpty()));
        props.put("mail.smtp.starttls.enable", "true");
        Session session;
        if (user != null && !user.isEmpty() && password != null) {
            String pwd = password != null ? password : "";
            session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(user, pwd);
                }
            });
        } else {
            session = Session.getInstance(props);
        }
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(subject);
        msg.setContent(htmlBody, "text/html; charset=UTF-8");
        Transport.send(msg);
        log("Email sent successfully to " + to);
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

}
