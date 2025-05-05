package com.testsigma.addons.web;

import com.testsigma.addons.utils.GoogleSheetsAuthenticationUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

@Data
@Action(actionText = "If both values exist in same row, Get cell row and column of value TargetValue in same row" +
        " where SearchKey1 and SearchKey2 exist from Google Sheet sheetId, sheet SheetName and credentials file" +
        " json-creds-file-path and application name appName, store in runtime variables rowVar and colVar",
        description = "Finds the row that contains both SearchKey1 and SearchKey2, then finds TargetValue in that" +
                " row and returns row and column number",
        applicationType = ApplicationType.WEB,
        actionType = StepActionType.IF_CONDITION)
public class IfTwoValuesExistGetCellFromRowWhere extends WebAction {

    @TestData(reference = "sheetId")
    private com.testsigma.sdk.TestData sheetId;
    @TestData(reference = "SearchKey1")
    private com.testsigma.sdk.TestData searchKey1;
    @TestData(reference = "SearchKey2")
    private com.testsigma.sdk.TestData searchKey2;
    @TestData(reference = "TargetValue")
    private com.testsigma.sdk.TestData targetValue;
    @TestData(reference = "SheetName")
    private com.testsigma.sdk.TestData sheetName;
    @TestData(reference = "rowVar", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData rowVar;
    @TestData(reference = "colVar", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData colVar;
    @TestData(reference = "json-creds-file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "appName")
    private com.testsigma.sdk.TestData applicationName;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData2;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            logger.info("initializing Google Sheets service...");
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            logger.info("google sheets service initialized");
            String range = sheetName.getValue().toString() + "!A:Z";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(sheetId.getValue().toString(), range)
                    .execute();
            logger.info("response: " + response);
            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                setErrorMessage("No data found in the sheet.");
                return com.testsigma.sdk.Result.FAILED;
            }
            logger.info("values size : " + values.size());
            boolean keysFoundInRow = false;

            for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                List<Object> row = values.get(rowIndex);
                boolean hasKey1 = row.stream().anyMatch(cell ->
                        cell.toString().equalsIgnoreCase(searchKey1.getValue().toString()));
                boolean hasKey2 = row.stream().anyMatch(cell ->
                        cell.toString().equalsIgnoreCase(searchKey2.getValue().toString()));

                if (hasKey1 && hasKey2) {
                    logger.info("rowIndex: " + rowIndex + " has both keys");
                    keysFoundInRow = true;
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        String cellValue = row.get(colIndex).toString();
                        if (cellValue.equalsIgnoreCase(targetValue.getValue().toString())) {

                            runTimeData1.setKey(rowVar.getValue().toString());
                            runTimeData1.setValue(String.valueOf(rowIndex + 1));

                            runTimeData2.setKey(colVar.getValue().toString());
                            runTimeData2.setValue(String.valueOf(colIndex + 1));

                            setSuccessMessage("Target value '" + targetValue.getValue().toString() +
                                    "' found at Row: " + rowIndex + 1 + ", Column: " + colIndex + 1);
                            return result;
                        }
                    }
                }
            }
            // If we reach here, it means we didn't find the target value in the same row as both keys
            if (!keysFoundInRow) {
                setErrorMessage("Neither SearchKey1 '" + searchKey1.getValue().toString() + "' nor SearchKey2 '"
                        + searchKey2.getValue().toString() + "' were found in the same row.");
                result = com.testsigma.sdk.Result.FAILED;
            } else {
                setErrorMessage("TargetValue '" + targetValue.getValue().toString() +
                        "' not found in row where both keys exist.");
                result = com.testsigma.sdk.Result.FAILED;
            }
        } catch (Exception e) {
            logger.debug("Error occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}