package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.addons.util.PdfAndDocUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.NoSuchElementException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Data
@Action(actionText = "Write the data data_value into Latest Excelfile in newline is test_data and store rownumber into a variable testdata",
description = "Write the data in new line into xlsx file where absolutepath and store the new row number",
applicationType = ApplicationType.WEB)
public class WriteNewExcelFile extends WebAction {

	@TestData(reference = "data_value")
	private com.testsigma.sdk.TestData data;

	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;


	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		//Your Awesome code starts here
		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		PdfAndDocUtilities documentutil = new PdfAndDocUtilities(driver, logger);

		String dataValues = data.getValue().toString();

		try {
			
			File downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx",null);
			
			FileInputStream inputStream = new FileInputStream(downloadedExcelFile);
			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			int newRowNumber = sheet.getLastRowNum() + 1;
			Row newRow = sheet.createRow(newRowNumber);
			Cell cell = newRow.createCell(0);
			cell.setCellValue(dataValues);

			FileOutputStream outputStream = new FileOutputStream(downloadedExcelFile);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

			runTimeData.setValue(Integer.toString(newRowNumber));
			runTimeData.setKey(testData.getValue().toString());

			setSuccessMessage("Data added to the CSV file successfully. New row number: " + newRowNumber);

		}catch (Exception e) {
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage("Operation Failed "+e.getMessage());
		}
		return result;
	}
}
