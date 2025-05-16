package com.testsigma.addons.web;

import com.testsigma.addons.util.PdfAndDocUtilities;
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
        actionText = "Excel: Read the entire Column of the latest Excel file (.xlsx) using the Column Index column-index and Sheet Index sheet-index and store the unique values in a runtime variable variable-name",
        description = "Reads the entire column of the latest Excel file using the column number and store it in a variable named testdata",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ExtractUniqueColumnValuesWithSheetIndex extends WebAction {
    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex_;

    @TestData(reference = "column-index")
    private com.testsigma.sdk.TestData columnIndex_;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution for extracting unique column values.");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        PdfAndDocUtilities documentutil = new PdfAndDocUtilities(driver, logger);

        try {
            File downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx", null);
            logger.info("Downloaded Excel file: " + downloadedExcelFile.getAbsolutePath());

            Set<String> uniqueFieldValues = new LinkedHashSet<>();
            StringBuffer uniqueValuesString = new StringBuffer();

            try (FileInputStream inputStream = new FileInputStream(downloadedExcelFile)) {
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

                // Validate and parse column index (0-based)
                int columnIndex;
                try {
                    columnIndex = Integer.parseInt(columnIndex_.getValue().toString());
                    if (columnIndex < 0) {
                        logger.warn("Column index must be non-negative. Provided: " + columnIndex);
                        setErrorMessage("Column index must be non-negative. Provided: " + columnIndex);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid column index format: " + columnIndex_.getValue().toString());
                    setErrorMessage("Invalid column index format. Must be a non-negative integer. Provided: " + columnIndex_.getValue().toString());
                    return com.testsigma.sdk.Result.FAILED;
                }

                // Check if column index is within range for the sheet
                int maxColumns = sheet.getRow(0) != null ? sheet.getRow(0).getLastCellNum() : 0;
                if (columnIndex >= maxColumns) {
                    logger.warn("Column index " + columnIndex + " is out of range. Sheet has " + maxColumns + " columns (0-based).");
                    setErrorMessage("Column index " + columnIndex + " is out of range. Sheet has " + maxColumns + " columns.");
                    return com.testsigma.sdk.Result.FAILED;
                }

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    var row = sheet.getRow(rowIndex);
                    if (row != null) {
                        Cell cell = row.getCell(columnIndex);
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
                    } else {
                        logger.info("Row " + (rowIndex + 1) + " is empty.");
                    }
                }
            }

            for (String value : uniqueFieldValues) {
                uniqueValuesString.append(value).append(",");
            }

            if (uniqueValuesString.length() > 0) {
                uniqueValuesString.deleteCharAt(uniqueValuesString.length() - 1); // Remove trailing comma
            }

            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(uniqueValuesString.toString());
            logger.info("Extracted unique column values: " + uniqueValuesString);

            setSuccessMessage("Extracted unique column values and stored in variable '" + testData.getValue().toString() + "' = " + runTimeData.getValue());
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.warn("Error during execution: " + errorMessage);
            setErrorMessage("Failed to extract column values: " + errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }
}