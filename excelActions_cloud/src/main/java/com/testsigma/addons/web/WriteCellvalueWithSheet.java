package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelUtilities;
import com.testsigma.addons.util.ExcelUtilitiesFactory;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
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

@Data
@Action(actionText = "Write the data datavalue into Latest Excelfile with Cell value rowNo,columnNo, sheet-index and store the path in runtime variable variable-name",
description = "Read the cell value from the Excel file ",
applicationType = ApplicationType.WEB)
public class WriteCellvalueWithSheet extends WebAction {

	@TestData(reference = "rowNo")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "columnNo")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "datavalue")
	private com.testsigma.sdk.TestData testData3;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData testData4;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute(){
		//Your Awesome code starts here
		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		int rowIndex = Integer.parseInt(testData1.getValue().toString());;
		int columnIndex = Integer.parseInt(testData2.getValue().toString());;
        int sheetIndex = Integer.parseInt(testData4.getValue().toString());;

		String data = testData3.getValue().toString();

        ExcelUtilities excelutil = ExcelUtilitiesFactory.create(driver, logger);
		
		File downloadedExcelFile = null;
		try {
			downloadedExcelFile = excelutil.copyFileFromDownloads("xlsx",null);
		} catch (Exception el) {
			String errorMessage = ExceptionUtils.getStackTrace(el);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		}
		
		try (FileInputStream fis = new FileInputStream(downloadedExcelFile);
				Workbook workbook = new XSSFWorkbook(fis)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
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
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(downloadedExcelFile.getAbsolutePath());
				String successMsg = "Successfully wrote value '" + data + "' to cell [Row: " + rowIndex + 
						", Column: " + columnIndex + "] in Sheet index: " + sheetIndex + 
						".<br>File path: " + downloadedExcelFile.getAbsolutePath();
				logger.info(successMsg.replace("<br>", " "));
				setSuccessMessage(successMsg);
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