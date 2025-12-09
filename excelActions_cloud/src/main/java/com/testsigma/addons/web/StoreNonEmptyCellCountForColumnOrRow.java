package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

@Data
@Action(actionText = "Store the count of non-empty cells from the excel file file-url in sheet sheet-name-or-index" +
        " for column-or-row-type name or index test-data into runtime variable runtime-variable",
        description = "stores the count of non-empty cells from a specified column (by name like A, B, AA)" +
                " or row (by index) in the given sheet (by name or index) of the excel file into a runtime variable",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreNonEmptyCellCountForColumnOrRow extends WebAction {

    @TestData(reference = "file-url")
    private com.testsigma.sdk.TestData fileUrlData;
    @TestData(reference = "sheet-name-or-index")
    private com.testsigma.sdk.TestData sheetNameOrIndex;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData nameOrIndex;
    @TestData(reference = "column-or-row-type", allowedValues = {"Column", "Row"})
    private com.testsigma.sdk.TestData columnOrRowType;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        logger.info("File URL: " + fileUrlData.getValue().toString());
        logger.info("Sheet name or index: " + sheetNameOrIndex.getValue().toString());
        logger.info("Column/Row name or index: " + nameOrIndex.getValue().toString());
        logger.info("Type: " + columnOrRowType.getValue().toString());

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
                String nameOrIndexStr = nameOrIndex.getValue().toString().trim();
                int calculatedCount;
                
                if(columnOrRowType.getValue().toString().equalsIgnoreCase("Column")) {
                    // Handle column: can be column number, column name (A, B, AA), or column header name
                    int columnIndex = getColumnIndex(sheet, nameOrIndexStr);
                    if (columnIndex < 0) {
                        setErrorMessage("Column not found: " + nameOrIndexStr + ". Please provide a valid column number " +
                                "(1-based), column name (A, B, AA), or column header name.");
                        return com.testsigma.sdk.Result.FAILED;
                    }
                    logger.info("Using column index: " + columnIndex + " (0-based)");
                    calculatedCount = countNonEmptyCellsInColumn(sheet, columnIndex);
                } else {
                    // It's a row index
                    try {
                        int rowIndex = Integer.parseInt(nameOrIndexStr);
                        if (rowIndex < 0) {
                            setErrorMessage("Row index must be non-negative. Provided: " + rowIndex);
                            return com.testsigma.sdk.Result.FAILED;
                        }
                        logger.info("Processing row index: " + rowIndex);
                        calculatedCount = countNonEmptyCellsInRow(sheet, rowIndex);
                    } catch (NumberFormatException e) {
                        setErrorMessage("Invalid row index format: " + nameOrIndexStr);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                }
                
                logger.info("Calculated count: " + calculatedCount);
                String variableName = runtimeVariableName.getValue().toString();
                runTimeData.setKey(variableName);
                runTimeData.setValue(String.valueOf(calculatedCount));
                logger.info("runtime data: " + runTimeData.getValue());
                
                String typeStr = columnOrRowType.getValue().toString()
                        .equalsIgnoreCase("Column") ? "column" : "row";
                setSuccessMessage("Successfully stored the count of non-empty cells from sheet '" + 
                        sheet.getSheetName() + "' " + typeStr + " '" + nameOrIndexStr +
                        "' to " + variableName + " = " + calculatedCount);
                return com.testsigma.sdk.Result.SUCCESS;
            }
        } catch (Exception e) {
            logger.info("Failed to read the excel file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to read the excel file: " + ExceptionUtils.getStackTrace(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }

    /**
     * Gets column index from various input formats:
     * 1. Column number (1-based, e.g., "1", "2", "3")
     * 2. Column name (e.g., "A", "B", "AA")
     * 3. Column header name (e.g., "Name", "Email", "Address")
     */
    private int getColumnIndex(XSSFSheet sheet, String input) {
        // Try 1: Check if it's a numeric column number (1-based)
        try {
            int columnNumber = Integer.parseInt(input);
            if (columnNumber >= 1) {
                int columnIndex = columnNumber - 1; // Convert to 0-based
                logger.info("Interpreting as column number: " + columnNumber + " (0-based index: " + columnIndex + ")");
                return columnIndex;
            }
        } catch (NumberFormatException e) {
            // Not a number, continue to other checks
        }
        
        // Try 2: Check if it's a column name (A, B, AA, etc.)
        if (isColumnName(input)) {
            int columnIndex = convertColumnNameToIndex(input);
            if (columnIndex >= 0) {
                logger.info("Interpreting as column name: " + input + " (0-based index: " + columnIndex + ")");
                return columnIndex;
            }
        }
        
        // Try 3: Search for column header name in the first row
        int headerColumnIndex = findColumnByHeaderName(sheet, input);
        if (headerColumnIndex >= 0) {
            logger.info("Interpreting as column header name: '" + input
                    + "' (0-based index: " + headerColumnIndex + ")");
            return headerColumnIndex;
        }
        
        // Not found in any format
        return -1;
    }
    
    private boolean isColumnName(String input) {
        // Check if the input contains only letters (column name like A, B, AA, etc.)
        return input.matches("^[A-Za-z]+$");
    }
    
    /**
     * Finds column index by searching for the header name in the first row
     */
    private int findColumnByHeaderName(XSSFSheet sheet, String headerName) {
        Row firstRow = sheet.getRow(0);
        if (firstRow == null) {
            logger.warn("First row is empty, cannot search for column header: " + headerName);
            return -1;
        }
        
        // Search through all cells in the first row
        for (Cell cell : firstRow) {
            if (cell != null) {
                String cellValue = cell.toString().trim();
                if (cellValue.equalsIgnoreCase(headerName.trim())) {
                    return cell.getColumnIndex();
                }
            }
        }
        
        logger.warn("Column header '" + headerName + "' not found in the first row");
        return -1;
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

    private int convertColumnNameToIndex(String columnName) {
        try {
            // Convert column name to uppercase
            columnName = columnName.toUpperCase();
            // Use CellReference to convert column name to index
            // CellReference uses 0-based indexing
            CellReference cellRef = new CellReference(columnName + "1");
            return cellRef.getCol();
        } catch (Exception e) {
            logger.warn("Error converting column name to index: " + columnName);
            return -1;
        }
    }

    private static int countNonEmptyCellsInColumn(XSSFSheet sheet, int columnIndex) {
        int cellCount = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(columnIndex);
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    cellCount++;
                }
            }
        }
        return cellCount;
    }

    private static int countNonEmptyCellsInRow(XSSFSheet sheet, int rowIndex) {
        int cellCount = 0;
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            for (Cell cell : row) {
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    cellCount++;
                }
            }
        }
        return cellCount;
    }

    private File downloadFileFromUrl(String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Processing remote URL...");
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile("excelData", ".xlsx");
                FileUtils.copyURLToFile(urlObject, tempFile);
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

