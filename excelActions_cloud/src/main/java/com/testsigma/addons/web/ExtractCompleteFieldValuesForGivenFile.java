package com.testsigma.addons.web;

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
import com.testsigma.addons.util.PdfAndDocUtilities;

import java.io.File;
import java.io.FileInputStream;

@Data
@Action(actionText = "Excel: Read the entire Row of the Excel fileName using the Row Index and store it in a "
        + "variable named testdata",
        description = "Read the entire Row of the latest Excel file using the Row number and store it in a "
                + "variable named testdata",
        applicationType = ApplicationType.WEB, useCustomScreenshot = false)
public class ExtractCompleteFieldValuesForGivenFile extends WebAction {

    @TestData(reference = "fileName")
    private com.testsigma.sdk.TestData FileName;
    @TestData(reference = "Index")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData2;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        PdfAndDocUtilities documentutil = new PdfAndDocUtilities(driver, logger);
        logger.info("line 42");
        try {

            logger.info("line 45");
            logger.info("fileName: " + getFileName().getValue().toString());
            String updatedFileName = getFileName().getValue().toString();
            if (updatedFileName.endsWith(".xlsx")) {
                updatedFileName = updatedFileName.substring(0, updatedFileName.length() - 5);
            }
            File downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx", updatedFileName);
            StringBuilder entireFieldValues = new StringBuilder();
            logger.info("entireFieldValues: " + entireFieldValues);
            try (FileInputStream inputStream = new FileInputStream(downloadedExcelFile)) {
                // Load the workbook and get the first sheet
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                logger.info("Case Row");
                int rowIndex = Integer.parseInt(testData1.getValue().toString());
                var row = sheet.getRow(rowIndex);
                if (row != null) {
                    logger.info("Values in row " + (rowIndex + 1) + ":");

                    // Iterate over all cells in the row
                    for (Cell cell : row) {
                        String CellValueForRow;
                        // Check cell type and print the value
                        switch (cell.getCellType()) {
                            case STRING:
                                CellValueForRow = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    CellValueForRow = cell.getDateCellValue().toString();
                                } else {
                                    CellValueForRow = Double.toString(cell.getNumericCellValue());
                                }
                                break;
                            case BOOLEAN:
                                CellValueForRow = Boolean.toString(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                CellValueForRow = cell.getCellFormula();
                                break;
                            default:
                                CellValueForRow = "N/A";
                        }
                        entireFieldValues.append(CellValueForRow);
                        entireFieldValues.append(",");
                    }
                } else {
                    logger.info("Row " + (rowIndex + 1) + " is empty or this is the last column with data");
                }
            }
            if (entireFieldValues.length() > 0) {
                entireFieldValues.deleteCharAt(entireFieldValues.length() - 1);
            }

            logger.info("Storing values");
            runTimeData.setKey(testData2.getValue().toString());
            runTimeData.setValue(entireFieldValues.toString());

            setSuccessMessage("Extracted the Complete Row Values and Stored it in variable "
                    + testData2.getValue().toString() + " = " + runTimeData.getValue());

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        }
        return result;
    }
}
