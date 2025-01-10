package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.addons.util.PdfAndDocUtilities;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;

@Data
@Action(actionText = "Verify the value in the Latest Excelfile with Cell value rowNo and columnNo",
description = "Verify the cell value from the Excel file",
applicationType = ApplicationType.WEB)
public class VerifyExcel extends WebAction {

	@TestData(reference = "value")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "rowNo")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "columnNo")
	private com.testsigma.sdk.TestData testData3;

	@Override
	public com.testsigma.sdk.Result execute(){
		//Your Awesome code starts here
		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

		PdfAndDocUtilities documentutil = new PdfAndDocUtilities(driver, logger);

		try {
			
			File downloadedExcelFile = documentutil.copyFileFromDownloads("xlsx",null);

			FileInputStream inputStream = new FileInputStream(downloadedExcelFile);


			XSSFWorkbook wb=new XSSFWorkbook(inputStream);

			XSSFSheet sheet = wb.getSheetAt(0);

			int introw = Integer.parseInt(testData2.getValue().toString());
			int intcolumn = Integer.parseInt(testData3.getValue().toString());

			XSSFRow row=sheet.getRow(introw);

			XSSFCell cell=row.getCell(intcolumn);

			if (cell != null) {
				String CellValue;

				switch (cell.getCellType()) {
				case STRING:
					CellValue = cell.getStringCellValue();
					break;
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						CellValue = cell.getDateCellValue().toString();
					} else {
						CellValue = Double.toString(cell.getNumericCellValue());
					}
					break;
				case BOOLEAN:
					CellValue = Boolean.toString(cell.getBooleanCellValue());
					break;
				case FORMULA:
					CellValue = cell.getCellFormula();
					break;
				default:
					CellValue = "N/A";
				}


				if(CellValue.contains(testData1.getValue().toString())) {
					result = com.testsigma.sdk.Result.SUCCESS;
					setSuccessMessage("The particular value is matched with the value displayed in excel file : " +CellValue);
					logger.info("The particular value is matched with the value displayed in excel file" +CellValue);	
				}else {
					result = com.testsigma.sdk.Result.FAILED;
					setErrorMessage("The particular value is not matched in th excel file Actual value: " +CellValue + ",and expected value:" +testData1.getValue().toString());
					logger.warn("The particular value is not matched in th excel file Actual value: " +CellValue + ",and expected value:" +testData1.getValue().toString());	
				}
			}
		}
		catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		}
		return result;
	}
}