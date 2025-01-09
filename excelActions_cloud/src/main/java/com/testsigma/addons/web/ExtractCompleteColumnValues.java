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
@Action(actionText = "Excel: Read the entire Column of the latest Excel file (.xlsx) using the Column Index and store it in a " +
        "variable named testdata",
        description = "Reads the entire column of the latest Excel file using the column number and store it in a " +
                "variable named testdata",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ExtractCompleteColumnValues extends WebAction {
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
        try {

            File downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx", null);

            StringBuffer entireFieldValues = new StringBuffer();

            try (FileInputStream inputStream = new FileInputStream(downloadedExcelFile)) {
                // Load the workbook and get the first sheet
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                logger.info("Case Column");
                int columnIndex = Integer.parseInt(testData1.getValue().toString());
                for (int rowIndex1 = 0; rowIndex1 <= sheet.getLastRowNum(); rowIndex1++) {
                    var row1 = sheet.getRow(rowIndex1);
                    if (row1 != null) {
                        Cell cell = row1.getCell(columnIndex);
                        if (cell != null) {
                            String CellValue;

                            switch (cell.getCellType()) {
                                case STRING:
                                    CellValue = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        CellValue = cell.getDateCellValue().toString();
                                    } else {
                                        CellValue = Double.toString(cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    CellValue = Boolean.toString(cell.getBooleanCellValue());
                                    break;
                                case FORMULA:
                                    CellValue = cell.getCellFormula();
                                    break;
                                default:
                                    CellValue = "N/A";
                            }
                            entireFieldValues.append(CellValue);
                            logger.info("Values in row " + (rowIndex1 + 1) + ":" + CellValue);
                            entireFieldValues.append(",");
                        } else {
                            logger.info("Ignoring next cells as we found empty cell");
                            break;
                        }
                    }
                }
            }
            logger.info(entireFieldValues.toString());
            if (entireFieldValues.length() > 0) {
                entireFieldValues.deleteCharAt(entireFieldValues.length() - 1);
            }

            runTimeData.setKey(testData2.getValue().toString());
            runTimeData.setValue(entireFieldValues.toString());

            setSuccessMessage("Extracted the Complete Column Values and Stored it in variable "
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
