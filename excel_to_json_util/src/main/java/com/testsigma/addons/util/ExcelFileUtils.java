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
        if (lowerUrl.contains(".xlsx")) {
            return ".xlsx";
        } else if (lowerUrl.contains(".xls")) {
            return ".xls";
        } else if (lowerUrl.contains(".csv")) {
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
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setAllowMissingColumnNames(true)
                 .build())) {

            List<String> headers = csvParser.getHeaderNames();
            List<CSVRecord> records = csvParser.getRecords();

            if (rowNumber > records.size()) {
                throw new IOException("Row number " + rowNumber + " exceeds total rows (" + records.size() + ") in CSV file");
            }

            CSVRecord record = records.get(rowNumber - 1); // Convert to 0-based index
            Map<String, String> rowData = new LinkedHashMap<>();

            for (int i = 0; i < record.size(); i++) {
                String header = i < headers.size() ? headers.get(i) : null;
                if (header == null || header.trim().isEmpty()) {
                    header = "Column_" + (i + 1);
                }
                String value = record.get(i);
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
        return convertExcelRowToJson(filePath, rowNumber, isXlsx, null, -1);
    }

    /**
     * Converts an Excel row to JSON format with sheet selection
     *
     * @param filePath Path to the Excel file
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @param isXlsx true if XLSX format, false if XLS format
     * @param sheetName Sheet name (null if using sheetIndex)
     * @param sheetIndex Sheet index (0-based, -1 if using sheetName)
     * @return JSON string representation of the row
     * @throws IOException if file cannot be read or row doesn't exist
     */
    public String convertExcelRowToJson(String filePath, int rowNumber, boolean isXlsx, String sheetName, int sheetIndex) throws IOException {
        Workbook workbook = null;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            if (isXlsx) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }

            Sheet sheet;
            if (sheetName != null && !sheetName.trim().isEmpty()) {
                // Use sheet name
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IOException("Sheet with name '" + sheetName + "' not found in the Excel file");
                }
            } else if (sheetIndex >= 0) {
                // Use sheet index
                if (sheetIndex >= workbook.getNumberOfSheets()) {
                    throw new IOException("Sheet index " + sheetIndex + " exceeds total sheets (" + workbook.getNumberOfSheets() + ") in Excel file");
                }
                sheet = workbook.getSheetAt(sheetIndex);
            } else {
                // Default to first sheet
                sheet = workbook.getSheetAt(0);
            }

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
        return convertRowToJson(filePath, rowNumber, null, -1);
    }

    /**
     * Converts a row from Excel or CSV file to JSON format with sheet selection.
     * Automatically detects file type based on extension.
     *
     * @param filePath Path to the file (can be URL or local path)
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @param sheetName Sheet name (null if using sheetIndex, ignored for CSV files)
     * @param sheetIndex Sheet index (0-based, -1 if using sheetName, ignored for CSV files)
     * @return JSON string representation of the row
     * @throws IOException if file cannot be read or row doesn't exist
     */
    public String convertRowToJson(String filePath, int rowNumber, String sheetName, int sheetIndex) throws IOException {
        // Convert URL to file if needed
        File file = urlToFileConverter(filePath);
        String fileName = file.getName().toLowerCase();

        // Determine file type and process accordingly
        if (fileName.contains(".csv")) {
            return convertCsvRowToJson(file.getAbsolutePath(), rowNumber);
        } else if (fileName.contains(".xlsx")) {
            return convertExcelRowToJson(file.getAbsolutePath(), rowNumber, true, sheetName, sheetIndex);
        } else if (fileName.contains(".xls")) {
            return convertExcelRowToJson(file.getAbsolutePath(), rowNumber, false, sheetName, sheetIndex);
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

    /**
     * Converts Excel column name (A, B, C, etc.) to 0-based column index
     * @param columnName Column name like "A", "B", "AA", etc.
     * @return 0-based column index
     */
    private int columnNameToIndex(String columnName) {
        int index = 0;
        columnName = columnName.toUpperCase().trim();
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("Invalid column name: " + columnName);
            }
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Convert to 0-based
    }

    /**
     * Gets a single column value from a specific row
     * @param filePath Path to the file (can be URL or local path)
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @param columnIdentifier Column identifier: can be index (0-based), column name (A, B, C), or header value
     * @param sheetName Sheet name (null if using sheetIndex, ignored for CSV files)
     * @param sheetIndex Sheet index (0-based, -1 if using sheetName, ignored for CSV files)
     * @return Column value as string
     * @throws IOException if file cannot be read or row/column doesn't exist
     */
    public String getColumnValue(String filePath, int rowNumber, String columnIdentifier, String sheetName, int sheetIndex) throws IOException {
        File file = urlToFileConverter(filePath);
        String fileName = file.getName().toLowerCase();

        if (fileName.contains(".csv")) {
            return getCsvColumnValue(file.getAbsolutePath(), rowNumber, columnIdentifier);
        } else if (fileName.contains(".xlsx")) {
            return getExcelColumnValue(file.getAbsolutePath(), rowNumber, columnIdentifier, true, sheetName, sheetIndex);
        } else if (fileName.contains(".xls")) {
            return getExcelColumnValue(file.getAbsolutePath(), rowNumber, columnIdentifier, false, sheetName, sheetIndex);
        } else {
            throw new IOException("Unsupported file format. Supported formats: .csv, .xls, .xlsx");
        }
    }

    /**
     * Gets multiple column values from a specific row and returns as JSON
     * @param filePath Path to the file (can be URL or local path)
     * @param rowNumber Row number (1-based, where 1 is the first data row after header)
     * @param columnIdentifiers Comma-separated column identifiers: can be indices (0-based), column names (A, B, C), or header values
     * @param sheetName Sheet name (null if using sheetIndex, ignored for CSV files)
     * @param sheetIndex Sheet index (0-based, -1 if using sheetName, ignored for CSV files)
     * @return JSON string with column values
     * @throws IOException if file cannot be read or row/columns don't exist
     */
    public String getColumnsToJson(String filePath, int rowNumber, String columnIdentifiers, String sheetName, int sheetIndex) throws IOException {
        File file = urlToFileConverter(filePath);
        String fileName = file.getName().toLowerCase();

        String[] columns = columnIdentifiers.split(",");
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }

        if (fileName.contains(".csv")) {
            return getCsvColumnsToJson(file.getAbsolutePath(), rowNumber, columns);
        } else if (fileName.contains(".xlsx")) {
            return getExcelColumnsToJson(file.getAbsolutePath(), rowNumber, columns, true, sheetName, sheetIndex);
        } else if (fileName.contains(".xls")) {
            return getExcelColumnsToJson(file.getAbsolutePath(), rowNumber, columns, false, sheetName, sheetIndex);
        } else {
            throw new IOException("Unsupported file format. Supported formats: .csv, .xls, .xlsx");
        }
    }

    private String getCsvColumnValue(String filePath, int rowNumber, String columnIdentifier) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setAllowMissingColumnNames(true)
                 .build())) {

            List<String> headers = csvParser.getHeaderNames();
            List<CSVRecord> records = csvParser.getRecords();

            if (rowNumber > records.size()) {
                throw new IOException("Row number " + rowNumber + " exceeds total rows (" + records.size() + ") in CSV file");
            }

            CSVRecord record = records.get(rowNumber - 1);
            int columnIndex = resolveColumnIndex(headers, columnIdentifier);
            
            if (columnIndex < 0 || columnIndex >= headers.size()) {
                throw new IOException("Column '" + columnIdentifier + "' not found in CSV file");
            }

            String value = record.get(columnIndex);
            return value != null ? value : "";
        }
    }

    private String getCsvColumnsToJson(String filePath, int rowNumber, String[] columnIdentifiers) throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setAllowMissingColumnNames(true)
                 .build())) {

            List<String> headers = csvParser.getHeaderNames();
            List<CSVRecord> records = csvParser.getRecords();

            if (rowNumber > records.size()) {
                throw new IOException("Row number " + rowNumber + " exceeds total rows (" + records.size() + ") in CSV file");
            }

            CSVRecord record = records.get(rowNumber - 1);
            Map<String, String> columnData = new LinkedHashMap<>();

            for (String columnId : columnIdentifiers) {
                int columnIndex = resolveColumnIndex(headers, columnId);
                if (columnIndex >= 0 && columnIndex < headers.size()) {
                    String header = headers.get(columnIndex);
                    String value = record.get(columnIndex);
                    columnData.put(header, value != null ? value : "");
                } else {
                    throw new IOException("Column '" + columnId + "' not found in CSV file");
                }
            }

            return convertMapToJson(columnData);
        }
    }

    private String getExcelColumnValue(String filePath, int rowNumber, String columnIdentifier, boolean isXlsx, String sheetName, int sheetIndex) throws IOException {
        Workbook workbook = null;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            if (isXlsx) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }

            Sheet sheet = getSheet(workbook, sheetName, sheetIndex);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("Header row is empty or file is invalid");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            int columnIndex = resolveColumnIndex(headers, columnIdentifier);
            if (columnIndex < 0 || columnIndex >= headers.size()) {
                throw new IOException("Column '" + columnIdentifier + "' not found in Excel file");
            }

            Row dataRow = sheet.getRow(rowNumber);
            if (dataRow == null) {
                throw new IOException("Row number " + rowNumber + " does not exist in the Excel file");
            }

            Cell cell = dataRow.getCell(columnIndex);
            return getCellValueAsString(cell);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private String getExcelColumnsToJson(String filePath, int rowNumber, String[] columnIdentifiers, boolean isXlsx, String sheetName, int sheetIndex) throws IOException {
        Workbook workbook = null;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            if (isXlsx) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }

            Sheet sheet = getSheet(workbook, sheetName, sheetIndex);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("Header row is empty or file is invalid");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            Row dataRow = sheet.getRow(rowNumber);
            if (dataRow == null) {
                throw new IOException("Row number " + rowNumber + " does not exist in the Excel file");
            }

            Map<String, String> columnData = new LinkedHashMap<>();
            for (String columnId : columnIdentifiers) {
                int columnIndex = resolveColumnIndex(headers, columnId);
                if (columnIndex >= 0 && columnIndex < headers.size()) {
                    String header = headers.get(columnIndex);
                    Cell cell = dataRow.getCell(columnIndex);
                    String value = getCellValueAsString(cell);
                    columnData.put(header, value);
                } else {
                    throw new IOException("Column '" + columnId + "' not found in Excel file");
                }
            }

            return convertMapToJson(columnData);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private Sheet getSheet(Workbook workbook, String sheetName, int sheetIndex) throws IOException {
        if (sheetName != null && !sheetName.trim().isEmpty()) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IOException("Sheet with name '" + sheetName + "' not found in the Excel file");
            }
            return sheet;
        } else if (sheetIndex >= 0) {
            if (sheetIndex >= workbook.getNumberOfSheets()) {
                throw new IOException("Sheet index " + sheetIndex + " exceeds total sheets (" + workbook.getNumberOfSheets() + ") in Excel file");
            }
            return workbook.getSheetAt(sheetIndex);
        } else {
            return workbook.getSheetAt(0);
        }
    }

    /**
     * Resolves column identifier to 0-based index
     * Supports: column index (0-based), column name (A, B, C), or header value
     */
    private int resolveColumnIndex(List<String> headers, String columnIdentifier) throws IOException {
        String colId = columnIdentifier.trim();
        
        // Try as column name (A, B, C, etc.)
        if (colId.matches("^[A-Z]+$")) {
            try {
                return columnNameToIndex(colId);
            } catch (IllegalArgumentException e) {
                // Not a valid column name, continue to other methods
            }
        }
        
        // Try as numeric index (0-based)
        try {
            int index = Integer.parseInt(colId);
            if (index >= 0 && index < headers.size()) {
                return index;
            }
            throw new IOException("Column index " + index + " is out of range. Valid range: 0 to " + (headers.size() - 1));
        } catch (NumberFormatException e) {
            // Not a number, try as header value
        }
        
        // Try as header value
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(colId)) {
                return i;
            }
        }
        
        throw new IOException("Column '" + columnIdentifier + "' not found. Use column index (0-based), column name (A, B, C), or header value");
    }
}

