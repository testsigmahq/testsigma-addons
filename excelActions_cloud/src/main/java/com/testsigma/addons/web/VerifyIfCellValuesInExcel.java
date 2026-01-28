package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelCellUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(actionText = "Verify if cell values are equal for Excel file-path excel-path-1 and another Excel file-path excel-path-2 " +
        "at row row-index column column-index in sheet sheet-index",
        description = "Compares cell values at the specified position in two Excel files and displays differences if any",
        applicationType = ApplicationType.WEB)
public class VerifyIfCellValuesInExcel extends WebAction {

    @TestData(reference = "excel-path-1")
    private com.testsigma.sdk.TestData excelPath1;

    @TestData(reference = "excel-path-2")
    private com.testsigma.sdk.TestData excelPath2;

    @TestData(reference = "row-index")
    private com.testsigma.sdk.TestData rowIndex;

    @TestData(reference = "column-index")
    private com.testsigma.sdk.TestData columnIndex;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating Excel cell comparison");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String filePath1 = excelPath1.getValue().toString();
            String filePath2 = excelPath2.getValue().toString();
            int rowIdx = Integer.parseInt(rowIndex.getValue().toString());
            int colIdx = Integer.parseInt(columnIndex.getValue().toString());
            int sheetIdx = Integer.parseInt(sheetIndex.getValue().toString());

            logger.info("Comparing Excel files:");
            logger.info("File 1: " + filePath1);
            logger.info("File 2: " + filePath2);
            logger.info("Sheet Index: " + sheetIdx + ", Row: " + rowIdx + ", Column: " + colIdx);

            // Load both Excel files
            File excelFile1 = getExcelFile(filePath1);
            File excelFile2 = getExcelFile(filePath2);

            String cellValue1;
            String cellValue2;

            // Read cell value from first Excel file
            try (FileInputStream fis1 = new FileInputStream(excelFile1);
                 XSSFWorkbook workbook1 = new XSSFWorkbook(fis1)) {
                
                if (sheetIdx < 0 || sheetIdx >= workbook1.getNumberOfSheets()) {
                    setErrorMessage("Sheet index " + sheetIdx + " is out of range for Excel file 1. " +
                            "Valid range is 0 to " + (workbook1.getNumberOfSheets() - 1) + " (0-based index). " +
                            "File contains " + workbook1.getNumberOfSheets() + " sheet(s).");
                    return com.testsigma.sdk.Result.FAILED;
                }
                
                XSSFSheet sheet1 = workbook1.getSheetAt(sheetIdx);
                cellValue1 = ExcelCellUtils.getCellValueAsString(sheet1, rowIdx, colIdx);
                logger.info("Cell value from File 1: " + cellValue1);
            }

            // Read cell value from second Excel file
            try (FileInputStream fis2 = new FileInputStream(excelFile2);
                 XSSFWorkbook workbook2 = new XSSFWorkbook(fis2)) {
                
                if (sheetIdx < 0 || sheetIdx >= workbook2.getNumberOfSheets()) {
                    setErrorMessage("Sheet index " + sheetIdx + " is out of range for Excel file 2. " +
                            "Valid range is 0 to " + (workbook2.getNumberOfSheets() - 1) + " (0-based index). " +
                            "File contains " + workbook2.getNumberOfSheets() + " sheet(s).");
                    return com.testsigma.sdk.Result.FAILED;
                }
                
                XSSFSheet sheet2 = workbook2.getSheetAt(sheetIdx);
                cellValue2 = ExcelCellUtils.getCellValueAsString(sheet2, rowIdx, colIdx);
                logger.info("Cell value from File 2: " + cellValue2);
            }

            // Compare the cell values
            if (cellValue1.equals(cellValue2)) {
                String successMsg = "Cell values match!<br>" +
                        "Location: Sheet " + sheetIdx + ", Row " + rowIdx + ", Column " + colIdx + "<br>" +
                        "Value: '" + cellValue1 + "'";
                logger.info(successMsg.replace("<br>", " | "));
                setSuccessMessage(successMsg);
                result = com.testsigma.sdk.Result.SUCCESS;
            } else {
                String errorMsg = "Cell values DO NOT match!<br>" +
                        "Location: Sheet " + sheetIdx + ", Row " + rowIdx + ", Column " + colIdx + "<br>" +
                        "<b>File 1 Value:</b> '" + cellValue1 + "'<br>" +
                        "<b>File 2 Value:</b> '" + cellValue2 + "'";
                logger.warn(errorMsg.replace("<br>", " | ").replace("<b>", "").replace("</b>", ""));
                setErrorMessage(errorMsg);
                result = com.testsigma.sdk.Result.FAILED;
            }

        } catch (NumberFormatException e) {
            String errorMessage = "Invalid number format for row, column, or sheet index: " + e.getMessage();
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            String errorMessage = "Error comparing Excel files: " + ExceptionUtils.getMessage(e);
            logger.warn("Full error: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    /**
     * Gets the Excel file from a local path or downloads from URL
     */
    private File getExcelFile(String filePath) throws IOException {
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            logger.info("Downloading file from URL: " + filePath);
            return downloadFile(filePath);
        } else {
            logger.info("Using local file: " + filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File not found: " + filePath);
            }
            return file;
        }
    }

    /**
     * Downloads a file from URL to a temporary location
     */
    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        File tempFile = File.createTempFile("excel-compare-", "-" + fileName);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        logger.info("Downloaded file to: " + tempFile.getAbsolutePath());
        return tempFile;
    }
}
