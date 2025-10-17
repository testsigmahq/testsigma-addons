package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelUtilities;
import com.testsigma.addons.util.ExcelUtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Action(
        actionText = "Excel: Read the entire Row of the latest Excel file (.xlsx) using the Row Index row-index and Sheet Index sheet-index and store the unique values in a runtime variable variable-name",
        description = "Reads the entire row of the latest Excel file using the row index and stores unique values in a variable named testdata",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ExtractUniqueRowValuesWithSheetIndex extends WebAction {

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex_;

    @TestData(reference = "row-index")
    private com.testsigma.sdk.TestData rowIndex_;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Starting Excel row value extraction.");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        ExcelUtilities excelutil = ExcelUtilitiesFactory.create(driver, logger);

        try {
            File downloadedExcelFile = excelutil.copyFileFromDownloads("xlsx", null);
            logger.info("Downloaded Excel file: " + downloadedExcelFile.getAbsolutePath());

            Set<String> uniqueFieldValues = new LinkedHashSet<>();
            StringBuilder uniqueValuesString = new StringBuilder();

            try (FileInputStream inputStream = new FileInputStream(downloadedExcelFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

                // Validate and parse sheet index (1-based to 0-based)
                int userSheetIndex;
                try {
                    userSheetIndex = Integer.parseInt(sheetIndex_.getValue().toString());
                    if (userSheetIndex < 1) {
                        logger.warn("Sheet index must be greater than or equal to 1. Provided: " + userSheetIndex);
                        setErrorMessage("Sheet index must be greater than or equal to 1. Provided: " + userSheetIndex);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid sheet index format: " + sheetIndex_.getValue().toString());
                    setErrorMessage("Invalid sheet index format. Must be a positive integer. Provided: " + sheetIndex_.getValue().toString());
                    return com.testsigma.sdk.Result.FAILED;
                }

                int sheetIndex = userSheetIndex - 1;
                if (sheetIndex >= workbook.getNumberOfSheets()) {
                    logger.warn("Sheet index " + userSheetIndex + " is out of range. Workbook contains only " + workbook.getNumberOfSheets() + " sheet(s).");
                    setErrorMessage("Sheet index " + userSheetIndex + " is out of range. Workbook contains only " + workbook.getNumberOfSheets() + " sheet(s).");
                    return com.testsigma.sdk.Result.FAILED;
                }

                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                logger.info("Processing sheet at index: " + userSheetIndex + " (0-based: " + sheetIndex + ")");

                // Validate and parse row index (0-based)
                int rowIndex;
                try {
                    rowIndex = Integer.parseInt(rowIndex_.getValue().toString());
                    if (rowIndex < 0) {
                        logger.warn("Row index must be non-negative. Provided: " + rowIndex);
                        setErrorMessage("Row index must be non-negative. Provided: " + rowIndex);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid row index format: " + rowIndex_.getValue().toString());
                    setErrorMessage("Invalid row index format. Must be a non-negative integer. Provided: " + rowIndex_.getValue().toString());
                    return com.testsigma.sdk.Result.FAILED;
                }

                if (rowIndex > sheet.getLastRowNum()) {
                    logger.warn("Row index " + rowIndex + " is out of range. Sheet has " + (sheet.getLastRowNum() + 1) + " rows (0-based).");
                    setErrorMessage("Row index " + rowIndex + " is out of range. Sheet has " + (sheet.getLastRowNum() + 1) + " rows.");
                    return com.testsigma.sdk.Result.FAILED;
                }

                var row = sheet.getRow(rowIndex);

                if (row == null) {
                    logger.info("Row " + rowIndex + " is empty or does not exist in the sheet.");
                    setSuccessMessage("Row " + rowIndex + " is empty. Stored empty string in variable '" + testData.getValue() + "'.");
                    runTimeData.setKey(testData.getValue().toString());
                    runTimeData.setValue("");
                    return com.testsigma.sdk.Result.SUCCESS;
                }

                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    String cellValue;

                    if (cell == null) {
                        cellValue = "null";
                    } else {
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = cell.getDateCellValue().toString();
                                } else {
                                    cellValue = Double.toString(cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                cellValue = Boolean.toString(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                cellValue = cell.getCellFormula();
                                break;
                            default:
                                cellValue = "N/A";
                        }
                    }

                    uniqueFieldValues.add(cellValue);
                }

                for (String value : uniqueFieldValues) {
                    uniqueValuesString.append(value).append(",");
                }

                if (uniqueValuesString.length() > 0) {
                    uniqueValuesString.deleteCharAt(uniqueValuesString.length() - 1);
                }

                runTimeData.setKey(testData.getValue().toString());
                runTimeData.setValue(uniqueValuesString.toString());
                logger.info("Extracted unique row values: " + runTimeData.getValue());

                setSuccessMessage("Extracted unique row values and stored in variable '" +
                        testData.getValue() + "' = " + runTimeData.getValue());
            }

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.warn("Exception occurred while reading Excel row: " + errorMessage);
            setErrorMessage("Failed to extract Excel row data: " + errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }
}