package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelCellUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Action(actionText = "Write the data data-value into the Excelfile excel-path with Cell value rowNo,columnNo in new sheet name sheet-name and store the path in runtime variable variable-name(supports upload section)",
        description = "Write the given data value into a specified row and column of a sheet if it exists, or create a new sheet in the Excel file and store the updated file path in a runtime variable",
        applicationType = ApplicationType.WEB)
public class WriteCellValueInNewSheetFilePath extends WebAction {

    @TestData(reference = "excel-path")
    private com.testsigma.sdk.TestData excelPath;

    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "data-value")
    private com.testsigma.sdk.TestData testData3;

    @TestData(reference = "sheet-name")
    private com.testsigma.sdk.TestData testData4;

    // @TestData(reference = "variable-name", isRuntimeVariable = true)
    // private com.testsigma.sdk.TestData variableName;

    // @RunTimeData
    // private com.testsigma.sdk.RunTimeData runTimeData;

    public static void main(String[] args) {
        WriteCellValueInNewSheetFilePath writeCellValueInNewSheetFilePath = new WriteCellValueInNewSheetFilePath();
        writeCellValueInNewSheetFilePath.setExcelPath(new com.testsigma.sdk.TestData("https://s3.amazonaws.com/permanent-attachments-production.testsigma.com/2817/uploads/1478/6264/File_Example_10_%281%29.xlsx?response-content-type=application%2Foctet-stream&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEKj%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCXVzLWVhc3QtMSJIMEYCIQDXakLi9G%2BqPl%2FUFinIeYva70rh2KcKPJu%2FC5D14FEXHgIhAKys21OtsBPd%2F2wpmJ3fCN44hq1iM3p3t7MKVsJOVgk0KsoFCHAQAxoMMTM0ODYzOTU2NzIxIgwqW%2FYSppuwRoqAEc4qpwWgkI5shnLsjGi2neumNwBM8JswYTMuuumSl9NLvMPbXeQ%2Fj0DrjMvUUfyvQNsIh%2FrEWY92jGejfxk0oyBDOBVGaE9ldXn79XF9CNGUqleTd9515regrKQu4nAk80gaQIuYznNhOQPWD1xc6n9S%2FLwclbBdetq6r0gLSiHiPbjQOjil8HoGrh%2BboZwBrfESd%2FmiFXyr36Wn9b2grHI3cyR%2F9qnW0ygbAGiM9qCttt9iuZ7Jb9dpIQnfSsyuMR8suqn4tdUjr2gqeksvwlDpX5T19wYyxIesImWaqf4d02JOI%2F4PukeOW%2Fo0hdz%2BJerlPDZQdPQSXCgrt%2BUOeYY%2F8BuPo7rYLgCUITDq7NDZz87RkKBV7x8i4hVDcWCDlvj8b39LdYqfOARaz1Lqz5t4Iw9Y6IhIk5CPq5pgl%2BL%2BeG3JjQ4mRYlGBf3ftdmWyzkb5oUvpSTC4Dxd5m7fAbxo327%2BWvXr%2B5VWPN92eV7l3ddCV8vGs8qC9VMkbygjbgRG6AwU5L0n0GTz9udFLf2nE%2F8GcYLMbJacR%2FvC%2BgS%2BQYfPc3LTiZpvyCRA5QwdpdOFrfvW9WvMOqu7Z9c2uvDEXEFC%2F9ViyuhqE0K9lLCobLWMw8wEsI7JmzHf7VW3yZs%2BInG%2FeWOtuVoBbDAOYPZlXam37Gcxptq2vt9b%2Fy7h2Cv6yZ4sKuLWcdf3u0TXBcJm70tLVSyx%2BjOZZ7skIcu0pQHsJxbvVY7pimgsZzts%2BKGDA6ocip0UbBlFiyM4rTmJbTWizRJMLlXmtScsgmD73nLiyS9JDhMKeNmqKZ3Nm%2BD%2Blernu%2FmP7YmxFitLojD3J8LmiUpxCm7c0fHmsrQOJ1vMrzVp4fKvPOtxsHrARDn50JXFHkZslWE78Xkn3p%2Bx9dxjhbgfbHSmMJeLus4GOo8B7f64maNE7S2YOvj4sTbo%2FAPXHpsrh6BYYILC004rSKJwRIl8gkxwITH2r%2FZQhdGeH1A1ZvqaCiiixjfRWVl5jhthdCLeT4KTqJXLH2wRdy9lfDRp%2BFkaJpYX29h%2F0RYNs29UiwkGyNGHArkR1cEOlJWAFNBv2p5L1EWK%2FA8TNDw92Bxzc2IuuN2nGvu%2FBnE%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260402T181849Z&X-Amz-SignedHeaders=host&X-Amz-Credential=ASIAR6ZUEVLYWVLHGLDF%2F20260402%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Expires=10800&X-Amz-Signature=e9e02fcf2f7230c88f1b98ee751decc379e737620d2b875829b9f0eb540211fc"));
        writeCellValueInNewSheetFilePath.setTestData1(new com.testsigma.sdk.TestData("1"));
        writeCellValueInNewSheetFilePath.setTestData2(new com.testsigma.sdk.TestData("1"));
        writeCellValueInNewSheetFilePath.setTestData3(new com.testsigma.sdk.TestData("aKHIL"));
        writeCellValueInNewSheetFilePath.setTestData4(new com.testsigma.sdk.TestData("Sheet1"));
        writeCellValueInNewSheetFilePath.execute();
    }


    @Override
    public com.testsigma.sdk.Result execute(){
        //Your Awesome code starts here
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String filePath = excelPath.getValue().toString();
        int rowIndex = Integer.parseInt(testData1.getValue().toString());;
        int columnIndex = Integer.parseInt(testData2.getValue().toString());;
        String newsheetName = testData4.getValue().toString();

        String data = testData3.getValue().toString();

        File excelFile = null;

        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            try {
                logger.info("Inside if");
                excelFile = downloadFile(filePath);
                logger.info("Downloaded excel file at: " + excelFile.getAbsolutePath());
            } catch (IOException e) {
                logger.warn("IO Exception: " + ExceptionUtils.getStackTrace(e));
                setErrorMessage("IO Exception: " + ExceptionUtils.getMessage(e));
                result = com.testsigma.sdk.Result.FAILED;
                return  result;
            }
        } else {
            excelFile = new File(filePath);
            logger.info("Inside else");
            logger.info("Downloaded excel file  at: " + excelFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet newSheet = workbook.getSheet(newsheetName);
            if (newSheet == null) {
                newSheet = workbook.createSheet(newsheetName);
            }
            Row row = newSheet.getRow(rowIndex);
            if (row == null) {
                row = newSheet.createRow(rowIndex);
            }
            Cell cell = row.getCell(columnIndex);
            if (cell == null) {
                cell = row.createCell(columnIndex);
            }
            cell.setCellValue(data);
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
            } catch (IOException e) {
                String errorMessage = ExceptionUtils.getStackTrace(e);
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage(errorMessage);
                logger.warn(errorMessage);
            }

            System.out.println("Excel file path: " + excelFile.getAbsolutePath());
            // runTimeData.setKey(variableName.getValue().toString());
            // runTimeData.setValue(excelFile.getAbsolutePath());
            logger.info("Data written successfully to Excel file.File path: " + excelFile.getAbsolutePath());
        } catch (IOException e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        }

        setSuccessMessage("Data written successfully to Excel file.File path: " + excelFile.getAbsolutePath());

        return result;
    }

    private File downloadFile(String fileUrl) throws IOException {
        return ExcelCellUtils.downloadUrlToTempFile(fileUrl);
    }
}