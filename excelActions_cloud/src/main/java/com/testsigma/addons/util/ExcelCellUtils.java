package com.testsigma.addons.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Utility class for common Excel cell operations and downloading workbooks from URLs.
 */
public class ExcelCellUtils {

    private static final Random FILENAME_RANDOM = new Random();

    /**
     * Downloads a remote file to the system temp directory using a short basename (≤63 chars)
     * derived from the URL path (percent-decoded), for UIs that limit filename length.
     */
    public static File downloadUrlToTempFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String rawName = Paths.get(url.getPath()).getFileName().toString();
        String fileName;
        try {
            fileName = URLDecoder.decode(rawName, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            fileName = rawName;
        }
        String ext = tempSuffixFromFileName(fileName);
        String stem = fileStem(fileName);
        stem = sanitizeTempStem(stem);
        File tempFile = createShortNamedTempFile(stem, ext);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private static File createShortNamedTempFile(String stem, String ext) throws IOException {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
        final int maxBase = 63;
        final int uniqueLen = 8;
        int maxStem = maxBase - ext.length() - 1 - uniqueLen;
        if (maxStem < 1) {
            maxStem = 1;
        }
        if (stem.length() > maxStem) {
            stem = stem.substring(0, maxStem);
        }
        for (int attempt = 0; attempt < 64; attempt++) {
            String unique = String.format("%08x", FILENAME_RANDOM.nextInt());
            String name = stem + "-" + unique + ext;
            Path path = dir.resolve(name);
            try {
                Files.createFile(path);
                return path.toFile();
            } catch (FileAlreadyExistsException ignored) {
                // retry
            }
        }
        return File.createTempFile("ex-", ext);
    }

    private static String fileStem(String name) {
        int dot = name.lastIndexOf('.');
        if (dot <= 0) {
            return name.isEmpty() ? "excel" : name;
        }
        return name.substring(0, dot);
    }

    private static String sanitizeTempStem(String stem) {
        if (stem == null || stem.isEmpty()) {
            return "excel";
        }
        String s = stem.replaceAll("[^a-zA-Z0-9._()-]", "_");
        s = s.replaceAll("_+", "_");
        if (s.isEmpty() || ".".equals(s)) {
            return "excel";
        }
        return s;
    }

    private static String tempSuffixFromFileName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return ".tmp";
        }
        String ext = fileName.substring(dot);
        if (ext.length() > 12) {
            return ".tmp";
        }
        return ext;
    }

    /**
     * Gets a cell value as a String, handling different cell types
     * 
     * @param sheet The Excel sheet
     * @param rowIdx The row index (0-based)
     * @param colIdx The column index (0-based)
     * @return The cell value as a String
     */
    public static String getCellValueAsString(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            return "[EMPTY ROW]";
        }

        Cell cell = row.getCell(colIdx);
        return getCellValueAsString(cell);
    }

    /**
     * Gets a cell value as a String, handling different cell types
     * 
     * @param cell The Excel cell
     * @return The cell value as a String
     */
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "[EMPTY CELL]";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    // Check if it's a whole number
                    if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                        return String.valueOf((long) numValue);
                    }
                    return Double.toString(numValue);
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return Double.toString(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return "[BLANK]";
            default:
                return "[UNKNOWN]";
        }
    }

    /**
     * Gets a cell value as a String with custom handling for empty/null values
     * This version returns empty string for blank cells instead of placeholders
     * 
     * @param cell The Excel cell
     * @return The cell value as a String (empty string for null/blank cells)
     */
    public static String getCellValueOrEmpty(Cell cell) {
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
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                        return String.valueOf((long) numValue);
                    }
                    return Double.toString(numValue);
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return Double.toString(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return "";
            default:
                return "N/A";
        }
    }
}

