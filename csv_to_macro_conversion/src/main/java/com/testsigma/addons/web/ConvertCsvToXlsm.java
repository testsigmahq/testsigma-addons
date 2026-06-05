package com.testsigma.addons.web;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Data
@Action(
        actionText = "Convert CSV file csv-file-path into XLSM file xlsm-file-path in sheet sheet-name and store output path in variable-name",
        description = "Reads all data from the given CSV file and writes it into the specified sheet of an XLSM " +
                "(macro-enabled Excel) workbook. If the XLSM file does not exist, a new one is created. " +
                "The sheet is cleared before writing. Supports local paths and URLs for the CSV file. " +
                "Stores the absolute path of the resulting XLSM file in a runtime variable.",
        applicationType = ApplicationType.WEB
)
public class ConvertCsvToXlsm extends WebAction {

    @TestData(reference = "csv-file-path")
    private com.testsigma.sdk.TestData csvFilePath;

    @TestData(reference = "xlsm-file-path")
    private com.testsigma.sdk.TestData xlsmFilePath;

    @TestData(reference = "sheet-name")
    private com.testsigma.sdk.TestData sheetName;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating ConvertCsvToXlsm action");

        String csvPath = csvFilePath.getValue().toString().trim();
        String xlsmPath = xlsmFilePath.getValue().toString().trim();
        String sheet = sheetName.getValue().toString().trim();

        if (sheet.isEmpty()) {
            setErrorMessage("Sheet name must not be empty.");
            return Result.FAILED;
        }

        File csvFile;
        try {
            csvFile = resolveFile(csvPath, "csv");
        } catch (IOException e) {
            setErrorMessage("Failed to load CSV file: " + e.getMessage());
            return Result.FAILED;
        }

        if (csvFile == null || !csvFile.exists()) {
            setErrorMessage("CSV file not found: " + csvPath);
            return Result.FAILED;
        }

        List<String[]> csvRows;
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            csvRows = reader.readAll();
        } catch (IOException | CsvException e) {
            setErrorMessage("Failed to read CSV file: " + e.getMessage());
            return Result.FAILED;
        }

        logger.info("CSV rows read: " + csvRows.size());

        File xlsmFile = new File(xlsmPath);
        XSSFWorkbook workbook = null;
        try {
            if (xlsmFile.exists()) {
                logger.info("Opening existing XLSM file: " + xlsmPath);
                try (FileInputStream fis = new FileInputStream(xlsmFile)) {
                    workbook = new XSSFWorkbook(fis);
                }
            } else {
                logger.info("XLSM file not found — creating new workbook at: " + xlsmPath);
                workbook = new XSSFWorkbook();
                File parentDir = xlsmFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
            }

            Sheet targetSheet = workbook.getSheet(sheet);
            if (targetSheet == null) {
                logger.info("Sheet '" + sheet + "' not found — creating it.");
                targetSheet = workbook.createSheet(sheet);
            } else {
                // Clear existing rows in the sheet
                int lastRow = targetSheet.getLastRowNum();
                for (int i = lastRow; i >= 0; i--) {
                    Row existingRow = targetSheet.getRow(i);
                    if (existingRow != null) {
                        targetSheet.removeRow(existingRow);
                    }
                }
                logger.info("Cleared " + (lastRow + 1) + " existing rows from sheet '" + sheet + "'.");
            }

            // Write CSV data into the sheet
            for (int r = 0; r < csvRows.size(); r++) {
                String[] columns = csvRows.get(r);
                Row row = targetSheet.createRow(r);
                for (int c = 0; c < columns.length; c++) {
                    row.createCell(c).setCellValue(columns[c]);
                }
            }

            logger.info("Written " + csvRows.size() + " rows to sheet '" + sheet + "'.");

            try (FileOutputStream fos = new FileOutputStream(xlsmFile)) {
                workbook.write(fos);
            }

            logger.info("XLSM file saved: " + xlsmFile.getAbsolutePath());

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(xlsmFile.getAbsolutePath());

            setSuccessMessage("Successfully wrote " + csvRows.size() + " rows from CSV into sheet '" + sheet +
                    "' of XLSM file. Path stored in '" + variableName.getValue() + "': " +
                    xlsmFile.getAbsolutePath());
            return Result.SUCCESS;

        } catch (IOException e) {
            setErrorMessage("Failed to write XLSM file: " + e.getMessage());
            logger.warn("IO error during XLSM write: " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Unexpected error: " + e.getMessage());
            logger.warn("Unexpected error: " + e.getMessage());
            return Result.FAILED;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    logger.warn("Failed to close workbook: " + e.getMessage());
                }
            }
        }
    }

    private File resolveFile(String pathOrUrl, String extension) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            String uniqueName = "temp_" + System.currentTimeMillis() + "." + extension;
            logger.info("Downloading from URL: " + pathOrUrl + " as " + uniqueName);
            String tempPath = FileUtils.getTempDirectoryPath() + File.separator + uniqueName;
            File tempFile = new File(tempPath);
            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile, 10000, 10000);
            return tempFile;
        }
        return new File(pathOrUrl);
    }
}
