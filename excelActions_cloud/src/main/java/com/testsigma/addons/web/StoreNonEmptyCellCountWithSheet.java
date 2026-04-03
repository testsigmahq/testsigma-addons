package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelCellUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

@Data
@Action(actionText = "Store the count of test-data from the excel file file-url in sheet sheet-name-or-index " +
        "into runtime variable runtime-variable",
        description = "stores the count of non-empty cells, rows, or columns from the specified sheet" +
                " (by name or index) of the excel file into a runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreNonEmptyCellCountWithSheet extends WebAction {

    @TestData(reference = "test-data", allowedValues = {"non-empty cells", "rows", "columns"})
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "file-url")
    private com.testsigma.sdk.TestData fileUrlData;
    @TestData(reference = "sheet-name-or-index")
    private com.testsigma.sdk.TestData sheetNameOrIndex;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        logger.info("File URL: " + fileUrlData.getValue().toString());
        logger.info("Sheet name or index: " + sheetNameOrIndex.getValue().toString());

        try {
            File excelFile = downloadFileFromUrl(fileUrlData.getValue().toString());
            logger.info("Opening workbook");
            try (FileInputStream inputStream = new FileInputStream(excelFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                
                logger.info("Successfully opened workbook");
                
                // Get sheet by name or index
                XSSFSheet sheet = getSheetByNameOrIndex(workbook, sheetNameOrIndex.getValue().toString());
                if (sheet == null) {
                    setErrorMessage("Sheet not found. Please check the sheet name or index.");
                    return com.testsigma.sdk.Result.FAILED;
                }
                
                logger.info("Processing sheet: " + sheet.getSheetName());
                String countType = testData.getValue().toString();
                logger.info("Count type: " + countType);
                int calculatedCount = calculateExcelDataCount(countType, sheet);
                logger.info("Calculated count: " + calculatedCount);
                
                String variableName = runtimeVariableName.getValue().toString();
                runTimeData.setKey(variableName);
                runTimeData.setValue(String.valueOf(calculatedCount));
                logger.info("runtime data: " + runTimeData.getValue());
                
                setSuccessMessage("Successfully stored the count of non-empty " + countType + " from sheet '" + 
                        sheet.getSheetName() + "' to " + variableName + " = " + calculatedCount);
                return com.testsigma.sdk.Result.SUCCESS;
            }
        } catch (Exception e) {
            logger.info("Failed to read the excel file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to read the excel file: " + ExceptionUtils.getStackTrace(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }

    private XSSFSheet getSheetByNameOrIndex(XSSFWorkbook workbook, String sheetNameOrIndex) {
        // Try to parse as integer (sheet index)
        try {
            int userSheetIndex = Integer.parseInt(sheetNameOrIndex);
            if (userSheetIndex < 1) {
                logger.warn("Sheet index must be greater than or equal to 1. Provided: " + userSheetIndex);
                return null;
            }
            int sheetIndex = userSheetIndex - 1; // Convert to 0-based
            if (sheetIndex >= workbook.getNumberOfSheets()) {
                logger.warn("Sheet index " + userSheetIndex + " is out of range. Workbook contains only " + 
                        workbook.getNumberOfSheets() + " sheet(s).");
                return null;
            }
            logger.info("Using sheet index: " + userSheetIndex);
            return workbook.getSheetAt(sheetIndex);
        } catch (NumberFormatException e) {
            // Not a number, treat as sheet name
            logger.info("Using sheet name: " + sheetNameOrIndex);
            XSSFSheet sheet = workbook.getSheet(sheetNameOrIndex);
            if (sheet == null) {
                logger.warn("Sheet with name '" + sheetNameOrIndex + "' not found.");
            }
            return sheet;
        }
    }

    public static int calculateExcelDataCount(String countType, XSSFSheet sheet) {
        int count = 0;

        if (countType.equalsIgnoreCase("non-empty cells")) {
            count = countNonEmptyCells(sheet);
        } else if (countType.equalsIgnoreCase("rows")) {
            count = countNonEmptyRows(sheet);
        } else if (countType.equalsIgnoreCase("columns")) {
            count = countMaxColumns(sheet);
        }

        return count;
    }

    private static int countNonEmptyCells(XSSFSheet sheet) {
        int cellCount = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            if (sheet.getRow(i) != null) {
                for (int j = 0; j < sheet.getRow(i).getPhysicalNumberOfCells(); j++) {
                    if (sheet.getRow(i).getCell(j) != null && 
                        !sheet.getRow(i).getCell(j).toString().trim().isEmpty()) {
                        cellCount++;
                    }
                }
            }
        }
        return cellCount;
    }

    private static int countNonEmptyRows(XSSFSheet sheet) {
        int rowCount = 0;
        for (Row row : sheet) {
            boolean hasData = false;
            for (Cell cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (hasData) rowCount++;
        }
        return rowCount;
    }

    private static int countMaxColumns(XSSFSheet sheet) {
        int maxColumnCount = 0;
        for (Row row : sheet) {
            int currentColumnCount = 0;
            for (Cell cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    currentColumnCount = cell.getColumnIndex() + 1; // +1 because column index is zero-based
                }
            }
            if (currentColumnCount > maxColumnCount) {
                maxColumnCount = currentColumnCount;
            }
        }
        return maxColumnCount;
    }

    private File downloadFileFromUrl(String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Processing remote URL...");
                File tempFile = ExcelCellUtils.downloadUrlToTempFile(url);
                logger.info("Temporary file created: " + tempFile.getName() + " at path: " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Processing local file path...");
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Error accessing file: " + url);
            throw new RuntimeException("Unable to access the specified Excel file." +
                    " Please check the provided URL or file path.");
        }
    }
}

