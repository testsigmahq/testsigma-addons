package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

@Data
@Action(actionText = "Fetch data from the csv file filepath by value targetvalue with corresponding row value index and store into variable testdata",
description = "Fetch the value from csv with targetvalue and corresponding row value and store into a runtime variable",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class FetchCSVData extends WebAction {

	@TestData(reference = "filepath")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "targetvalue")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "index")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData4;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		String filePath = testData1.getValue().toString();
		String targetCellValue = testData2.getValue().toString();

		File excelFile = null;

		if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
			try {
				logger.info("Inside if");
				excelFile = downloadFile(filePath);
				logger.info("Downloaded excel file at: " + excelFile.getAbsolutePath());
			} catch (IOException e) {
				logger.warn("IO Exception: " + ExceptionUtils.getStackTrace(e));
				setErrorMessage("IO Exception: " + ExceptionUtils.getMessage(e));
				result = com.testsigma.sdk.Result.FAILED;
				return  result;
			}
		} else {
			excelFile = new File(filePath);
			logger.info("Inside else");
			logger.info("Downloaded excel file  at: " + excelFile.getAbsolutePath());
		}

		try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath)).build()) {
			List<String[]> records = csvReader.readAll();

			for (String[] record : records) {
				for (int i = 0; i < record.length; i++) {
					// Check if the current cell value contains the target value
					if (record[i].contains(targetCellValue)) {
						String correspondingCellValue = record[Integer.valueOf(testData3.getValue().toString())];

						runTimeData.setValue(correspondingCellValue);
						runTimeData.setKey(testData4.getValue().toString());
						result = com.testsigma.sdk.Result.SUCCESS; 
						setSuccessMessage("Value fetch from the csv file :" +correspondingCellValue+"store into a variable:"+testData4.getValue().toString());
					}
				}
			}
		}catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		} 
		return result;
	}

	private File downloadFile(String fileUrl) throws IOException {
		URL url = new URL(fileUrl);
		String fileName = Paths.get(url.getPath()).getFileName().toString();
		File tempFile = File.createTempFile("downloaded-", fileName);
		try (InputStream in = url.openStream();
			 OutputStream out = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		}
		return tempFile;
	}
}