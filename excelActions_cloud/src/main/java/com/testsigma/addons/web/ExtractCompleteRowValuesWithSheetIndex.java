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

        PdfAndDocUtilities documentutil = new PdfAndDocUtilities(driver, logger);
        try {

            File downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx", null);

            StringBuffer entireFieldValues = new StringBuffer();

            try (FileInputStream inputStream = new FileInputStream(downloadedExcelFile)) {
                // Load the workbook
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

                // Get the sheet index from user input, ensuring it starts from 1
                int sheetIndex = Integer.parseInt(sheetIndex_.getValue().toString()) - 1;
                if (sheetIndex < 1) {
                    result = com.testsigma.sdk.Result.FAILED;
                    logger.warn("Sheet index must be greater than 0");
                    setErrorMessage("Sheet index must be greater than 0.");
                    return result;
                }

                // Check if the sheet index is within the valid range
                if (sheetIndex >= workbook.getNumberOfSheets()) {
                    logger.warn("Sheet index " + sheetIndex + " is out of range for the workbook.");
                    result = com.testsigma.sdk.Result.FAILED;
                    setErrorMessage("Sheet index " + sheetIndex + " is out of range for the workbook.");
                    return result;
                }

                // Get the specified sheet
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                logger.info("Processing sheet at index: " + sheetIndex);

                int rowIndex = Integer.parseInt(rowIndex_.getValue().toString());
                // Check if the row index is within the valid range
                if (rowIndex < 0 || rowIndex > sheet.getLastRowNum()) {
                    logger.warn("Row index " + rowIndex + " is out of range for the sheet.");
                    result = com.testsigma.sdk.Result.FAILED;
                    setErrorMessage("Row index " + rowIndex + " is out of range for the sheet.");
                    return result;
                }

                var row = sheet.getRow(rowIndex);
                if (row != null) {
                    logger.info("Values in row " + (rowIndex + 1) + ":");

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
                } else {
                    logger.info("Row " + (rowIndex + 1) + " is empty or this is the last column with data");
                }
            }

            logger.info(entireFieldValues.toString());
            if (entireFieldValues.length() > 0) {
                entireFieldValues.deleteCharAt(entireFieldValues.length() - 1);
            }

            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(entireFieldValues.toString());

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