package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "Testing Write the data datavalue into Excel(xls) filepath with Cell value rowNo,columnNo and Sheet sheet-index(starts with 1) and store the path in runtime variable variable-name",
        description = "Write the data into a specific cell in the Excel file",
        applicationType = ApplicationType.WEB
)
public class WriteCellvalueWithSheet extends WebAction {

    @TestData(reference = "filepath")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData columnNumber;

    @TestData(reference = "datavalue")
    private com.testsigma.sdk.TestData dataValue;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Starting WriteCellvalueWithSheet execution");
        Result result = Result.SUCCESS;

        String valueToWrite = dataValue.getValue().toString();
        int rowIdx = Integer.parseInt(rowNumber.getValue().toString());
        int colIdx = Integer.parseInt(columnNumber.getValue().toString());
        int sheetIdx = Integer.parseInt(sheetIndex.getValue().toString());

        File excelFile;
        try {
            // Handle URL or local file
            String fileLocation = filePath.getValue().toString();
            if (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) {
                excelFile = downloadFile(fileLocation);
            } else {
                excelFile = new File(fileLocation);
            }

            if (!excelFile.exists() || !excelFile.isFile()) {
                setErrorMessage("Invalid file path or file not found: " + fileLocation);
                return Result.FAILED;
            }

            // Open workbook safely
            try (FileInputStream inputStream = new FileInputStream(excelFile);
                 HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {

                if (sheetIdx < 1 || sheetIdx > workbook.getNumberOfSheets()) {
                    setErrorMessage("Invalid sheet index: " + sheetIdx);
                    return Result.FAILED;
                }

                HSSFSheet sheet = workbook.getSheetAt(sheetIdx - 1);
                HSSFRow row = sheet.getRow(rowIdx);
                if (row == null) {
                    row = sheet.createRow(rowIdx);
                }

                HSSFCell cell = row.getCell(colIdx);
                if (cell == null) {
                    cell = row.createCell(colIdx, CellType.STRING);
                }

                cell.setCellValue(valueToWrite);

                // Write changes back to file
                try (FileOutputStream outputStream = new FileOutputStream(excelFile)) {
                    workbook.write(outputStream);
                }

                runTimeData.setValue(excelFile.getAbsolutePath());
                runTimeData.setKey(variableName.getValue().toString());
                setSuccessMessage("Successfully written value '" + valueToWrite +
                        "' to Excel file at row " + rowIdx + ", column " + colIdx +
                        ", sheet index " + sheetIdx + ". File path: " + excelFile.getAbsolutePath());

            }

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            setErrorMessage("Error writing to Excel: " + e.getMessage());
            logger.warn(errorMessage);
            result = Result.FAILED;
        }

        return result;
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        File tempFile = File.createTempFile("downloaded-", "-" + fileName);
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
