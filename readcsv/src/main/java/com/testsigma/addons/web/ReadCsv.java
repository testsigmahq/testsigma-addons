package com.testsigma.addons.web;

import com.testsigma.addons.web.util.FileDownloadUtil;
import com.testsigma.sdk.WebAction;
import com.opencsv.CSVReader;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

@Data
@Action(actionText = "Read row count from CSV file where directory is test_data and store in into a runtimevariable var1",
        description = "Reading row count from CSV file",
        applicationType = ApplicationType.WEB)
public class ReadCsv extends WebAction {

  @TestData(reference = "test_data")
  private com.testsigma.sdk.TestData testData;

  @TestData(reference = "var1")
  private com.testsigma.sdk.TestData testData1;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    String testDataPath = testData != null && testData.getValue() != null ? testData.getValue().toString() : null;
    logger.info("test_data resolved to: " + testDataPath);

    Reader reader = null;
    CSVReader csvreader = null;
    File downloadedFile = null;

    try {
        File s1 = FileDownloadUtil.resolveInputFile(testDataPath, "test_data");
        if (FileDownloadUtil.isUrl(testDataPath)) {
            downloadedFile = s1;
        }
        logger.info("Name of the file is " + s1.getName());

    	System.out.println(s1.getName());
    	reader = new FileReader(s1.getAbsolutePath());

    	csvreader = new CSVReader(reader);
    	List<String[]> data = csvreader.readAll();
    	String s2 = String.valueOf(data.size());

    	runTimeData.setKey(testData1.getValue().toString());
    	runTimeData.setValue(s2);

        setSuccessMessage("Success in execution count of the data is " + runTimeData);
    }
    catch (FileDownloadUtil.InvalidTestDataException e) {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage(e.getMessage());
    }
    catch(Exception e) {
    	result = com.testsigma.sdk.Result.FAILED;
    	setErrorMessage("Operation Failed "+e.getMessage());
        logger.warn("Exception Occurred: " + e);
    } finally {
        FileDownloadUtil.closeQuietly(csvreader);
        FileDownloadUtil.closeQuietly(reader);
        FileDownloadUtil.deleteQuietly(downloadedFile);
    }

    return result;
  }
}
