package com.testsigma.addons.web;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.testsigma.addons.utils.GoogleSheetsAuthenticationUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

@Data
@Action(
        actionText = "Write the data datavalue into Google Sheet spreadsheetId with Cell value rowNo,columnNo" +
                " and Sheet sheetName using credentials file json-creds-file-path and app name appName",
        description = "Write value to specified cell in the Google Sheet",
        applicationType = ApplicationType.WEB
)
public class WriteCellvalueToGoogleSheet extends WebAction {

    @TestData(reference = "spreadsheetId")
    private com.testsigma.sdk.TestData spreadsheetId;

    @TestData(reference = "json-creds-file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "appName")
    private com.testsigma.sdk.TestData applicationName;

    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData rowNo;

    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData columnNo;

    @TestData(reference = "datavalue")
    private com.testsigma.sdk.TestData dataValue;

    @TestData(reference = "sheetName")
    private com.testsigma.sdk.TestData sheetName;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Starting process to write value to Google Sheet...");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            logger.info("Initializing Google Sheets service...");
            logger.info("filePath : " + filePath.getValue().toString());
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            logger.info("Google Sheets service initialized");
            int row = Integer.parseInt(rowNo.getValue().toString()) - 1;
            int col = Integer.parseInt(columnNo.getValue().toString()) - 1;

            // Convert zero-based index to A1 notation
            String cellRef = sheetName.getValue().toString() + "!" + getA1Notation(row, col);

            ValueRange body = new ValueRange().setValues(List.of(List.of(dataValue.getValue().toString())));
            logger.info("Writing data to cell: " + cellRef);
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId.getValue().toString(), cellRef, body)
                    .setValueInputOption("RAW")
                    .execute();
            logger.info("Data written successfully to cell: " + cellRef);
            setSuccessMessage("Data written successfully to Google Sheet cell: " + cellRef);

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    // Convert numeric indices to A1 notation (e.g., 0,0 -> A1)
    private String getA1Notation(int row, int col) {
        StringBuilder colName = new StringBuilder();
        col++; // Make 1-based
        while (col > 0) {
            int rem = (col - 1) % 26;
            colName.insert(0, (char) (rem + 'A'));
            col = (col - 1) / 26;
        }
        return colName + String.valueOf(row + 1); // Make row 1-based
    }
}
