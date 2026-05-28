package com.testsigma.addons.web;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

@Data
@Action(actionText = "Write value test-data into a cell in CSV file file-path at row row-number and column column-number with delimiter delimiter-value",
        description = "Writes a value to a particular cell in a delimiter-separated file (e.g. pipe | for pipe-separated files, comma , for standard CSV) using 1-based indexing for row and column numbers.",
        applicationType = ApplicationType.WEB)
public class WriteCellValueInCsvWithCustomDelimiter extends WebAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePathOrUpload;

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "column-number")
    private com.testsigma.sdk.TestData columnNumber;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData cellValue;

    @TestData(reference = "delimiter-value")
    private com.testsigma.sdk.TestData delimiter;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String filePathString = filePathOrUpload.getValue().toString();
        String valueToWrite = cellValue.getValue().toString();
        String delimiterStr = delimiter.getValue().toString().trim();
        if (delimiterStr.isEmpty()) delimiterStr = ",";
        char sep = delimiterStr.charAt(0);

        int targetRow;
        int targetColumn;
        try {
            targetRow = Integer.parseInt(rowNumber.getValue().toString());
            targetColumn = Integer.parseInt(columnNumber.getValue().toString());
        } catch (NumberFormatException e) {
            setErrorMessage("Row number and Column number must be valid integers.");
            return com.testsigma.sdk.Result.FAILED;
        }

        if (targetRow < 1 || targetColumn < 1) {
            setErrorMessage("Row and Column numbers must be at least 1. Given: row=" + targetRow + ", column=" + targetColumn);
            return com.testsigma.sdk.Result.FAILED;
        }

        int rowIndex = targetRow - 1;
        int columnIndex = targetColumn - 1;

        String csvFilePath;
        if (filePathString.startsWith("https://") || filePathString.startsWith("http://")) {
            File tempFile = urlToCSVFileConverter("csvFileName", filePathString);
            csvFilePath = tempFile.getAbsolutePath();
        } else {
            logger.info("Given is local file path...");
            csvFilePath = filePathString;
        }
        logger.info("CSV File path: " + csvFilePath);

        try (Reader reader = new FileReader(csvFilePath);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(sep).build())
                     .build()) {
            List<String[]> rows = csvReader.readAll();

            if (rowIndex >= 0 && rowIndex < rows.size()) {
                logger.info("Accessing row " + targetRow);
                String[] row = rows.get(rowIndex);

                if (columnIndex >= 0 && columnIndex < row.length) {
                    row[columnIndex] = valueToWrite;

                    try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath), sep,
                            CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                            CSVWriter.DEFAULT_LINE_END)) {
                        writer.writeAll(rows);
                    } catch (IOException e) {
                        logger.warn("Error writing to CSV file: " + ExceptionUtils.getStackTrace(e));
                        setErrorMessage("Error writing to CSV file: " + e.getMessage());
                        return com.testsigma.sdk.Result.FAILED;
                    }

                    logger.info("Value '" + valueToWrite + "' written to row " + targetRow + " and column " + targetColumn);
                    setSuccessMessage("Value '" + valueToWrite + "' written to row " + targetRow + " and column " + targetColumn);
                } else {
                    logger.warn("Column number " + targetColumn + " is out of bounds.");
                    setErrorMessage("Column number " + targetColumn + " is out of bounds.");
                    result = com.testsigma.sdk.Result.FAILED;
                }
            } else {
                logger.warn("Row number " + targetRow + " is out of bounds.");
                setErrorMessage("Row number " + targetRow + " is out of bounds.");
                result = com.testsigma.sdk.Result.FAILED;
            }
        } catch (IOException | CsvException e) {
            logger.warn("Error processing CSV file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error processing CSV file: " + e.getMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    public File urlToCSVFileConverter(String fileName, String url) {
        try {
            logger.info("Given is URL... File name: " + fileName);
            URL urlObject = new URL(url);

            String baseName = fileName;
            String extension = ".csv";
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                baseName = fileName.substring(0, lastDotIndex);
                extension = fileName.substring(lastDotIndex);
            }

            File tempFile = File.createTempFile(baseName, extension);
            FileUtils.copyURLToFile(urlObject, tempFile);
            logger.info("CSV file created: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            throw new RuntimeException("Unable to access or validate the CSV file. Please check the inputs.", e);
        }
    }
}
