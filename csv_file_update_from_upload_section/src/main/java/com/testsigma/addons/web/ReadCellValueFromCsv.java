package com.testsigma.addons.web;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Read cell value from CSV file file-path at row row-number and column column-number and" +
        " store in variable-name",
        description = "Reads a value from a particular cell in CSV file using 1-based indexing for row and column" +
                " numbers and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB)
public class ReadCellValueFromCsv extends WebAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePathOrUpload;

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "column-number")
    private com.testsigma.sdk.TestData columnNumber;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = Result.SUCCESS;

        String filePathString = filePathOrUpload.getValue().toString();
        int targetRow;
        int targetColumn;
        try {
            targetRow = Integer.parseInt(rowNumber.getValue().toString());
            targetColumn = Integer.parseInt(columnNumber.getValue().toString());
        } catch (NumberFormatException e) {
            setErrorMessage("Row number and Column number must be valid integers.");
            return Result.FAILED;
        }

        // Validate 1-based input (must be at least 1)
        if (targetRow < 1 || targetColumn < 1) {
            setErrorMessage("Row and Column numbers must be at least 1. Given: row="
                    + targetRow + ", column=" + targetColumn);
            return Result.FAILED;
        }

        // Store original 1-based values for user-friendly messages
        int originalRow = targetRow;
        int originalColumn = targetColumn;

        // Convert to 0-based index (user gives 1,1 → maps to 0,0 in CSV)
        int rowIndex = targetRow - 1;
        int columnIndex = targetColumn - 1;

        String csvFilePath;
        if (filePathString.startsWith("https://") || filePathString.startsWith("http://")) {
            File tempFile = urlToCSVFileConverter("csvFileName", filePathString);
            csvFilePath = tempFile.getAbsolutePath();
        } else {
            // Use local file path directly
            logger.info("Given is local file path...");
            csvFilePath = filePathString;
        }
        logger.info("CSV File path: " + csvFilePath);

        try (Reader reader = new FileReader(csvFilePath);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withIgnoreQuotations(true).build())
                     .build()) {
            List<String[]> rows = csvReader.readAll();

            // Check row bounds using 0-based index
            if (rowIndex < rows.size()) {
                logger.info("Accessing row " + originalRow);
                String[] row = rows.get(rowIndex);

                // Check column bounds using 0-based index
                if (columnIndex < row.length) {
                    String cellValue = row[columnIndex];

                    // Store the cell value in the runtime variable
                    runTimeData.setKey(variableName.getValue().toString());
                    runTimeData.setValue(cellValue);

                    System.out.println("Reading cell value from CSV file: " + cellValue);

                    logger.info("Cell value '" + cellValue + "' read from row " + originalRow +
                            " and column " + originalColumn);
                    setSuccessMessage("Cell value '" + cellValue + "' read from row " + originalRow +
                            " and column " + originalColumn
                            + ". Stored in variable: " + variableName.getValue().toString());
                } else {
                    logger.warn("Column number " + originalColumn + " is out of bounds.");
                    setErrorMessage("Column number " + originalColumn + " is out of bounds.");
                    result = Result.FAILED;
                }
            } else {
                logger.warn("Row number " + originalRow + " is out of bounds.");
                setErrorMessage("Row number " + originalRow + " is out of bounds.");
                result = Result.FAILED;
            }
        } catch (IOException | CsvException e) {
            logger.warn("Error processing CSV file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error processing CSV file: " + e.getMessage());
            result = Result.FAILED;
            return result;
        }
        return result;
    }

    public File urlToCSVFileConverter(String fileName, String url) {
        try {
            logger.info("Given is URL... File name: " + fileName);
            URL urlObject = new URL(url);

            // Extract file extension if present
            String baseName = fileName;
            String extension = ".csv"; // default csv format
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

