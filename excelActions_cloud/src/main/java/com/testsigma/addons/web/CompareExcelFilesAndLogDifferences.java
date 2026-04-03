package com.testsigma.addons.web;

import com.testsigma.addons.util.ExcelCellUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Compare two Excel files at first file path file-path-1 and second file path file-path-2",
        description = "Compares two Excel files (.xls or .xlsx) and logs each differing sheet/row/column location",
        applicationType = ApplicationType.WEB)
public class CompareExcelFilesAndLogDifferences extends WebAction {

    private static final int MAX_REPORTED_DIFFERENCES = 25;

    @TestData(reference = "file-path-1")
    private com.testsigma.sdk.TestData filePath1;

    @TestData(reference = "file-path-2")
    private com.testsigma.sdk.TestData filePath2;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating Excel file comparison");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String path1 = filePath1.getValue().toString();
            String path2 = filePath2.getValue().toString();

            logger.info("File 1: " + path1);
            logger.info("File 2: " + path2);

            File excelFile1 = getExcelFile(path1);
            File excelFile2 = getExcelFile(path2);

            int totalDifferences = 0;
            List<String> reportedDifferences = new ArrayList<>();

            try (InputStream input1 = new FileInputStream(excelFile1);
                 InputStream input2 = new FileInputStream(excelFile2);
                 Workbook workbook1 = WorkbookFactory.create(input1);
                 Workbook workbook2 = WorkbookFactory.create(input2)) {

                int sheetCount1 = workbook1.getNumberOfSheets();
                int sheetCount2 = workbook2.getNumberOfSheets();
                int maxSheets = Math.max(sheetCount1, sheetCount2);

                for (int s = 0; s < maxSheets; s++) {
                    Sheet sheet1 = s < sheetCount1 ? workbook1.getSheetAt(s) : null;
                    Sheet sheet2 = s < sheetCount2 ? workbook2.getSheetAt(s) : null;

                    if (sheet1 == null || sheet2 == null) {
                        totalDifferences++;
                        String message = "Sheet index " + s + " mismatch. " +
                                "File 1 sheet: " + getSheetName(sheet1) + ", " +
                                "File 2 sheet: " + getSheetName(sheet2);
                        logger.warn(message);
                        if (totalDifferences <= MAX_REPORTED_DIFFERENCES) {
                            reportedDifferences.add(message);
                        }
                        continue;
                    }

                    int maxRow = Math.max(sheet1.getLastRowNum(), sheet2.getLastRowNum());
                    for (int r = 0; r <= maxRow; r++) {
                        Row row1 = sheet1.getRow(r);
                        Row row2 = sheet2.getRow(r);
                        short lastCell1 = row1 != null ? row1.getLastCellNum() : -1;
                        short lastCell2 = row2 != null ? row2.getLastCellNum() : -1;
                        int maxCell = Math.max(lastCell1, lastCell2);

                        if (maxCell < 0) {
                            continue;
                        }

                        for (int c = 0; c < maxCell; c++) {
                            Cell cell1 = row1 != null ? row1.getCell(c) : null;
                            Cell cell2 = row2 != null ? row2.getCell(c) : null;
                            String value1 = ExcelCellUtils.getCellValueAsString(cell1);
                            String value2 = ExcelCellUtils.getCellValueAsString(cell2);

                            if (!value1.equals(value2)) {
                                totalDifferences++;
                                String message = "Sheet " + s + " (" + sheet1.getSheetName() + "), " +
                                        "Row " + r + ", Column " + c +
                                        " | File 1: '" + value1 + "' | File 2: '" + value2 + "'";
                                logger.warn(message);
                                if (totalDifferences <= MAX_REPORTED_DIFFERENCES) {
                                    reportedDifferences.add(message);
                                }
                            }
                        }
                    }
                }
            }

            if (totalDifferences == 0) {
                setSuccessMessage("No differences found between the two Excel files.");
                result = com.testsigma.sdk.Result.SUCCESS;
            } else {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("Found ").append(totalDifferences).append(" difference(s).<br>");
                errorMsg.append("First ").append(Math.min(totalDifferences, MAX_REPORTED_DIFFERENCES))
                        .append(" difference(s):<br>");
                for (String diff : reportedDifferences) {
                    errorMsg.append(diff).append("<br>");
                }
                setErrorMessage(errorMsg.toString());
                result = com.testsigma.sdk.Result.FAILED;
            }
        } catch (Exception e) {
            String errorMessage = "Error comparing Excel files: " + ExceptionUtils.getMessage(e);
            logger.warn("Full error: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private String getSheetName(Sheet sheet) {
        return sheet == null ? "[MISSING]" : "'" + sheet.getSheetName() + "'";
    }

    /**
     * Gets the Excel file from a local path or downloads from URL.
     */
    private File getExcelFile(String filePath) throws IOException {
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            logger.info("Downloading file from URL: " + filePath);
            return downloadFile(filePath);
        } else {
            logger.info("Using local file: " + filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File not found: " + filePath);
            }
            return file;
        }
    }

    /**
     * Downloads a file from URL to a temporary location.
     */
    private File downloadFile(String fileUrl) throws IOException {
        File tempFile = ExcelCellUtils.downloadUrlToTempFile(fileUrl);
        logger.info("Downloaded file to: " + tempFile.getAbsolutePath());
        return tempFile;
    }
}
