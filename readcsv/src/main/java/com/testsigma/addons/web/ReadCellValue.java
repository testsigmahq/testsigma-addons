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
@Action(actionText = "Verify the value in the CSV filename with rowNo and columnNo",
        description = "Verifying the value displayed in the csv file",
        applicationType = ApplicationType.WEB)

public class ReadCellValue extends WebAction {

  @TestData(reference = "value")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "rowNo")
  private com.testsigma.sdk.TestData testData2;
  @TestData(reference = "columnNo")
  private com.testsigma.sdk.TestData testData3;
  @TestData(reference = "filename")
  private com.testsigma.sdk.TestData testData4;

  @Override
  public com.testsigma.sdk.Result execute() {

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");
    Reader reader = null;
    CSVReader csvread = null;
    try {
    	int rowNo = Integer.parseInt(testData2.getValue().toString());
    	int columnNo = Integer.parseInt(testData3.getValue().toString());

    	String fileName = testData4 != null && testData4.getValue() != null ? testData4.getValue().toString() : null;
    	if (fileName == null || fileName.trim().isEmpty()) {
    	    result = com.testsigma.sdk.Result.FAILED;
    	    setErrorMessage("filename test data value is empty. Please provide a valid CSV filename (without the Downloads path).");
    	    return result;
    	}

    	String Filecsv = System.getProperty("user.home") +File.separator+ "Downloads" +File.separator+ fileName + ".csv";
    	logger.info("Resolved CSV path: " + Filecsv);

    	File csvFile = new File(Filecsv);
    	if (!csvFile.exists()) {
    	    result = com.testsigma.sdk.Result.FAILED;
    	    setErrorMessage("CSV file does not exist at: " + Filecsv
    	            + ". Make sure it has been downloaded/generated in the Downloads folder before this step runs.");
    	    return result;
    	}

		reader = new FileReader(Filecsv);
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

	} catch (Exception e) {
		String errorMessage = ExceptionUtils.getStackTrace(e);
		result = com.testsigma.sdk.Result.FAILED;
		setErrorMessage(errorMessage);
		logger.warn(errorMessage);
	} finally {
		FileDownloadUtil.closeQuietly(csvread);
		FileDownloadUtil.closeQuietly(reader);
	}
	return result;
  }
}
