package com.testsigma.addons.web;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Data
@Action(actionText = "clear cell value from CSV file test_data where row is row-number and column column-number" +
        " and store filepath in runtime variable variable-name (It supports file from upload section)",
        description = "Deletes content from a particular cell in CSV file using 1-based indexing for row " +
                "and column numbers. Can accept local file paths or URLs for the CSV file." +
                " Stores the file path in a runtime variable. It supports file from upload section.",
        applicationType = ApplicationType.WEB)
public class DeleteRowContentFromCsvAndStorePath extends WebAction {

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "column-number")
    private com.testsigma.sdk.TestData columnNumber;

    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String filePathString = filePath.getValue().toString();

        int targetRow;
        int targetColumn;
        try {
            targetRow = Integer.parseInt(rowNumber.getValue().toString());
            targetColumn = Integer.parseInt(columnNumber.getValue().toString());
        } catch (NumberFormatException e) {
            setErrorMessage("Row number and Column number must be valid integers.");
            return com.testsigma.sdk.Result.FAILED;
        }

        // Validate 1-based input (must be at least 1)
        if (targetRow < 1 || targetColumn < 1) {
            setErrorMessage("Row and Column numbers must be at least 1. Given: row=" + targetRow + ", column=" + targetColumn);
            return com.testsigma.sdk.Result.FAILED;
        }

        // Store original 1-based values for user-friendly messages
        int originalRow = targetRow;
        int originalColumn = targetColumn;

        File csvFile = null;
        File tempCsvFile = null;

        try {
            csvFile = convertToFile(filePathString);
            logger.info("CSV file path: " + csvFile.getAbsolutePath());

            if (csvFile == null || !csvFile.exists()) {
                setErrorMessage("CSV File not found or could not be downloaded: " + filePathString);
                return com.testsigma.sdk.Result.FAILED;
            }

            // Create a unique temp file each time
            String uniqueFileName = "updated_" + System.currentTimeMillis() + ".csv";
            tempCsvFile = new File(csvFile.getParentFile(), uniqueFileName);
            logger.info("Temp file path: " + tempCsvFile.getAbsolutePath());
            Files.copy(csvFile.toPath(), tempCsvFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            CSVReader csvReader = null;
            CSVWriter writer = null;
            try {
                csvReader = new CSVReaderBuilder(new FileReader(tempCsvFile))
                        .withCSVParser(new CSVParserBuilder().withIgnoreQuotations(true).build())
                        .build();
                List<String[]> data = csvReader.readAll();

                // Convert from 1-based (user input) to 0-based index (user gives 1,1 → maps to 0,0)
                int rowIndex = targetRow - 1;
                int columnIndex = targetColumn - 1;

                // Check row bounds using 0-based index
                if (rowIndex < data.size()) {
                    logger.info("Accessing row " + originalRow);
                    String[] row = data.get(rowIndex);

                    // Check column bounds using 0-based index
                    if (columnIndex < row.length) {
                        row[columnIndex] = "";

                        writer = new CSVWriter(new FileWriter(tempCsvFile), ',', CSVWriter.NO_QUOTE_CHARACTER,
                                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
                        writer.writeAll(data);
                        writer.flush();

                        logger.info("Content deleted from row " + originalRow + " and column " + originalColumn);
                    } else {
                        setErrorMessage("Column number " + originalColumn + " is out of bounds. Max columns: " + row.length);
                        return com.testsigma.sdk.Result.FAILED;
                    }
                } else {
                    setErrorMessage("Row number " + originalRow + " is out of bounds. Max rows: " + data.size());
                    return com.testsigma.sdk.Result.FAILED;
                }
            } catch (IOException | CsvException e) {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Error processing CSV file: " + e.getMessage());
                logger.warn("Error processing CSV file: " + e);
                return result;
            } finally {
                if (csvReader != null) {
                    try {
                        csvReader.close();
                    } catch (IOException e) {
                        logger.warn("Error closing CSVReader: " + e.getMessage() + e);
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        logger.warn("Error closing CSVWriter: " + e.getMessage() + e);
                    }
                }
            }

            // Copy the temp file back to the original file
            try {
                Files.copy(tempCsvFile.toPath(), csvFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully copied content to original file: " + csvFile.getAbsolutePath());
            } catch (IOException ex) {
                logger.warn("Error copying data from temp file to original file: " + ex);
                setErrorMessage("Failed to copy data from temp file to original file: " + ex.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            }

            // Store the path of the updated file
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(csvFile.getAbsolutePath());

            setSuccessMessage("Content deleted from row " + originalRow + ", column " + originalColumn
                    + ". File path stored in runtime variable: " + variableName.getValue().toString()
                    + " = " + csvFile.getAbsolutePath());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Operation Failed: " + e.getMessage());
            logger.warn("Error during CSV processing: " + e.getMessage() + e);
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            // Extract the original file name from the URL for logging only
            String originalFileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
            // Use only the timestamp for uniqueness — the original name can be arbitrarily long
            // (e.g. an encoded path from a prior temp file), which would exceed OS filename limits
            String uniqueFileName = "temp_" + System.currentTimeMillis() + ".csv";
            logger.info("Given is a URL... Original file name: " + originalFileName + ", Unique file name: "
                    + uniqueFileName);

            // Create the full path for the temporary file
            String filePath = String.format("%s%s%s", FileUtils.getTempDirectoryPath(), File.separator, uniqueFileName);
            File tempFile = new File(filePath);

            // Download the file from the URL to the temporary location
            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile, 10000, 10000);
            logger.info("Temp file created for URL file: " + uniqueFileName + " at path " + filePath);

            return tempFile;
        } else {
            logger.info("Given is a local file path...");
            return new File(pathOrUrl);
        }
    }
}

