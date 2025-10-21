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
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "Read the ExcelFile(xls) fullfilepath with Cell value rowNo,columnNo and with sheet sheet-index(starts with 1) and store into a variable testdata",
        description = "Read the specific cell value from the Excel file and store it in a runtime variable",
        applicationType = ApplicationType.WEB
)
public class StoreCellvalueXLS extends WebAction {

    @TestData(reference = "fullfilepath")
    private com.testsigma.sdk.TestData fullFilePath;

    @TestData(reference = "rowNo")
    private com.testsigma.sdk.TestData rowNo;

    @TestData(reference = "columnNo")
    private com.testsigma.sdk.TestData columnNo;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData runtimeKey;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating StoreCellvalueXLS execution");
        Result result = Result.SUCCESS;

        int sheetNum = Integer.parseInt(sheetIndex.getValue().toString());
        String fileLocation = fullFilePath.getValue().toString();
        File excelFile;

        FileInputStream inputStream = null;
        HSSFWorkbook workbook = null;

        try {
            logger.info("Excel file path: " + fileLocation);

            if (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) {
                excelFile = downloadFile(fileLocation);
            } else {
                excelFile = new File(fileLocation);
            }

            if (!excelFile.exists() || !excelFile.isFile()) {
                setErrorMessage("Invalid file path or file not found: " + fileLocation);
                return Result.FAILED;
            }

            inputStream = new FileInputStream(excelFile);
            workbook = new HSSFWorkbook(inputStream);

            if (sheetNum < 1 || sheetNum > workbook.getNumberOfSheets()) {
                setErrorMessage("Invalid sheet index: " + sheetNum);
                return Result.FAILED;
            }

            HSSFSheet sheet = workbook.getSheetAt(sheetNum - 1);
            int intRow = Integer.parseInt(rowNo.getValue().toString());
            int intColumn = Integer.parseInt(columnNo.getValue().toString());

            HSSFRow row = sheet.getRow(intRow);
            if (row == null) {
                setErrorMessage("Row " + intRow + " does not exist in sheet index " + sheetNum);
                return Result.FAILED;
            }

            HSSFCell cell = row.getCell(intColumn);
            String cellValue = "";
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                cellValue = "";
                logger.info("Successfully read cell value from cell sheet index");
                setSuccessMessage("Successfully read an empty/blank cell at row " + intRow + ", column " + intColumn);
            } else {
                cellValue = getCellValue(cell, workbook).trim();;
            }

            logger.info("Read cell value: " + cellValue);

            runTimeData.setKey(runtimeKey.getValue().toString());
            runTimeData.setValue(cellValue);

            setSuccessMessage("Successfully stored cell value '" + cellValue +
                    "' from row " + intRow + ", column " + intColumn + " (Sheet index " + sheetNum + ") into runtime variable '" +
                    runtimeKey.getValue() + "'");

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
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    switch (evaluator.evaluateInCell(cell).getCellType()) {
                        case NUMERIC:
                            cellValue = String.valueOf(cell.getNumericCellValue());
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
