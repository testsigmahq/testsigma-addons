package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Data
@Action(actionText = "Write the data datavalue into Excel filepath with Cell value rowNo,columnNo and Sheet sheetIndex",
description = "Read the cell value from the Excel file ",
applicationType = ApplicationType.WEB)
public class WriteCellvalueWithSheet extends WebAction {

	@TestData(reference = "filepath")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "rowNo")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "columnNo")
	private com.testsigma.sdk.TestData testData3;

	@TestData(reference = "datavalue")
	private com.testsigma.sdk.TestData testData4;

	@TestData(reference = "sheetIndex")
	private com.testsigma.sdk.TestData testData5;


	@Override
	public com.testsigma.sdk.Result execute(){
		//Your Awesome code starts here
		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		int rowIndex = Integer.parseInt(testData2.getValue().toString());;
		int columnIndex = Integer.parseInt(testData3.getValue().toString());;
		int sheetIndex = Integer.parseInt(testData5.getValue().toString());;

		//String data = testData4.getValue().toString();

		String filePath = testData1.getValue().toString();

		try (FileInputStream fis = new FileInputStream(filePath);
				Workbook workbook = new XSSFWorkbook(fis)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet == null) {
				result = com.testsigma.sdk.Result.FAILED;
				setErrorMessage("Sheet not found at index: " + sheetIndex);
				logger.warn("Sheet not found at index: " + sheetIndex);
			}

			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				row = sheet.createRow(rowIndex);
			}
			Cell cell = row.getCell(columnIndex);
			if (cell == null) {
				cell = row.createCell(columnIndex);
			}

			if (cell.getCellType() == CellType.STRING) {
				RichTextString rts = new XSSFRichTextString(String.valueOf(testData4.getValue()));
				cell.setCellType(CellType.STRING);
				cell.setCellValue(rts);
			} else {
				cell.setCellValue(testData4.getValue().toString());
			}
			try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
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