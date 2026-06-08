package com.testsigma.addons.DataGenerators;

import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = false)
@com.testsigma.sdk.annotation.TestDataFunction(
        displayName = "Convert unix-timestamp to output-format",
        description = "Converts a UNIX timestamp (seconds or milliseconds) to the specified date format. " +
                "Supported output-format values: DD/MM/YYYY, DD/MON/YYYY, DD-MM-YYYY, DD-MON-YYYY, " +
                "YYYY-MM-DD, MM/DD/YYYY, DD/MM/YYYY HH:MM:SS, DD-MM-YYYY HH:MM:SS, YYYY-MM-DD HH:MM:SS")
public class ConvertUnixTimestamp extends TestDataFunction {

    @TestDataFunctionParameter(reference = "unix-timestamp")
    private com.testsigma.sdk.TestDataParameter unixTimestamp;

    @TestDataFunctionParameter(reference = "output-format")
    private com.testsigma.sdk.TestDataParameter outputFormat;

    @Override
    public TestData generate() throws Exception {
        try {
            String unixTimestampStr = unixTimestamp.getValue().toString().trim();
            String outputFormatStr = outputFormat.getValue().toString();

            logger.info("Converting UNIX timestamp: " + unixTimestampStr + " to format: " + outputFormatStr);

            long timestamp = Long.parseLong(unixTimestampStr);
            // Auto-detect milliseconds vs seconds: values > 9999999999 are milliseconds
            if (timestamp > 9_999_999_999L) {
                timestamp = timestamp / 1000;
            }

            Instant instant = Instant.ofEpochSecond(timestamp);
            ZonedDateTime utcDateTime = instant.atZone(ZoneOffset.UTC);

            String javaPattern = toJavaPattern(outputFormatStr);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaPattern, Locale.ENGLISH);
            String result = utcDateTime.format(formatter);

            logger.info("Converted UNIX timestamp <b>" + unixTimestampStr + "</b> to <b>" + result + "</b> (format: " + outputFormatStr + ")");

            return new TestData(result);

        } catch (NumberFormatException e) {
            throw new Exception("Invalid UNIX timestamp value: " + unixTimestamp.getValue());
        } catch (Exception e) {
            logger.warn("Failed to convert UNIX timestamp: " + ExceptionUtils.getStackTrace(e));
            throw new Exception("Failed to convert UNIX timestamp: " + e.getMessage());
        }
    }

    private String toJavaPattern(String format) {
        switch (format) {
            case "DD/MM/YYYY":          return "dd/MM/yyyy";
            case "DD/MON/YYYY":         return "dd/MMM/yyyy";
            case "DD-MM-YYYY":          return "dd-MM-yyyy";
            case "DD-MON-YYYY":         return "dd-MMM-yyyy";
            case "YYYY-MM-DD":          return "yyyy-MM-dd";
            case "MM/DD/YYYY":          return "MM/dd/yyyy";
            case "DD/MM/YYYY HH:MM:SS": return "dd/MM/yyyy HH:mm:ss";
            case "DD-MM-YYYY HH:MM:SS": return "dd-MM-yyyy HH:mm:ss";
            case "YYYY-MM-DD HH:MM:SS": return "yyyy-MM-dd HH:mm:ss";
            default: throw new IllegalArgumentException("Invalid output format: " + format);
        }
    }
}
