package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Data
@Action(
        actionText = "Run Lighthouse audit for target-url using Google API key google-api-key with strategy options, store performance score in performance-value, accessibility score in accessibility-value, and best practices score in best-practices-value, then generate Lighthouse HTML report and upload it to Testsigma using project id projectid-value, application id applicationid-value, upload name uploadname-value, API endpoint api-endpoint-value, and Testsigma API key testsigma-api-key",
        description = "Runs Lighthouse audit, stores scores, generates report, and uploads it to Testsigma",
        applicationType = ApplicationType.WEB
)

public class RunLighthouseAuditAndUploadReport extends WebAction {

    @TestData(reference = "target-url")
    private com.testsigma.sdk.TestData targetUrl;

    @TestData(reference = "google-api-key")
    private com.testsigma.sdk.TestData apiKey;

    @TestData(reference = "options", allowedValues = {"DESKTOP", "MOBILE"})
    private com.testsigma.sdk.TestData strategy;

    @TestData(reference = "performance-value", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData performanceVal;

    @TestData(reference = "accessibility-value", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData accessibilityVal;

    @TestData(reference = "best-practices-value", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData bestPracticesVal;

    @TestData(reference = "projectid-value")
    private com.testsigma.sdk.TestData projectId;

    @TestData(reference = "applicationid-value")
    private com.testsigma.sdk.TestData applicationId;

    @TestData(reference = "uploadname-value")
    private com.testsigma.sdk.TestData uploadName;

    @TestData(reference = "api-endpoint-value")
    private com.testsigma.sdk.TestData uploadUrl;

    @TestData(reference = "testsigma-api-key")
    private com.testsigma.sdk.TestData uploadApiKey;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData performanceRt;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData accessibilityRt;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData bestPracticesRt;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(60))
            .build();

    @Override
    public Result execute() {
        File jsonFile = null;
        File htmlReport = null;

        try {
            String apiUrl = buildLighthouseApiUrl();
            JSONObject lighthouseJson = callGoogleApi(apiUrl);

            Map<String, Integer> scores = extractScores(lighthouseJson);
            storeScores(scores);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            jsonFile = writeTempFile("lighthouse_" + timestamp, ".json", lighthouseJson.toString());
            htmlReport = generateViewerReport(jsonFile, timestamp);

            uploadToTestsigma(htmlReport);

            setSuccessMessage("Lighthouse audit completed successfully. Scores: " + scores);
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Lighthouse audit failed: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Lighthouse audit failed: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;

        } finally {
            if (jsonFile != null && jsonFile.exists()) jsonFile.delete();
            if (htmlReport != null && htmlReport.exists()) htmlReport.delete();
        }
    }

    private String buildLighthouseApiUrl() {
        String encodedUrl = URLEncoder.encode(targetUrl.getValue().toString(), StandardCharsets.UTF_8);
        String strat = strategy.getValue().toString().toLowerCase();

        return String.format(
            "https://www.googleapis.com/pagespeedonline/v5/runPagespeed" +
            "?url=%s&strategy=%s&category=performance&category=accessibility&category=best-practices&key=%s",
            encodedUrl, strat, apiKey.getValue().toString()
        );
    }

    private JSONObject callGoogleApi(String apiUrl) throws Exception {
        Request request = new Request.Builder().url(apiUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("Google API error (" + response.code() + "): " + body);
            }
            return new JSONObject(body);
        }
    }

    private Map<String, Integer> extractScores(JSONObject json) {
        JSONObject categories = json
                .getJSONObject("lighthouseResult")
                .getJSONObject("categories");

        Map<String, Integer> scores = new HashMap<>();
        scores.put("performance", getScore(categories, "performance"));
        scores.put("accessibility", getScore(categories, "accessibility"));
        scores.put("best-practices", getScore(categories, "best-practices"));
        return scores;
    }

    private int getScore(JSONObject categories, String key) {
        if (!categories.has(key)) return 0;
        return (int) (categories.getJSONObject(key).getDouble("score") * 100);
    }

    private void storeScores(Map<String, Integer> scores) {
        storeRuntime(performanceVal, scores.get("performance"), performanceRt);
        storeRuntime(accessibilityVal, scores.get("accessibility"), accessibilityRt);
        storeRuntime(bestPracticesVal, scores.get("best-practices"), bestPracticesRt);
    }

    private File generateViewerReport(File jsonFile, String timestamp) throws Exception {
        String originalTab = driver.getWindowHandle();

        ((JavascriptExecutor) driver).executeScript("window.open()");
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));

        try {
            driver.get("https://googlechrome.github.io/lighthouse/viewer/");

            driver.findElement(By.xpath("//input[@type='file']"))
                    .sendKeys(jsonFile.getAbsolutePath());

            new WebDriverWait(driver, Duration.ofSeconds(60))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class,'lh-report')]")));

            Thread.sleep(1500);

            return writeTempFile(
                    "LighthouseReport_" + timestamp,
                    ".html",
                    driver.getPageSource()
            );
        } finally {
            driver.close();
            driver.switchTo().window(originalTab);
        }
    }

    private void uploadToTestsigma(File htmlFile) throws Exception {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileContent", htmlFile.getName(),
                        RequestBody.create(htmlFile, MediaType.parse("text/html")))
                .addFormDataPart("projectId", projectId.getValue().toString())
                .addFormDataPart("applicationId", applicationId.getValue().toString())
                .addFormDataPart("name", uploadName.getValue().toString())
                .addFormDataPart("uploadType", "Attachment")
                .addFormDataPart("platformType", "TestsigmaLab")
                .addFormDataPart("isPublic", "true")
                .addFormDataPart("Version", String.valueOf(System.currentTimeMillis()))
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl.getValue().toString())
                .put(body)
                .addHeader("Authorization", "Bearer " + uploadApiKey.getValue())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Upload failed: " + response.code());
            }
        }
    }

    private File writeTempFile(String name, String ext, String content) throws IOException {
        File file = File.createTempFile(name, ext);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private void storeRuntime(com.testsigma.sdk.TestData key, int value, com.testsigma.sdk.RunTimeData rt) {
        if (key != null && rt != null) {
            rt.setKey(key.getValue().toString());
            rt.setValue(String.valueOf(value));
        }
    }
}
