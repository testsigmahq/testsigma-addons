package com.testsigma.addons.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TimeUtil {

    // Enum to represent format types
    public enum FormatType {
        TIME_ONLY,
        DATE_ONLY,
        DATETIME
    }

    // Helper class to hold detected format information
    public static class DetectedFormat {
        public String formatStr;
        public FormatType type;
        public DateTimeFormatter formatter;

        public DetectedFormat(String formatStr, FormatType type, DateTimeFormatter formatter) {
            this.formatStr = formatStr;
            this.type = type;
            this.formatter = formatter;
        }
    }

    // Helper class for format information
    private static class FormatInfo {
        String formatStr;
        FormatType type;
        DateTimeFormatter formatter;

        FormatInfo(String formatStr, FormatType type, DateTimeFormatter formatter) {
            this.formatStr = formatStr;
            this.type = type;
            this.formatter = formatter;
        }
    }

    /**
     * Determines the format type from a format string
     */
    public static FormatType getFormatType(String formatStr) {
        if (formatStr.equals("HH:MM") || formatStr.equals("HH:MM:SS") || 
            formatStr.equals("hh:mm AM/PM") || formatStr.equals("hh:mm:ss AM/PM")) {
            return FormatType.TIME_ONLY;
        } else if (formatStr.equals("YYYY-MM-DD") || formatStr.equals("DD-MM-YYYY") || 
                   formatStr.equals("MM/DD/YYYY") || formatStr.equals("DD/MM/YYYY")) {
            return FormatType.DATE_ONLY;
        } else {
            return FormatType.DATETIME;
        }
    }

    /**
     * Auto-detects the input format of a time string
     */
    public static DetectedFormat detectInputFormat(String inputTimeStr) throws IllegalArgumentException {
        // List of all possible formats to try
        List<FormatInfo> formats = new ArrayList<>();
        
        // Time-only formats
        formats.add(new FormatInfo("HH:MM", FormatType.TIME_ONLY, DateTimeFormatter.ofPattern("HH:mm")));
        formats.add(new FormatInfo("HH:MM:SS", FormatType.TIME_ONLY, DateTimeFormatter.ofPattern("HH:mm:ss")));
        formats.add(new FormatInfo("hh:mm AM/PM", FormatType.TIME_ONLY, DateTimeFormatter.ofPattern("hh:mm a")));
        formats.add(new FormatInfo("hh:mm:ss AM/PM", FormatType.TIME_ONLY, DateTimeFormatter.ofPattern("hh:mm:ss a")));
        
        // Date-only formats
        formats.add(new FormatInfo("YYYY-MM-DD", FormatType.DATE_ONLY, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        formats.add(new FormatInfo("DD-MM-YYYY", FormatType.DATE_ONLY, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        formats.add(new FormatInfo("MM/DD/YYYY", FormatType.DATE_ONLY, DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        formats.add(new FormatInfo("DD/MM/YYYY", FormatType.DATE_ONLY, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Date and time formats
        formats.add(new FormatInfo("YYYY-MM-DD HH:MM", FormatType.DATETIME, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        formats.add(new FormatInfo("YYYY-MM-DD HH:MM:SS", FormatType.DATETIME, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        formats.add(new FormatInfo("DD-MM-YYYY HH:MM", FormatType.DATETIME, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        formats.add(new FormatInfo("DD-MM-YYYY HH:MM:SS", FormatType.DATETIME, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        formats.add(new FormatInfo("MM/DD/YYYY HH:MM", FormatType.DATETIME, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
        formats.add(new FormatInfo("MM/DD/YYYY HH:MM:SS", FormatType.DATETIME, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
        formats.add(new FormatInfo("DD/MM/YYYY HH:MM", FormatType.DATETIME, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        formats.add(new FormatInfo("DD/MM/YYYY HH:MM:SS", FormatType.DATETIME, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        formats.add(new FormatInfo("YYYY-MM-DD hh:mm AM/PM", FormatType.DATETIME, DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")));
        formats.add(new FormatInfo("YYYY-MM-DD hh:mm:ss AM/PM", FormatType.DATETIME, DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")));
        formats.add(new FormatInfo("YYYY-MM-DDTHH:MM:SS", FormatType.DATETIME, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        
        // Handle ISO format with Z
        if (inputTimeStr.endsWith("Z")) {
            try {
                String withoutZ = inputTimeStr.replace("Z", "");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime.parse(withoutZ, formatter);
                return new DetectedFormat("YYYY-MM-DDTHH:MM:SSZ", FormatType.DATETIME, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (DateTimeParseException e) {
                // Continue to try other formats
            }
        }
        
        // Try each format
        for (FormatInfo formatInfo : formats) {
            try {
                if (formatInfo.type == FormatType.TIME_ONLY) {
                    LocalTime.parse(inputTimeStr, formatInfo.formatter);
                } else if (formatInfo.type == FormatType.DATE_ONLY) {
                    LocalDate.parse(inputTimeStr, formatInfo.formatter);
                } else {
                    LocalDateTime.parse(inputTimeStr, formatInfo.formatter);
                }
                return new DetectedFormat(formatInfo.formatStr, formatInfo.type, formatInfo.formatter);
            } catch (DateTimeParseException e) {
                // Try next format
                continue;
            }
        }
        
        throw new IllegalArgumentException("Unable to detect input format for: " + inputTimeStr);
    }

    /**
     * Validates conversion compatibility between format types
     */
    public static void validateConversion(FormatType inputType, FormatType outputType) throws IllegalArgumentException {
        // Time-only to date-only is not allowed
        if (inputType == FormatType.TIME_ONLY && outputType == FormatType.DATE_ONLY) {
            throw new IllegalArgumentException("can't convert to this format");
        }
        // Date-only to time-only is not allowed
        if (inputType == FormatType.DATE_ONLY && outputType == FormatType.TIME_ONLY) {
            throw new IllegalArgumentException("can't convert to this format");
        }
    }

    /**
     * Parses an input time string based on auto-detected format
     */
    public static LocalDateTime parseTime(String inputTimeStr) throws IllegalArgumentException {
        DetectedFormat detectedInput = detectInputFormat(inputTimeStr);
        String detectedInputFormatStr = detectedInput.formatStr;
        
        // Parse the input time based on the detected format
        if (detectedInput.type == FormatType.TIME_ONLY) {
            LocalTime timeOnly = LocalTime.parse(inputTimeStr, detectedInput.formatter);
            return LocalDateTime.of(LocalDate.now(), timeOnly);
        } else if (detectedInput.type == FormatType.DATE_ONLY) {
            LocalDate dateOnly = LocalDate.parse(inputTimeStr, detectedInput.formatter);
            return dateOnly.atStartOfDay();
        } else {
            // Handle ISO format with Z
            if (detectedInputFormatStr.equals("YYYY-MM-DDTHH:MM:SSZ")) {
                String withoutZ = inputTimeStr.replace("Z", "");
                return LocalDateTime.parse(withoutZ, detectedInput.formatter);
            } else {
                return LocalDateTime.parse(inputTimeStr, detectedInput.formatter);
            }
        }
    }

    /**
     * Formats a LocalDateTime to the specified format string
     */
    public static String formatTime(LocalDateTime localTime, String format) throws IllegalArgumentException {
        if (localTime == null) {
            throw new IllegalArgumentException("LocalDateTime cannot be null");
        }
        
        DateTimeFormatter outputFormatter;
        String formattedResult;
        
        switch(format) {
            // Time-only formats
            case "HH:MM":
                outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "HH:MM:SS":
                outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                formattedResult = localTime.format(outputFormatter);
                break;
            // Date-only formats
            case "YYYY-MM-DD":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "DD-MM-YYYY":
                outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "MM/DD/YYYY":
                outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "DD/MM/YYYY":
                outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                formattedResult = localTime.format(outputFormatter);
                break;
            // Date and time formats with dash separator
            case "YYYY-MM-DD HH:MM":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "YYYY-MM-DD HH:MM:SS":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "DD-MM-YYYY HH:MM":
                outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "DD-MM-YYYY HH:MM:SS":
                outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                formattedResult = localTime.format(outputFormatter);
                break;
            // Date and time formats with slash separator
            case "MM/DD/YYYY HH:MM":
                outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "MM/DD/YYYY HH:MM:SS":
                outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "DD/MM/YYYY HH:MM":
                outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "DD/MM/YYYY HH:MM:SS":
                outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                formattedResult = localTime.format(outputFormatter);
                break;
            // 12-hour format (time only)
            case "hh:mm AM/PM":
                outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "hh:mm:ss AM/PM":
                outputFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
                formattedResult = localTime.format(outputFormatter);
                break;
            // 12-hour format (date and time)
            case "YYYY-MM-DD hh:mm AM/PM":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "YYYY-MM-DD hh:mm:ss AM/PM":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
                formattedResult = localTime.format(outputFormatter);
                break;
            // ISO 8601 formats
            case "YYYY-MM-DDTHH:MM:SS":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                formattedResult = localTime.format(outputFormatter);
                break;
            case "YYYY-MM-DDTHH:MM:SSZ":
                outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                formattedResult = localTime.format(outputFormatter);
                break;
            default:
                throw new IllegalArgumentException("Invalid time format: " + format);
        }
        
        return formattedResult;
    }

    /**
     * Formats a ZonedDateTime to the specified format string (for UTC handling)
     */
    public static String formatTime(ZonedDateTime zonedTime, String format) throws IllegalArgumentException {
        if (zonedTime == null) {
            throw new IllegalArgumentException("ZonedDateTime cannot be null");
        }
        
        String formattedResult;
        
        switch(format) {
            case "UTC":
                // Return full UTC ZonedDateTime string
                formattedResult = zonedTime.toString();
                break;
            case "ISO_8601":
                formattedResult = zonedTime.format(DateTimeFormatter.ISO_DATE_TIME);
                break;
            default:
                // For other formats, use LocalDateTime formatting
                LocalDateTime localTime = zonedTime.toLocalDateTime();
                formattedResult = formatTime(localTime, format);
                break;
        }
        
        return formattedResult;
    }

    /**
     * Legacy method: Formats a time string (assumes inputTime is already a LocalDateTime string)
     * This method is kept for backward compatibility but may have issues.
     * Prefer using formatTime(LocalDateTime, String) or formatTime(ZonedDateTime, String) instead.
     */
    public static String formatTime(String inputTime, String format) throws IllegalArgumentException {
        LocalDateTime localTime = LocalDateTime.parse(inputTime);
        return formatTime(localTime, format);
    }
}
