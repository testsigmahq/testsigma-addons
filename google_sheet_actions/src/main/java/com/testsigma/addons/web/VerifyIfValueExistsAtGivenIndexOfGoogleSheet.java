package com.testsigma.addons.web;

import com.testsigma.addons.utils.GoogleSheetsAuthenticationUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

@Data
@Action(
        actionText = "Verify if the value test-data exists in the google sheet with id spreadsheet-id, credentials" +
                " file json-creds-file-path and sheets application name app-name where row index is rowNo," +
                " column index columnNo, and Sheet name is sheetName",
        description = "Verify that cell value matches expected in Google Sheet",
        applicationType = ApplicationType.WEB
)
public class VerifyIfValueExistsAtGivenIndexOfGoogleSheet extends WebAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData expectedValue;
    @TestData(reference = "app-name")
    private com.testsigma.sdk.TestData applicationName;
    @TestData(reference = "json-creds-file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "spreadsheet-id")
    private com.testsigma.sdk.TestData spreadsheetId;

    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData rowNo;

    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData columnNo;

    @TestData(reference = "sheetName")
    private com.testsigma.sdk.TestData sheetName;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Verifying Google Sheet value...");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            logger.info("file path : " + filePath.getValue().toString());
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            String sheetRange = String.format("%s!%s%d",
                    sheetName.getValue().toString(),
                    getColumnLetter(Integer.parseInt(columnNo.getValue().toString()) - 1), // 1-based index
                    Integer.parseInt(rowNo.getValue().toString())
            );

            logger.info("Fetching cell from range: " + sheetRange);
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId.getValue().toString(), sheetRange)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                throw new Exception("No value found at the specified location.");
            }

            String actualValue = values.get(0).get(0).toString();
            logger.info("Expected: " + expectedValue.getValue().toString() + ", Actual: " + actualValue);

            if (actualValue.equals(expectedValue.getValue().toString())) {
                logger.info("Value matched.");
                setSuccessMessage("Value matched: " + actualValue);
            } else {
                setErrorMessage("Expected: " + expectedValue.getValue().toString() + ", but got: " + actualValue);
                result = com.testsigma.sdk.Result.FAILED;
            }

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage("Error : " + errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private String getColumnLetter(int columnIndex) {
        StringBuilder column = new StringBuilder();
        while (columnIndex >= 0) {
            column.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = (columnIndex / 26) - 1;
        }
        return column.toString();
    }
}
