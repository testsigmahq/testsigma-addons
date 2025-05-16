package com.testsigma.addons.web;

import com.testsigma.addons.util.PdfAndDocUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

@Data
@Action(actionText = "Store the count of test-data from the excel file file-url into runtime variable runtime-variable",
        description = "Read the data from latest excel file",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class StoreNonEmptyCellCount extends WebAction {

    @TestData(reference = "test-data", allowedValues = {"Non-Empty cells", "Rows", "Columns"})
    private com.testsigma.sdk.TestData countTypeData;
    @TestData(reference = "file-url")
    private com.testsigma.sdk.TestData fileUrlData;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.info("Count type: " + countTypeData.getValue().toString());
        logger.info("File URL: " + fileUrlData.getValue().toString());

        try {
            File excelFile = downloadFileFromUrl(fileUrlData.getValue().toString());
            logger.info("Opening workbook");
            FileInputStream inputStream = new FileInputStream(excelFile);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

            logger.info("Successfully opened workbook");
            XSSFSheet sheet = workbook.getSheetAt(0);
            String countType = countTypeData.getValue().toString();
            int calculatedCount = PdfAndDocUtilities.calculateExcelDataCount(countType, sheet);

            String variableName = runtimeVariableName.getValue().toString();
            runTimeData.setValue(calculatedCount);
            runTimeData.setKey(variableName);

            setSuccessMessage("Successfully stored the count of " + countType + " from excel file to " +
                    variableName + " = " + calculatedCount);
            return result;
        } catch (Exception e) {
            logger.info("Failed to read the excel file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to read the excel file: " + ExceptionUtils.getStackTrace(e));
            return com.testsigma.sdk.Result.FAILED;
        }
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
            throw new RuntimeException("Unable to access the specified Excel file. Please check the provided URL or file path.");
        }
    }
}