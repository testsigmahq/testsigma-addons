package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Logger;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.Data;

@Data
@Action(
        actionText = "Compare excel files data from file1 with file2",
        description = "This addon compares the data from two Excel files to ensure their contents are identical.",
        applicationType = ApplicationType.WEB
)
public class ExcelComparison extends WebAction {

    @TestData(reference = "file1")
    private com.testsigma.sdk.TestData fileLocation1;

    @TestData(reference = "file2")
    private com.testsigma.sdk.TestData fileLocation2;

    @Override
    public Result execute() {
        logger.info("Initiating execution...");
        Result result = Result.SUCCESS;

        String file1 = fileLocation1.getValue().toString();
        String file2 = fileLocation2.getValue().toString();

        File excelFile1;
        File excelFile2;

        try {
            // Determine if file is a URL or local path
            excelFile1 = (file1.startsWith("http://") || file1.startsWith("https://")) ? downloadFile(file1) : new File(file1);
            excelFile2 = (file2.startsWith("http://") || file2.startsWith("https://")) ? downloadFile(file2) : new File(file2);

            // Validate file existence
            if (!excelFile1.exists() || !excelFile1.isFile()) {
                setErrorMessage("Invalid file: " + file1);
                return Result.FAILED;
            }
            if (!excelFile2.exists() || !excelFile2.isFile()) {
                setErrorMessage("Invalid file: " + file2);
                return Result.FAILED;
            }

            logger.debug("Reading excel file 1 from: " + excelFile1.getAbsolutePath());
            Map<Integer, List<String>> data1 = readExcel(excelFile1.getAbsolutePath());

            logger.debug("Reading excel file 2 from: " + excelFile2.getAbsolutePath());
            Map<Integer, List<String>> data2 = readExcel(excelFile2.getAbsolutePath());

            logger.debug("Comparing two excel files data.");
            if (compareExcel(data1, data2, logger)) {
                logger.debug("Data in both files is identical.");
                setSuccessMessage("The data in two excel files are identical.");
            } else {
                logger.debug("Data in both files is different.");
                result = Result.FAILED;
                setErrorMessage("The data in two excel files are different.");
            }

        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Error occurred while validating: " + e.getMessage());
            logger.warn(ExceptionUtils.getStackTrace(e));
        }

        return result;
    }

    public static Map<Integer, List<String>> readExcel(String fileLocation) throws IOException {
        Map<Integer, List<String>> data = new HashMap<>();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (FileInputStream fis = new FileInputStream(new File(fileLocation));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 0;

            for (Row row : sheet) {
                data.put(rowIndex, new ArrayList<>());

                for (int col = 0; col < row.getLastCellNum(); col++) {
                    Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                    switch (cell.getCellType()) {
                        case STRING:
                            data.get(rowIndex).add(cell.getStringCellValue().trim());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                data.get(rowIndex).add(cell.getLocalDateTimeCellValue().format(dateFormatter));
                            } else {
                                double num = cell.getNumericCellValue();
                                if (num == (long) num) {
                                    data.get(rowIndex).add(String.valueOf((long) num));
                                } else {
                                    data.get(rowIndex).add(decimalFormat.format(num));
                                }
                            }
                            break;
                        case BOOLEAN:
                            data.get(rowIndex).add(String.valueOf(cell.getBooleanCellValue()));
                            break;
                        case BLANK:
                            data.get(rowIndex).add("");
                            break;
                        default:
                            data.get(rowIndex).add("");
                            break;
                    }
                }
                rowIndex++;
            }
        }
        return data;
    }

    public static boolean compareExcel(Map<Integer, List<String>> data1, Map<Integer, List<String>> data2, Logger logger) {
        if (data1.size() != data2.size()) {
            logger.debug("Row count mismatch: (" + data1.size() + " vs " + data2.size() + ")");
            return false;
        }

        for (int rowIndex : data1.keySet()) {
            if (!data2.containsKey(rowIndex)) {
                logger.debug("Missing row index: " + (rowIndex + 1));
                return false;
            }

            List<String> row1 = data1.get(rowIndex);
            List<String> row2 = data2.get(rowIndex);

            if (row1.size() != row2.size()) {
                logger.debug("Column count mismatch at row: " + (rowIndex + 1));
                return false;
            }

            for (int colIndex = 0; colIndex < row1.size(); colIndex++) {
                String cell1 = row1.get(colIndex).trim();
                String cell2 = row2.get(colIndex).trim();

                if (!cell1.equalsIgnoreCase(cell2)) { // Case-insensitive comparison
                    logger.debug("Mismatch at row: " + (rowIndex + 1) + ", column: " + (colIndex + 1)
                            + " (" + cell1 + " != " + cell2 + ")");
                    return false;
                }
            }
        }
        return true;
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        File tempFile = File.createTempFile("downloaded-", ".xlsx");

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
