package com.testsigma.addons.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Utility class for common Excel cell operations
 */
public class ExcelCellUtils {

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

