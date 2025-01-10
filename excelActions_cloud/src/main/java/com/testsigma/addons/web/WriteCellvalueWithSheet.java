package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.addons.util.PdfAndDocUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Data
@Action(actionText = "Write the data datavalue into Latest Excelfile with Cell value rowNo,columnNo",
description = "Read the cell value from the Excel file ",
applicationType = ApplicationType.WEB)
public class WriteCellvalueWithSheet extends WebAction {

	@TestData(reference = "rowNo")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "columnNo")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "datavalue")
	private com.testsigma.sdk.TestData testData3;


	@Override
	public com.testsigma.sdk.Result execute(){
		//Your Awesome code starts here
		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		int rowIndex = Integer.parseInt(testData1.getValue().toString());;
		int columnIndex = Integer.parseInt(testData2.getValue().toString());;

		String data = testData3.getValue().toString();

		PdfAndDocUtilities documentutil = new PdfAndDocUtilities(driver, logger);
		
		File downloadedExcelFile = null;
		try {
			downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx",null);
		} catch (Exception el) {
			String errorMessage = ExceptionUtils.getStackTrace(el);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		}
		
		try (FileInputStream fis = new FileInputStream(downloadedExcelFile);
				Workbook workbook = new XSSFWorkbook(fis)) {
			Sheet sheet = workbook.getSheetAt(0);
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				row = sheet.createRow(rowIndex);
			}
			Cell cell = row.getCell(columnIndex);
			if (cell == null) {
				cell = row.createCell(columnIndex);
			}
			cell.setCellValue(data);
			try (FileOutputStream fileOut = new FileOutputStream(downloadedExcelFile)) {
				workbook.write(fileOut);
				System.out.println("Data written successfully to Excel file.");
			} catch (IOException e) {
				String errorMessage = ExceptionUtils.getStackTrace(e);
				result = com.testsigma.sdk.Result.FAILED;
				setErrorMessage(errorMessage);
				logger.warn(errorMessage);	
			}
		} catch (IOException e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		}
		return result;
	}
}