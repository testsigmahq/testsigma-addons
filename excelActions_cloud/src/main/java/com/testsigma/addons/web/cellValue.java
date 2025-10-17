package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelUtilities;
import com.testsigma.addons.util.ExcelUtilitiesFactory;
import com.testsigma.sdk.WebAction;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;

@Data
@Action(actionText = "Read the Latest Excelfile with Cell value rowNo,columnNo and store into a variable testdata",
		description = "Read the data from latest excel file",
		applicationType = ApplicationType.WEB,
		useCustomScreenshot = false)
public class cellValue extends WebAction {

	@TestData(reference = "rowNo")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "columnNo")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "testdata" , isRuntimeVariable = true)
	private com.testsigma.sdk.TestData testData3;
	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {

		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        ExcelUtilities excelutil = ExcelUtilitiesFactory.create(driver, logger);

		try {

			File downloadedExcelFile = excelutil.copyFileFromDownloads("xlsx",null);

			FileInputStream inputStream = new FileInputStream(downloadedExcelFile);

			XSSFWorkbook wb=new XSSFWorkbook(inputStream);

			XSSFSheet sheet = wb.getSheetAt(0);

			int introw = Integer.parseInt(testData1.getValue().toString());
			int intcolumn = Integer.parseInt(testData2.getValue().toString());

			XSSFRow row=sheet.getRow(introw);

			XSSFCell cell=row.getCell(intcolumn);
			String CellValue = ""; // Initialize to empty string

			if (cell != null) {


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
					case BLANK:
						CellValue="";
						break;
					default:
						CellValue = "N/A";
				}
			}

			runTimeData.setKey(testData3.getValue().toString());
			runTimeData.setValue(CellValue);

			setSuccessMessage("Success in execution cell value stored from excel file is:<br> " +CellValue);

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