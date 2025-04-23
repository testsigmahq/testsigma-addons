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
        actionText = "Search value searchValue in Google Sheet with id spreadsheetId, credentials file" +
                " json-creds-file-path, app-name appName, in Sheet sheetName. Store row and column into" +
                " runtime variables rowRuntimeVar and colRuntimeVar",
        description = "Searches for a value in a Google Sheet and stores its row and column number",
        applicationType = ApplicationType.WEB
)
public class SearchValueInGoogleSheetAndStoreRowCol extends WebAction {

    @TestData(reference = "spreadsheetId")
    private com.testsigma.sdk.TestData spreadsheetId;

    @TestData(reference = "json-creds-file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "appName")
    private com.testsigma.sdk.TestData applicationName;


    @TestData(reference = "sheetName")
    private com.testsigma.sdk.TestData sheetName;

    @TestData(reference = "searchValue")
    private com.testsigma.sdk.TestData searchValue;

    @TestData(reference = "rowRuntimeVar", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData rowRuntimeVar;

    @TestData(reference = "colRuntimeVar", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData colRuntimeVar;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData1;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData2;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            logger.info("Initializing Google Sheets service...");
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            logger.info("Google Sheets service initialized");
            String range = sheetName.getValue().toString();

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId.getValue().toString(), range)
                    .execute();

            List<List<Object>> rows = response.getValues();
            logger.info("rows : " + rows.size());
            if (rows == null || rows.isEmpty()) {
                setErrorMessage("Sheet is empty or doesn't exist.");
                return com.testsigma.sdk.Result.FAILED;
            }

            boolean found = false;
            outerLoop:
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                List<Object> row = rows.get(rowIndex);
                for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                    if (searchValue.getValue().toString().equalsIgnoreCase(row.get(colIndex).toString())) {
                        runTimeData1.setKey(rowRuntimeVar.getValue().toString());
                        runTimeData1.setValue(String.valueOf(rowIndex + 1));
                        runTimeData2.setKey(colRuntimeVar.getValue().toString());
                        runTimeData2.setValue(String.valueOf(colIndex + 1));
                        logger.info("Found searchValue '" + searchValue.getValue() +
                                "' at Row: " + rowIndex + 1 + ", Column: " + colIndex + 1);
                        setSuccessMessage("successfully stored row and column of searchValue '" +
                                searchValue.getValue() + "' in runtime variable " + runTimeData1.getKey() + " = " +
                                runTimeData1.getValue() + runTimeData2.getKey() + " = " +
                                runTimeData2.getValue());
                        found = true;
                        break outerLoop;
                    }
                }
            }

            if (!found) {
                logger.info("Value '" + searchValue.getValue() + "' not found in the Google Sheet.");
                setErrorMessage("Value '" + searchValue.getValue() + "' not found in the Google Sheet.");
                result = com.testsigma.sdk.Result.FAILED;
            }
        } catch (Exception e) {
            logger.info("Error occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }
}
