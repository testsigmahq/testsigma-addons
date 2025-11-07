package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Data
@Action(
        actionText = "Write value testdata into TDP with TDP-ID tdp-id , column column-name, row number row-number and apikey apikey-value",
        description = "This action writes a given value into a specific cell (row number and column) of a Test Data Profile using the Testsigma API.",
        applicationType = ApplicationType.WEB
)
public class WriteValueInSpecificRowTDP extends WebAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData value;

    @TestData(reference = "tdp-id")
    private com.testsigma.sdk.TestData tdpId;

    @TestData(reference = "column-name")
    private com.testsigma.sdk.TestData columnName;

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "apikey-value")
    private com.testsigma.sdk.TestData apiKeyValue;

    private final String API_BASE_URL = "https://app.testsigma.com/api/v1/";
    private HttpClient httpClient;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution of WriteValueInTDP action using row number.");
        Result result = Result.SUCCESS;
        this.httpClient = HttpClient.newHttpClient();

        try {
            JSONObject tdpShell = getTdpShell();
            if (tdpShell == null) {
                logger.warn("TDP shell is null");
                setErrorMessage("Failed to fetch TDP shell.");
                return Result.FAILED;
            }

            JSONObject dataSet = getDataSetByRowNumber();
            if (dataSet == null) {
                logger.warn("Data set is null");
                setErrorMessage("No dataset found for row number: " + rowNumber.getValue());
                return Result.FAILED;
            }

            boolean isDataUpdated = updateDataSet(dataSet);
            if (!isDataUpdated) {
                logger.warn("Column '" + columnName.getValue() + "' not found in row number " + rowNumber.getValue() + ".");
                setErrorMessage("Column '" + columnName.getValue() + "' not found in row number " + rowNumber.getValue() + ".");
                return Result.FAILED;
            }

            JSONArray allDataSets = new JSONArray().put(dataSet);
            tdpShell.put("data", allDataSets);

            boolean isUpdateSuccessful = updateTdpOnServer(tdpShell.toString());
            if (isUpdateSuccessful) {
                logger.info("TDP shell updated successfully");
                setSuccessMessage("Successfully written the value '" + value.getValue() + "' to column '" + columnName.getValue() + "' in row number '" + rowNumber.getValue() + "'.");
            } else {
                logger.warn("TDP shell updated failed");
                setErrorMessage("Failed to update TDP on the server.");
                result = Result.FAILED;
            }

        } catch (Exception e) {
            result = Result.FAILED;
            String errorMessage = "An unexpected error occurred: " + ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
        }
        return result;
    }

    private JSONObject getTdpShell() throws URISyntaxException, IOException, InterruptedException {
        String fetchTdpUrl = API_BASE_URL + "test_data/" + tdpId.getValue();
        logger.info("Fetching TDP shell from URL: " + fetchTdpUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + apiKeyValue.getValue())
                .header("Content-Type", "application/json")
                .uri(new URI(fetchTdpUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            String error = "Failed to fetch TDP shell. Status Code: " + response.statusCode() + ", Response: " + response.body();
            setErrorMessage(error);
            return null;
        }
    }

    private JSONObject getDataSetByRowNumber() throws URISyntaxException, IOException, InterruptedException {
        logger.info("Fetching data set for row number '" + rowNumber.getValue() + "' in TDP ID: " + tdpId.getValue());

        String fetchUrl = API_BASE_URL + "test_data_sets?query=testDataProfileId:" + tdpId.getValue() + "&sort=position,asc";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + apiKeyValue.getValue())
                .header("Content-Type", "application/json")
                .uri(new URI(fetchUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String error = "Failed to fetch data sets. Status Code: " + response.statusCode() + ", Response: " + response.body();
            setErrorMessage(error);
            return null;
        }

        JSONObject responseBody = new JSONObject(response.body());
        JSONArray content = responseBody.getJSONArray("content");

        int targetIndex = Integer.parseInt(rowNumber.getValue().toString()) - 1;
        if (targetIndex < 0 || targetIndex >= content.length()) {
            logger.warn("Invalid row number '" + rowNumber.getValue() + "'. Total available rows: " + content.length());
            return null;
        }

        JSONObject selectedRow = content.getJSONObject(targetIndex);
        logger.info("Mapped row number '" + rowNumber.getValue() + "' to row name '" + selectedRow.optString("name") + "'.");
        return selectedRow;
    }

    private boolean updateDataSet(JSONObject dataSet) {
        logger.info("Updating column '" + columnName.getValue() + "' in the fetched data set.");
        JSONObject data = dataSet.getJSONObject("data");

        if (data.has(columnName.getValue().toString())) {
            data.put(columnName.getValue().toString(), value.getValue().toString());
            logger.info("Successfully updated column with value '" + value.getValue() + "'.");
            return true;
        } else {
            logger.warn("Column '" + columnName.getValue() + "' not found in the data set.");
            return false;
        }
    }

    private boolean updateTdpOnServer(String updatedTdpBody) throws URISyntaxException, IOException, InterruptedException {
        String updateTdpUrl = API_BASE_URL + "test_data/" + tdpId.getValue();
        logger.info("Sending updated TDP data to server.");

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + apiKeyValue.getValue())
                .header("Content-Type", "application/json")
                .uri(new URI(updateTdpUrl))
                .timeout(Duration.ofSeconds(30))
                .PUT(HttpRequest.BodyPublishers.ofString(updatedTdpBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 202) {
            logger.info("Successfully updated TDP on the server.");
            return true;
        } else {
            String error = "Failed to update TDP. Status Code: " + response.statusCode() + ", Response: " + response.body();
            setErrorMessage(error);
            return false;
        }
    }
}
