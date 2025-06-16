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
@Action(
        actionText = "Excel: Read the entire Column of the Excel local filePath using the Column Index and Sheet Index sheet-index and store it in a " +
                "variable named testdata",
        description = "Read the entire Column of the Excel file from given filepath or URL using the Column number and store it in a variable named testdata",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ReadColumnFromExcelByIndex extends WebAction {

    @TestData(reference = "filePath")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "Index")
    private com.testsigma.sdk.TestData columnIndex_;

    @TestData(reference = "sheet-index")
    private com.testsigma.sdk.TestData sheetIndex_;

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

                // Validate and parse sheet index (1-based to 0-based)
                int userSheetIndex;
                try {
                    userSheetIndex = Integer.parseInt(sheetIndex_.getValue().toString());
                    if (userSheetIndex < 1) {
                        logger.warn("Sheet index must be greater than or equal to 1. Provided: " + userSheetIndex);
                        setErrorMessage("Sheet index must be greater than or equal to 1. Provided: " + userSheetIndex);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid sheet index format: " + sheetIndex_.getValue().toString());
                    setErrorMessage("Invalid sheet index format. Must be a positive integer. Provided: " + sheetIndex_.getValue().toString());
                    return com.testsigma.sdk.Result.FAILED;
                }

                int sheetIndex = userSheetIndex - 1;
                if (sheetIndex >= workbook.getNumberOfSheets()) {
                    logger.warn("Sheet index " + userSheetIndex + " is out of range. Workbook contains only " + workbook.getNumberOfSheets() + " sheet(s).");
                    setErrorMessage("Sheet index " + userSheetIndex + " is out of range. Workbook contains only " + workbook.getNumberOfSheets() + " sheet(s).");
                    return com.testsigma.sdk.Result.FAILED;
                }

                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                logger.info("Processing sheet at index: " + userSheetIndex + " (0-based: " + sheetIndex + ")");

                // Validate and parse column index (0-based)
                int columnIndex;
                try {
                    columnIndex = Integer.parseInt(columnIndex_.getValue().toString());
                    if (columnIndex < 0) {
                        logger.warn("Column index must be non-negative. Provided: " + columnIndex);
                        setErrorMessage("Column index must be non-negative. Provided: " + columnIndex);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid column index format: " + columnIndex_.getValue().toString());
                    setErrorMessage("Invalid column index format. Must be a non-negative integer. Provided: " + columnIndex_.getValue().toString());
                    return com.testsigma.sdk.Result.FAILED;
                }

                // Check if column index is within range for the sheet
                int maxColumns = sheet.getRow(0) != null ? sheet.getRow(0).getLastCellNum() : 0;
                if (columnIndex >= maxColumns) {
                    logger.warn("Column index " + columnIndex + " is out of range. Sheet has " + maxColumns + " columns (0-based).");
                    setErrorMessage("Column index " + columnIndex + " is out of range. Sheet has " + maxColumns + " columns.");
                    return com.testsigma.sdk.Result.FAILED;
                }

                for (int rowIndex1 = 0; rowIndex1 <= sheet.getLastRowNum(); rowIndex1++) {
                    var row1 = sheet.getRow(rowIndex1);
                    if (row1 != null) {
                        Cell cell = row1.getCell(columnIndex);
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
                            entireFieldValues.append(CellValue);
                            entireFieldValues.append(",");
                        } else {
                            logger.info("Ignoring next cells as we found empty cell");
                            break;
                        }
                    }
                }

                // Remove trailing comma if any
                if (entireFieldValues.length() > 0) {
                    entireFieldValues.deleteCharAt(entireFieldValues.length() - 1);
                }

                logger.info("Storing values");
                runTimeData.setKey(testData2.getValue().toString());
                runTimeData.setValue(entireFieldValues.toString());
                logger.info("Extracted column values: " + entireFieldValues.toString());

                setSuccessMessage("Extracted the Complete Column Values and Stored it in variable "
                        + testData2.getValue().toString() + " = " + runTimeData.getValue());

            } catch (IOException e) {
                String errorMessage = "Error reading Excel file: " + ExceptionUtils.getStackTrace(e);
                setErrorMessage(errorMessage);
                logger.warn(errorMessage);
                return com.testsigma.sdk.Result.FAILED;
            }

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        } finally {
            if (fileLocation != null && (fileLocation.startsWith("http://") || fileLocation.startsWith("https://")) && excelFile != null) {
                excelFile.delete(); // Delete temp file if it was a download
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