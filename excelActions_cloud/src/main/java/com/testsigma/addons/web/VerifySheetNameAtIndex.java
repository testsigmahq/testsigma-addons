package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelCellUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Data
@Action(actionText = "Verify sheet name sheet-name is present at index sheet-index in Excel file at file-path",
        description = "Verifies if a sheet with the given name is present at the specified index (0-based) in the Excel file",
        applicationType = ApplicationType.WEB)
public class VerifySheetNameAtIndex extends WebAction {

    @TestData(reference = "sheet-name")
    private com.testsigma.sdk.TestData sheetName;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating sheet name at index verification");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String excelFilePath = filePath.getValue().toString();
            String expectedSheetName = sheetName.getValue().toString();
            int expectedIndex = Integer.parseInt(sheetIndex.getValue().toString());

            logger.info("Excel file path: " + excelFilePath);
            logger.info("Expected sheet name: " + expectedSheetName);
            logger.info("Expected sheet index: " + expectedIndex);

            // Load the Excel file
            File excelFile = getExcelFile(excelFilePath);

            try (FileInputStream fis = new FileInputStream(excelFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                int numberOfSheets = workbook.getNumberOfSheets();

                // Check if the index is valid
                if (expectedIndex < 0 || expectedIndex >= numberOfSheets) {
                    String errorMsg = "Sheet index " + expectedIndex + " is out of range.<br>" +
                            "Valid index range: 0 to " + (numberOfSheets - 1) + "<br>" +
                            "Total sheets in file: " + numberOfSheets;
                    logger.warn(errorMsg.replace("<br>", " | "));
                    setErrorMessage(errorMsg);
                    return com.testsigma.sdk.Result.FAILED;
                }

                // Get the actual sheet name at the specified index
                String actualSheetName = workbook.getSheetName(expectedIndex);
                logger.info("Actual sheet name at index " + expectedIndex + ": " + actualSheetName);

                if (actualSheetName.equals(expectedSheetName)) {
                    String successMsg = "Sheet name verification successful!<br>" +
                            "Sheet '" + expectedSheetName + "' is present at index " + expectedIndex + ".<br>" +
                            "Total sheets in file: " + numberOfSheets;
                    logger.info(successMsg.replace("<br>", " | "));
                    setSuccessMessage(successMsg);
                    result = com.testsigma.sdk.Result.SUCCESS;
                } else {
                    String errorMsg = "Sheet name verification failed!<br>" +
                            "Expected sheet name: '" + expectedSheetName + "'<br>" +
                            "Actual sheet name at index " + expectedIndex + ": '" + actualSheetName + "'<br>" +
                            "Total sheets in file: " + numberOfSheets;
                    logger.warn(errorMsg.replace("<br>", " | "));
                    setErrorMessage(errorMsg);
                    result = com.testsigma.sdk.Result.FAILED;
                }
            }

        } catch (NumberFormatException e) {
            String errorMessage = "Invalid number format for sheet index: " + e.getMessage();
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        } catch (Exception e) {
            String errorMessage = "Error verifying sheet name at index: " + ExceptionUtils.getMessage(e);
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
        File tempFile = ExcelCellUtils.downloadUrlToTempFile(fileUrl);
        logger.info("Downloaded file to: " + tempFile.getAbsolutePath());
        return tempFile;
    }
}

