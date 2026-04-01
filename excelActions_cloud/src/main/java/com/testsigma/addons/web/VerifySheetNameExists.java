package com.testsigma.addons.web;

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
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
@Action(actionText = "Verify sheet name sheet-name exists in Excel file at file-path",
        description = "Verifies if a sheet with the given name exists in the specified Excel file",
        applicationType = ApplicationType.WEB)
public class VerifySheetNameExists extends WebAction {

    @TestData(reference = "sheet-name")
    private com.testsigma.sdk.TestData sheetName;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating sheet name verification");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String excelFilePath = filePath.getValue().toString();
            String expectedSheetName = sheetName.getValue().toString();

            logger.info("Excel file path: " + excelFilePath);
            logger.info("Expected sheet name: " + expectedSheetName);

            // Load the Excel file
            File excelFile = getExcelFile(excelFilePath);

            try (FileInputStream fis = new FileInputStream(excelFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                int numberOfSheets = workbook.getNumberOfSheets();
                List<String> sheetNames = new ArrayList<>();
                boolean sheetFound = false;

                // Iterate through all sheets to find the matching sheet name
                for (int i = 0; i < numberOfSheets; i++) {
                    String currentSheetName = workbook.getSheetName(i);
                    sheetNames.add(currentSheetName);
                    if (currentSheetName.equals(expectedSheetName)) {
                        sheetFound = true;
                        logger.info("Sheet '" + expectedSheetName + "' found at index " + i);
                    }
                }

                if (sheetFound) {
                    String successMsg = "Sheet name '" + expectedSheetName + "' exists in the Excel file.<br>" +
                            "Total sheets in file: " + numberOfSheets + "<br>" +
                            "All sheet names: " + sheetNames;
                    logger.info(successMsg.replace("<br>", " | "));
                    setSuccessMessage(successMsg);
                    result = com.testsigma.sdk.Result.SUCCESS;
                } else {
                    String errorMsg = "Sheet name '" + expectedSheetName + "' does NOT exist in the Excel file.<br>" +
                            "Total sheets in file: " + numberOfSheets + "<br>" +
                            "Available sheet names: " + sheetNames;
                    logger.warn(errorMsg.replace("<br>", " | "));
                    setErrorMessage(errorMsg);
                    result = com.testsigma.sdk.Result.FAILED;
                }
            }

        } catch (Exception e) {
            String errorMessage = "Error verifying sheet name: " + ExceptionUtils.getMessage(e);
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
        File tempFile = File.createTempFile("excel-sheet-verify-", "-" + fileName);
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

