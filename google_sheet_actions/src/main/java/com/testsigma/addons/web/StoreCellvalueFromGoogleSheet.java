package com.testsigma.addons.web;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.testsigma.addons.utils.GoogleSheetsAuthenticationUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

@Data
@Action(
        actionText = "Read the Google Sheet spreadsheetId with Cell value at rowNo,columnNo and Sheet sheetName" +
                " using credentials file json-creds-file-path and app name appName," +
                " store into a runtime variable testdata",
        description = "Read the cell value from Google Sheets by specifying sheet name and cell coordinates",
        applicationType = ApplicationType.WEB
)
public class StoreCellvalueFromGoogleSheet extends WebAction {

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

    @TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeKey;

    @TestData(reference = "sheetName")
    private com.testsigma.sdk.TestData sheetName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            logger.info("Starting process to read value from Google Sheet...");
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            logger.info("Google Sheets service initialized");
            String sheet = sheetName.getValue().toString();
            String range = sheet; // Full sheet to find by index

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId.getValue().toString(), range)
                    .execute();

            List<List<Object>> values = response.getValues();
            int row = Integer.parseInt(rowNo.getValue().toString());
            int col = Integer.parseInt(columnNo.getValue().toString());

            String cellValue;
            if (values == null || row >= values.size() || values.get(row) == null || col >= values.get(row).size()) {
                cellValue = "null"; // simulate empty cell
                logger.info("Cell is null or out of bounds, storing 'null'");
            } else {
                cellValue = values.get(row).get(col).toString();
            }

            runTimeData.setKey(runtimeKey.getValue().toString());
            runTimeData.setValue(cellValue);
            setSuccessMessage("Cell value stored successfully: " + cellValue);

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            setErrorMessage("error : " + errorMessage);
            logger.warn(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }


}
