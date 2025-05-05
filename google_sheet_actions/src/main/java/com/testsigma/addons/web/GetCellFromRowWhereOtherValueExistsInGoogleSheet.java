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
        actionText = "Get cell row and column of value TargetValue in same row where SearchKey exists from Google" +
                " Sheet with id spreadsheet-id, credentials file json-creds-file-path, app-name appName, in sheet" +
                " SheetName. Store row in rowVar and column in colVar",
        description = "Finds the row that contains SearchKey, then finds TargetValue in that row and returns row" +
                " and column number from Google Sheets",
        applicationType = ApplicationType.WEB
)
public class GetCellFromRowWhereOtherValueExistsInGoogleSheet extends WebAction {

    @TestData(reference = "spreadsheet-id")
    private com.testsigma.sdk.TestData spreadsheetId;
    @TestData(reference = "json-creds-file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "appName")
    private com.testsigma.sdk.TestData applicationName;
    @TestData(reference = "SearchKey")
    private com.testsigma.sdk.TestData searchKey;
    @TestData(reference = "TargetValue")
    private com.testsigma.sdk.TestData targetValue;
    @TestData(reference = "SheetName")
    private com.testsigma.sdk.TestData sheetName;
    @TestData(reference = "rowVar", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData rowVar;
    @TestData(reference = "colVar", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData colVar;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData2;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            String range = sheetName.getValue().toString();

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId.getValue().toString(), range)
                    .execute();
            logger.info("response: " + response);
            List<List<Object>> rows = response.getValues();
            logger.info("Fetching cell from range: " + range);
            boolean foundSearchKey = false;

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                List<Object> row = rows.get(rowIndex);
                boolean rowHasSearchKey = row.stream()
                        .anyMatch(cell -> searchKey.getValue().toString().equalsIgnoreCase(cell.toString()));

                if (rowHasSearchKey) {
                    foundSearchKey = true;
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        if (targetValue.getValue().toString().equalsIgnoreCase(row.get(colIndex).toString())) {
                            runTimeData1.setKey(rowVar.getValue().toString());
                            runTimeData1.setValue(String.valueOf(rowIndex + 1));

                            runTimeData2.setKey(colVar.getValue().toString());
                            runTimeData2.setValue(String.valueOf(colIndex + 1));
                            logger.info("Found TargetValue '" + targetValue.getValue() +
                                    "' at Row: " + rowIndex + 1 + ", Column: " + colIndex + 1);
                            setSuccessMessage("Target value '" + targetValue.getValue() +
                                    "' found at Row: " + rowIndex + 1 + ", Column: " + colIndex + 1);
                            return result;
                        }
                    }
                }
            }

            if (!foundSearchKey) {
                logger.info("SearchKey '" + searchKey.getValue() + "' not found in any row.");
                setErrorMessage("SearchKey '" + searchKey.getValue() + "' not found in any row.");
            } else {
                logger.info("TargetValue '" + targetValue.getValue() + "' not found in row with SearchKey.");
                setErrorMessage("TargetValue '" + targetValue.getValue() + "' not found in row with SearchKey.");
            }
            result = com.testsigma.sdk.Result.FAILED;

        } catch (Exception e) {
            setErrorMessage(ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }
}

