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
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "file-url")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        logger.info("test-data: " + testData1.getValue().toString());
        logger.info("file url: " + testData2.getValue().toString());

        try {
            File newFile = urlToFileConverter(testData2.getValue().toString());
            logger.info("getting the workbook");
            FileInputStream inputStream = new FileInputStream(newFile);
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);

            logger.info("got the workbook");
            XSSFSheet sheet = wb.getSheetAt(0);
            if (testData1.getValue().toString().equalsIgnoreCase("non-empty cells")) {
                int nonEmptyCellCount = 0;
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    for (int j = 0; j < sheet.getRow(i).getPhysicalNumberOfCells(); j++) {
                        if (sheet.getRow(i).getCell(j) != null && !sheet.getRow(i).getCell(j).toString().isEmpty()) {
                            nonEmptyCellCount++;
                        }
                    }
                }
                logger.info("non-empty cell count: " + nonEmptyCellCount);
                runTimeData.setValue(nonEmptyCellCount);
                runTimeData.setKey(testData3.getValue().toString());
                setSuccessMessage("Successfully stored the count of non-empty cells from excel file " +
                        runTimeData.getKey() + " = " + runTimeData.getValue());
            } else if (testData1.getValue().toString().equalsIgnoreCase("rows")) {
                // using the below logic instead of sheet.getPhysicalNumberOfRows() as it is returning larger values
                // than expected no of rows...
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
                runTimeData.setValue(rowCount);
                runTimeData.setKey(testData3.getValue().toString());
                setSuccessMessage("Successfully stored the number of rows from excel file " +
                        runTimeData.getKey() + " = " + runTimeData.getValue());
            } else if (testData1.getValue().toString().equalsIgnoreCase("columns")) {
                int maxColumns = 0;
                for (Row row : sheet) {
                    int currentColCount = 0;
                    for (Cell cell : row) {
                        if (cell != null && !cell.toString().trim().isEmpty()) {
                            currentColCount = cell.getColumnIndex() + 1; // +1 because column index is zero-based
                        }
                    }
                    if (currentColCount > maxColumns) {
                        maxColumns = currentColCount;
                    }
                }
                runTimeData.setValue(maxColumns);
                runTimeData.setKey(testData3.getValue().toString());
                setSuccessMessage("Successfully stored the number of columns from excel file " +
                        runTimeData.getKey() + " = " + runTimeData.getValue());
            }
            return result;
        } catch (Exception e) {
            logger.info("Unable to read the file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to read the file: " + ExceptionUtils.getStackTrace(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }

    private File urlToFileConverter(String url) {
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                logger.info("Given is s3 url ...");
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile("tempExcelFile", "."
                        + ".xlsx");
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access the given pdfs, please check the given inputs.");
        }
    }

}