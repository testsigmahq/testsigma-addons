package com.testsigma.addons.web;

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
@Action(actionText = "Get cell row and column of value TargetValue in same row where SearchKey1 and SearchKey2 exist" +
        " from Google Sheet sheetId, sheet SheetName, application appName and credentials file json-creds-file-path," +
        " store in runtime variables rowVar and colVar",
        description = "Finds the row that contains both SearchKey1 and SearchKey2, then finds TargetValue in that" +
                " row and returns row and column number", applicationType = ApplicationType.WEB)
public class GetCellFromRowWhereTwoValuesExist extends WebAction {

    @TestData(reference = "sheetId")
    private com.testsigma.sdk.TestData sheetId;
    @TestData(reference = "json-creds-file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "appName")
    private com.testsigma.sdk.TestData applicationName;
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
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData2;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            logger.info("file path : " + filePath.getValue().toString());
            Sheets sheetsService = GoogleSheetsAuthenticationUtil.getSheetsService(filePath.getValue().toString(),
                    applicationName.getValue().toString());
            logger.info("sheetId: " + sheetId.getValue().toString());
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

            for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
                List<Object> row = values.get(rowIndex);
                boolean hasKey1 = row.stream().anyMatch(cell ->
                        cell.toString().equalsIgnoreCase(searchKey1.getValue().toString()));
                boolean hasKey2 = row.stream().anyMatch(cell ->
                        cell.toString().equalsIgnoreCase(searchKey2.getValue().toString()));
                if (hasKey1 && hasKey2) {
                    logger.info("rowIndex: " + rowIndex + " has both keys");
                    for (int colIndex = 0; colIndex < row.size(); colIndex++) {
                        String value = row.get(colIndex).toString();
                        if (value.equalsIgnoreCase(targetValue.getValue().toString())) {
                            runTimeData1.setKey(rowVar.getValue().toString());
                            runTimeData1.setValue(String.valueOf(rowIndex + 1));
                            runTimeData2.setKey(colVar.getValue().toString());
                            runTimeData2.setValue(String.valueOf(colIndex + 1));
                            logger.info("Found TargetValue '" + targetValue.getValue() +
                                    "' at Row: " + rowIndex + 1 + ", Column: " + colIndex + 1);
                            setSuccessMessage("Target value '" + targetValue.getValue().toString() + "' found at Row: "
                                    + rowIndex + 1 + ", Column: " + colIndex + 1);
                            return result;
                        }
                    }
                }
            }
            logger.info("No row found containing both SearchKey1 and SearchKey2.");
            setErrorMessage(String.format("No row found containing %s, %s and %s.", searchKey1.getValue().toString(),
                    searchKey2.getValue().toString(), targetValue.getValue().toString()));
            result = com.testsigma.sdk.Result.FAILED;

        } catch (Exception e) {
            logger.debug("Error occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error occurred: " + ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }
}
