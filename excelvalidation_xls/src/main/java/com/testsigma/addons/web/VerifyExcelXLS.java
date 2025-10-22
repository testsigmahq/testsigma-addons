package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "Verify the value in the ExcelFile(xls) fullfilepath with Cell value rowNo,columnNo and sheet sheet-index(starts with 1)",
        description = "Verify the specific cell value from the Excel file",
        applicationType = ApplicationType.WEB
)
public class VerifyExcelXLS extends WebAction {

    @TestData(reference = "value")
    private com.testsigma.sdk.TestData expectedValue;

    @TestData(reference = "fullfilepath")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData columnNumber;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex;

    @Override
    public Result execute() {
        logger.info("Starting VerifyExcelXLS execution");
        Result result = Result.SUCCESS;

        String expected = expectedValue.getValue().toString();
        String fileLocation = filePath.getValue().toString();
        int sheetIdx = Integer.parseInt(sheetIndex.getValue().toString());
        int rowIdx = Integer.parseInt(rowNumber.getValue().toString());
        int colIdx = Integer.parseInt(columnNumber.getValue().toString());

        File excelFile;
        try {
            // Handle URL or local file
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
                    setErrorMessage("Row " + rowIdx + " does not exist in sheet index " + sheetIdx);
                    return Result.FAILED;
                }

                HSSFCell cell = row.getCell(colIdx);
                String actual = "";
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    actual = "";
                    logger.info("Successfully read cell value from cell sheet index");
                    setSuccessMessage("Successfully read an empty/blank cell at row " + rowIdx + ", column " + colIdx);
                } else {
                    actual = getCellValue(cell, workbook).trim();;
                }

                logger.info("Read cell value: '" + actual + "' (Expected: '" + expected + "')");

                if (actual.equalsIgnoreCase(expected)) {
                    setSuccessMessage("The expected value matches the value in the Excel file: '" + actual + "'");
                    result = Result.SUCCESS;
                } else {
                    setErrorMessage("Value mismatch. Expected: '" + expected + "', but found: '" + actual + "'");
                    result = Result.FAILED;
                }
            }

        } catch (Exception e) {
            logger.warn(ExceptionUtils.getStackTrace(e));
            setErrorMessage("An unexpected error occurred: " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }

    private String getCellValue(HSSFCell cell, HSSFWorkbook workbook) {
        String cellValue = "";
        try {
            switch (cell.getCellType()) {
                case STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    double num = cell.getNumericCellValue();
                    if (num == (long) num) {
                        cellValue = String.valueOf((long) num); // format integers without .0
                    } else {
                        cellValue = String.valueOf(num);
                    }
                    break;
                case BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    switch (evaluator.evaluateInCell(cell).getCellType()) {
                        case NUMERIC:
                            double n = cell.getNumericCellValue();
                            cellValue = (n == (long) n) ? String.valueOf((long) n) : String.valueOf(n);
                            break;
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        case BOOLEAN:
                            cellValue = String.valueOf(cell.getBooleanCellValue());
                            break;
                        default:
                            cellValue = "";
                    }
                    break;
                case BLANK:
                    cellValue = "";
                    break;
                default:
                    cellValue = "";
            }
        } catch (Exception ex) {
            logger.warn("Error reading cell value: " + ex.getMessage());
        }
        return cellValue;
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
