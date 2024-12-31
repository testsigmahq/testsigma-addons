package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.NoSuchElementException;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(actionText = "Excel: Read the entire Row of the Excel from Local filePath using the Row Index and store it in a "
        + "variable named testdata",
        description = "Read the entire Row of the Excel file from given filepath or URL using the Row number and store it in a "
                + "variable named testdata",
        applicationType = ApplicationType.WEB, useCustomScreenshot = false)
public class ExtractCompleteFieldValuesForGivenFilePath extends WebAction {

  @TestData(reference = "filePath")
  private com.testsigma.sdk.TestData filePath;
  @TestData(reference = "Index")
  private com.testsigma.sdk.TestData testData1;
  @TestData(reference = "testdata", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData testData2;
  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");

    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;


    String fileLocation = null;
    File excelFile = null;
    try {
      logger.info("filePath: " + getFilePath().getValue().toString());
      fileLocation = getFilePath().getValue().toString();

      excelFile = null;

      // Check if it is a URL or a local file path
      if (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) {
        excelFile = downloadFile(fileLocation);
      } else {
        excelFile = new File(fileLocation);
      }


      StringBuilder entireFieldValues = new StringBuilder();
      logger.info("entireFieldValues: " + entireFieldValues);

      if (!excelFile.exists() || !excelFile.isFile()) {
        if (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) {
          setErrorMessage("Error occurred while downloading file from URL: " + fileLocation);
        } else {
          setErrorMessage("The provided file path is invalid: " + fileLocation);
        }
        return com.testsigma.sdk.Result.FAILED;
      }


      try (FileInputStream inputStream = new FileInputStream(excelFile)) {
        // Load the workbook and get the first sheet
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        logger.info("Case Row");
        int rowIndex = Integer.parseInt(testData1.getValue().toString());
        var row = sheet.getRow(rowIndex);
        if (row != null) {
          logger.info("Values in row " + (rowIndex + 1) + ":");

          // Iterate over all cells in the row
          for (Cell cell : row) {
            String CellValueForRow;
            // Check cell type and print the value
            switch (cell.getCellType()) {
              case STRING:
                CellValueForRow = cell.getStringCellValue();
                break;
              case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                  CellValueForRow = cell.getDateCellValue().toString();
                } else {
                  CellValueForRow = Double.toString(cell.getNumericCellValue());
                }
                break;
              case BOOLEAN:
                CellValueForRow = Boolean.toString(cell.getBooleanCellValue());
                break;
              case FORMULA:
                CellValueForRow = cell.getCellFormula();
                break;
              default:
                CellValueForRow = "N/A";
            }
            entireFieldValues.append(CellValueForRow);
            entireFieldValues.append(",");
          }
        } else {
          logger.info("Row " + (rowIndex + 1) + " is empty or this is the last column with data");
        }
      } catch (IOException e) {
        String errorMessage = "Error reading Excel file: " + ExceptionUtils.getStackTrace(e);
        setErrorMessage(errorMessage);
        logger.warn(errorMessage);
        return com.testsigma.sdk.Result.FAILED;
      }
      if (entireFieldValues.length() > 0) {
        entireFieldValues.deleteCharAt(entireFieldValues.length() - 1);
      }

      logger.info("Storing values");
      runTimeData.setKey(testData2.getValue().toString());
      runTimeData.setValue(entireFieldValues.toString());

      setSuccessMessage("Extracted the Complete Row Values and Stored it in variable "
              + testData2.getValue().toString() + " = " + runTimeData.getValue());


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