package com.testsigma.addons.web;

import com.testsigma.addons.web.util.FileDownloadUtil;
import com.testsigma.sdk.WebAction;
import com.opencsv.CSVReader;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Store the value from the CSV filepath with rowNo and columnNo and store the data into a runtimevariable var1",
        description = "Verifying the value displayed in the csv file",
        applicationType = ApplicationType.WEB)

public class ReadCsvFile extends WebAction {

    @TestData(reference = "value")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "filepath")
    private com.testsigma.sdk.TestData testData4;

    @TestData(reference = "var1")
    private com.testsigma.sdk.TestData testData5;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        String filePath = testData4 != null && testData4.getValue() != null ? testData4.getValue().toString() : null;
        logger.info("filepath resolved to: " + filePath);

        Reader reader = null;
        CSVReader csvread = null;
        File downloadedFile = null;

        try {
            int rowNo = Integer.parseInt(testData2.getValue().toString());
            int columnNo = Integer.parseInt(testData3.getValue().toString());

            File s1 = FileDownloadUtil.resolveInputFile(filePath, "filepath");
            if (FileDownloadUtil.isUrl(filePath)) {
                downloadedFile = s1;
            }

            logger.info("File picked for filepath: " + s1.getAbsolutePath());

            reader = new FileReader(s1.getAbsolutePath());
            csvread = new CSVReader(reader);
            List<String[]> csvvalue = csvread.readAll();
            String[] csvRow = csvvalue.get(rowNo);
            String csvColumn = csvRow[columnNo];

            if(csvColumn.contains(testData1.getValue().toString())) {
                result = com.testsigma.sdk.Result.SUCCESS;
                setSuccessMessage("The particular value is matched with the value displayed in csv file : " +csvColumn);
                logger.info("The particular value is matched with the value displayed in csv file" +csvColumn);
            }else {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("The particular value is not matched with the value displayed in csv file : " +csvColumn);
                logger.warn("The particular value is not matched with the value displayed in csv file : " +csvColumn);
            }

        } catch (FileDownloadUtil.InvalidTestDataException e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(e.getMessage());
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        } finally {
            FileDownloadUtil.closeQuietly(csvread);
            FileDownloadUtil.closeQuietly(reader);
            FileDownloadUtil.deleteQuietly(downloadedFile);
        }
        return result;
    }
}
