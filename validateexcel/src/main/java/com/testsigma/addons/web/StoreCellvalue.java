package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(actionText = "Read the Excel filepath with Cell value rowNo,columnNo and store into a variable testdata",
		description = "Read the cell value from the Excel file ",
		applicationType = ApplicationType.WEB)
public class StoreCellvalue extends WebAction {

	@TestData(reference = "filepath")
	private com.testsigma.sdk.TestData testData1;

	@TestData(reference = "rowNo")
	private com.testsigma.sdk.TestData testData2;

	@TestData(reference = "columnNo")
	private com.testsigma.sdk.TestData testData3;

	@TestData(reference = "testdata")
	private com.testsigma.sdk.TestData testData4;

	@RunTimeData
	private com.testsigma.sdk.RunTimeData runTimeData;

	@Override
	public com.testsigma.sdk.Result execute() {
		//Your Awesome code starts here
		logger.info("Initiating execution");

		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		String fileLocation = null;
		File excelFile = null;

		try {
			logger.info("filePath: " + getTestData1().getValue().toString());
			fileLocation = getTestData1().getValue().toString();
			excelFile = null;

			// Check if it is a URL or a local file path
			if (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) {
				excelFile = downloadFile(fileLocation);
			} else {
				excelFile = new File(fileLocation);
			}

			if (!excelFile.exists() || !excelFile.isFile()) {
				if (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) {
					setErrorMessage("Error occurred while downloading file from URL: " + fileLocation);
				} else {
					setErrorMessage("The provided file path is invalid: " + fileLocation);
				}
				return com.testsigma.sdk.Result.FAILED;
			}

			FileInputStream inputStream = new FileInputStream(excelFile);

			XSSFWorkbook wb = new XSSFWorkbook(inputStream);

			XSSFSheet sheet = wb.getSheetAt(0);

			int introw = Integer.parseInt(testData2.getValue().toString());
			int intcolumn = Integer.parseInt(testData3.getValue().toString());

			XSSFRow row = sheet.getRow(introw);

			XSSFCell cell = row.getCell(intcolumn);

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

				runTimeData.setKey(testData4.getValue().toString());
				runTimeData.setValue(CellValue);

				setSuccessMessage("Success in execution cell value stored from excel file is:<br> " + CellValue);
			} else {
				setErrorMessage("Cell at row " + introw + ", column " + intcolumn + " is null.");
				result = com.testsigma.sdk.Result.FAILED;
			}

		} catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		} finally {
			if (fileLocation != null && (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) && excelFile != null) {
				excelFile.delete(); //delete temp file if it was a download
			}
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