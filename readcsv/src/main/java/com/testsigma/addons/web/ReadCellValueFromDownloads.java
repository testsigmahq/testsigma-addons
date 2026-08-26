package com.testsigma.addons.web;

import com.testsigma.addons.web.util.FileDownloadUtil;
import com.testsigma.sdk.WebAction;
import com.opencsv.CSVReader;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Verify the value in the CSV directorypath with rowNo and columnNo",
        description = "Verifying the value displayed in the csv file",
        applicationType = ApplicationType.WEB)

public class ReadCellValueFromDownloads extends WebAction {

    @TestData(reference = "value")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "directorypath")
    private com.testsigma.sdk.TestData testData4;

    @Override
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        String directoryPath = testData4 != null && testData4.getValue() != null
                ? testData4.getValue().toString() : null;
        logger.info("directorypath resolved to: " + directoryPath);

        File downloadedFile = null;
        Reader reader = null;
        CSVReader csvread = null;

        try {
            int rowNo = Integer.parseInt(testData2.getValue().toString());
            int columnNo = Integer.parseInt(testData3.getValue().toString());

            File latestFile = FileDownloadUtil.resolveInputFile(directoryPath, "directorypath");
            if (FileDownloadUtil.isUrl(directoryPath)) {
                downloadedFile = latestFile;
            }
            logger.info("File picked for directorypath: " + latestFile.getAbsolutePath());

            String Filecsv = latestFile.getAbsolutePath();
            reader = new FileReader(Filecsv);
            csvread = new CSVReader(reader);
            List<String[]> csvvalue = csvread.readAll();
            String[] csvRow = csvvalue.get(rowNo);
            String csvColumn = csvRow[columnNo];

            if (csvColumn.contains(testData1.getValue().toString())) {
                result = com.testsigma.sdk.Result.SUCCESS;
                setSuccessMessage("The particular value is matched with the value displayed in csv file : " + csvColumn);
                logger.info("The particular value is matched with the value displayed in csv file" + csvColumn);
            } else {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("The particular value is not matched with the value displayed in csv file : " + csvColumn);
                logger.warn("The particular value is not matched with the value displayed in csv file : " + csvColumn);
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
