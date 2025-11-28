package com.testsigma.addons.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.testsigma.sdk.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExcelFileUtils {

    Logger logger;
    public ExcelFileUtils(Logger logger) {
        this.logger = logger;
    }

    /**
     * Converts a URL (HTTP/HTTPS) or local file path to a File object.
     * Handles different file formats (CSV, XLS, XLSX).
     *
     * @param url The URL or local file path
     * @return File object representing the file
     * @throws RuntimeException if unable to access the file
     */
    public File urlToFileConverter(String url) {

        try {
            logger.info("url " + url);
            logger.info(String.valueOf(url.startsWith("https://") || url.startsWith("http://")));

            if (url.startsWith("https://") || url.startsWith("http://")) {
                // file name use the current time stamp
                logger.info("Given is s3 url");
                URL urlObject = new URL(url);
                
                // Determine file extension from URL or default to .csv
                String extension = getFileExtensionFromUrl(url);
                File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), extension);
                tempFile.deleteOnExit();
                
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file " + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                File file = new File(url);
                if (!file.exists()) {
                    throw new FileNotFoundException("File not found: " + url);
                }
                return file;
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            logger.debug(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Unable to access the given file, please check the given input: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts file extension from URL or returns default .csv
     */
    private String getFileExtensionFromUrl(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".xlsx")) {
            return ".xlsx";
        } else if (lowerUrl.endsWith(".xls")) {
            return ".xls";
        } else if (lowerUrl.endsWith(".csv")) {
            return ".csv";
        }
        // Default to .csv if extension cannot be determined
        return ".csv";
    }

    /**
     * Converts a CSV row to JSON format
     *
     * @param filePath Path to the CSV file
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @return JSON string representation of the row
     * @throws IOException if file cannot be read or row doesn't exist
     */
    public String convertCsvRowToJson(String filePath, int rowNumber) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().build())) {

            List<String> headers = csvParser.getHeaderNames();
            List<CSVRecord> records = csvParser.getRecords();

            if (rowNumber > records.size()) {
                throw new IOException("Row number " + rowNumber + " exceeds total rows (" + records.size() + ") in CSV file");
            }

            CSVRecord record = records.get(rowNumber - 1); // Convert to 0-based index
            Map<String, String> rowData = new LinkedHashMap<>();

            for (String header : headers) {
                String value = record.get(header);
                rowData.put(header, value != null ? value : "");
            }

            return convertMapToJson(rowData);
        }
    }

    /**
     * Converts an Excel row to JSON format
     *
     * @param filePath Path to the Excel file
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @param isXlsx true if XLSX format, false if XLS format
     * @return JSON string representation of the row
     * @throws IOException if file cannot be read or row doesn't exist
     */
    public String convertExcelRowToJson(String filePath, int rowNumber, boolean isXlsx) throws IOException {
        Workbook workbook = null;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            if (isXlsx) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet

            // Read header row (row 0)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("Header row is empty or file is invalid");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            // Read data row
            Row dataRow = sheet.getRow(rowNumber); // rowNumber is 1-based, but Excel rows are 0-based
            if (dataRow == null) {
                throw new IOException("Row number " + rowNumber + " does not exist in the Excel file");
            }

            Map<String, String> rowData = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Cell cell = dataRow.getCell(i);
                String value = cell != null ? getCellValueAsString(cell) : "";
                rowData.put(header, value);
            }

            return convertMapToJson(rowData);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    /**
     * Converts a row from Excel or CSV file to JSON format.
     * Automatically detects file type based on extension.
     *
     * @param filePath Path to the file (can be URL or local path)
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @return JSON string representation of the row
     * @throws IOException if file cannot be read or row doesn't exist
     */
    public String convertRowToJson(String filePath, int rowNumber) throws IOException {
        // Convert URL to file if needed
        File file = urlToFileConverter(filePath);
        String fileName = file.getName().toLowerCase();

        // Determine file type and process accordingly
        if (fileName.endsWith(".csv")) {
            return convertCsvRowToJson(file.getAbsolutePath(), rowNumber);
        } else if (fileName.endsWith(".xlsx")) {
            return convertExcelRowToJson(file.getAbsolutePath(), rowNumber, true);
        } else if (fileName.endsWith(".xls")) {
            return convertExcelRowToJson(file.getAbsolutePath(), rowNumber, false);
        } else {
            throw new IOException("Unsupported file format. Supported formats: .csv, .xls, .xlsx");
        }
    }

    /**
     * Gets cell value as string, handling different cell types
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format numeric values without unnecessary decimals
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Converts a Map to JSON string format
     */
    private String convertMapToJson(Map<String, String> data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            json.append("\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escapes special characters in JSON strings
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

