package com.testsigma.addons.web;

import com.testsigma.addons.web.util.FileDownloadUtil;
import com.testsigma.sdk.WebAction;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.List;

@Data
@Action(actionText = "Write row number and column number into CSV file with testdata where directory is test_data",
        description = "Write a particular row and column into CSV file",
        applicationType = ApplicationType.WEB)
public class WriteCsvFile extends WebAction {

  @TestData(reference = "row")
  private com.testsigma.sdk.TestData row;

  @TestData(reference = "column")
  private com.testsigma.sdk.TestData column;

  @TestData(reference = "test_data")
  private com.testsigma.sdk.TestData filePath;

  @TestData(reference = "testdata")
  private com.testsigma.sdk.TestData testData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    String filePathDir = filePath != null && filePath.getValue() != null ? filePath.getValue().toString() : null;
    logger.info("test_data resolved to: " + filePathDir);

    Reader reader = null;
    CSVReader csvreader = null;
    boolean sourcedFromUrl = FileDownloadUtil.isUrl(filePathDir);
    File tempDownloadedFile = null;

    try {
        File s1 = FileDownloadUtil.resolveInputFile(filePathDir, "test_data");
        if (sourcedFromUrl) {
            tempDownloadedFile = s1;
            logger.warn("test_data was a URL - the write will only update a local temporary copy at "
                    + s1.getAbsolutePath() + " and will NOT be uploaded back to the source URL.");
        }
        logger.info("Name of the file is " + s1.getName());

        reader = new FileReader(s1.getAbsolutePath());
		String replace = testData.getValue().toString();
		csvreader = new CSVReader(reader);
		List<String[]> data = csvreader.readAll();

		data.get(Integer.parseInt(row.getValue().toString()))[Integer.parseInt(column.getValue().toString())] = replace;
		csvreader.close();
		try (CSVWriter writer = new CSVWriter(new FileWriter(s1.getAbsolutePath()))) {
			writer.writeAll(data);
			writer.flush();
		}
    	setSuccessMessage("Replaced Successfully in the CSV file . Replaced data is "+replace);
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
        // Only clean up the temp file if it was downloaded from a URL AND the write failed;
        // on success we intentionally leave it in place since it now holds the only copy of the edit.
        if (tempDownloadedFile != null && result == com.testsigma.sdk.Result.FAILED) {
            FileDownloadUtil.deleteQuietly(tempDownloadedFile);
        }
    }
    return result;
  }
}
