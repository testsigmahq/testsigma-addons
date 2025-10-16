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

@Data
@Action(actionText = "Excel: Read the entire Row of the latest Excel file (.xlsx) using the Row Index row-index and Sheet Index sheet-index and store it in a " +
        "variable named testdata",
        description = "Reads the entire Row of the latest Excel file using the Row index and store it in a " +
                "variable named testdata",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ExtractCompleteRowValuesWithSheetIndex extends WebAction {
    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex_;
    @TestData(reference = "row-index")
    private com.testsigma.sdk.TestData rowIndex_;
    @TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        ExcelUtilities excelutil = ExcelUtilitiesFactory.create(driver, logger);
        try {

            File downloadedExcelFile = excelutil.copyFileFromDownloads("xlsx", null);
            logger.info("Downloaded Excel file: " + downloadedExcelFile.getAbsolutePath());

            StringBuffer entireFieldValues = new StringBuffer();

            try (FileInputStream inputStream = new FileInputStream(downloadedExcelFile)) {
                // Load the workbook
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

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

                // Iterate over all cells in the row
                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    String cellValueForRow;

                    if (cell == null) {
                        cellValueForRow = "null"; // Use "null" as a placeholder for empty cells
                    } else {
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValueForRow = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cellValueForRow = cell.getDateCellValue().toString();
                                } else {
                                    cellValueForRow = Double.toString(cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                cellValueForRow = Boolean.toString(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                cellValueForRow = cell.getCellFormula();
                                break;
                            default:
                                cellValueForRow = "N/A";
                            }
                        }
                        entireFieldValues.append(cellValueForRow);
                        logger.info("Cell Value: " + cellValueForRow);
                        entireFieldValues.append(",");
                    }
                }

            logger.info(entireFieldValues.toString());
            if (entireFieldValues.length() > 0) {
                entireFieldValues.deleteCharAt(entireFieldValues.length() - 1);
            }

            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(entireFieldValues.toString());
            logger.info("Extracted row values: " + entireFieldValues.toString());

            setSuccessMessage("Extracted the Complete Row Values and Stored it in variable " + testData.getValue().toString() + " = " + runTimeData.getValue());

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        }

        return result;
    }
}