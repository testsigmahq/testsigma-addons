package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
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

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(actionText = "Write the data datavalue into the Excelfile excel-path with Cell value rowNo,columnNo, sheet-index and store the path in runtime variable variable-name(supports upload section)",
        description = "Read the cell value from the Excel file ",
        applicationType = ApplicationType.WEB)
public class WriteCellvalueWithSheetFilepath extends WebAction {

    @TestData(reference = "excel-path")
    private com.testsigma.sdk.TestData excelPath;

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

        String filePath = excelPath.getValue().toString();
        int rowIndex = Integer.parseInt(testData1.getValue().toString());;
        int columnIndex = Integer.parseInt(testData2.getValue().toString());;
        int sheetIndex = Integer.parseInt(testData4.getValue().toString());;

        String data = testData3.getValue().toString();

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

        try (FileInputStream fis = new FileInputStream(excelFile);
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
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(excelFile.getAbsolutePath());
                logger.info("Data written successfully to Excel file.File path: " + excelFile.getAbsolutePath());
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