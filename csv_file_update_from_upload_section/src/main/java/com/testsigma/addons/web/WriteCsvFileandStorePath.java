package com.testsigma.addons.web;

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
@Action(actionText = "Write row number and column number into CSV file with testdata where absolutepath is test_data and store filepath in runtime variable variable-name (It supports file from upload section)", description = "Write a particular row and column into CSV file where absolutepath. Can accept local file paths or URLs for the CSV file. Stores the file path in a runtime variable.It supports file from upload section.", applicationType = ApplicationType.WEB)
public class WriteCsvFileandStorePath extends WebAction {

    @TestData(reference = "row")
    private com.testsigma.sdk.TestData row;

    @TestData(reference = "column")
    private com.testsigma.sdk.TestData column;

    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String filePathString = filePath.getValue().toString();
        String replace = testData.getValue().toString();

        int targetRow;
        int targetColumn;
        try {
            targetRow = Integer.parseInt(row.getValue().toString());
            targetColumn = Integer.parseInt(column.getValue().toString());
        } catch (NumberFormatException e) {
            setErrorMessage("Row number and Column number must be valid integers.");
            return com.testsigma.sdk.Result.FAILED;
        }

        if (targetRow < 1 || targetColumn < 1) {
            setErrorMessage("Row and Column numbers must be at least 1. Given: row=" + targetRow + ", column=" + targetColumn);
            return com.testsigma.sdk.Result.FAILED;
        }

        File csvFile = null;
        File tempCsvFile = null;

        try {
            csvFile = convertToFile(filePathString);
            logger.info("CSV file path: " + csvFile.getAbsolutePath());

            if (csvFile == null || !csvFile.exists()) {
                setErrorMessage("CSV File not found or could not be downloaded: " + filePathString);
                return com.testsigma.sdk.Result.FAILED;
            }

            String uniqueFileName = "updated_" + System.currentTimeMillis() + ".csv";
            tempCsvFile = new File(csvFile.getParentFile(), uniqueFileName);
            logger.info("Temp file path: " + tempCsvFile.getAbsolutePath());
            Files.copy(csvFile.toPath(), tempCsvFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            CSVReader csvReader = null;
            CSVWriter writer = null;
            try {
                csvReader = new CSVReaderBuilder(new FileReader(tempCsvFile))
                        .build();
                List<String[]> data = csvReader.readAll();

                int rowIndex = targetRow - 1;
                int columnIndex = targetColumn - 1;

                while (data.size() <= rowIndex) {
                    data.add(new String[Math.max(columnIndex + 1, 0)]);
                }
                String[] targetRowData = data.get(rowIndex);
                if (targetRowData.length <= columnIndex) {
                    String[] newRow = new String[columnIndex + 1];
                    System.arraycopy(targetRowData, 0, newRow, 0, targetRowData.length);
                    data.set(rowIndex, newRow);
                    targetRowData = newRow;
                }
                targetRowData[columnIndex] = replace;

                writer = new CSVWriter(new FileWriter(tempCsvFile), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
                writer.writeAll(data);
                writer.flush();
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

            try {
                Files.copy(tempCsvFile.toPath(), csvFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully copied content to original file" + csvFile.getAbsolutePath());
            } catch (IOException ex) {
                logger.warn("Error copying data from temp file to original file" + ex);
                setErrorMessage("Failed to copy data from temp file to original file: " + ex.getMessage());
                return com.testsigma.sdk.Result.FAILED;
            }

            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(csvFile.getAbsolutePath());

            setSuccessMessage("Data updated successfully at row " + targetRow + ", column " + targetColumn
                    + " with value: " + replace + ". File path stored in runtime variable: "
                    + variableName.getValue().toString() + " = " + csvFile.getAbsolutePath());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Operation Failed: " + e.getMessage());
            logger.warn("Error during CSV processing: " + e.getMessage() + e);
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            String originalFileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
            String uniqueFileName = "temp_" + System.currentTimeMillis() + ".csv";
            logger.info("Given is a URL... Original file name: " + originalFileName + ", Unique file name: "
                    + uniqueFileName);

            String filePath = String.format("%s%s%s", FileUtils.getTempDirectoryPath(), File.separator, uniqueFileName);
            File tempFile = new File(filePath);

            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile, 10000, 10000);
            logger.info("Temp file created for URL file: " + uniqueFileName + " at path " + filePath);

            return tempFile;
        } else {
            logger.info("Given is a local file path...");
            return new File(pathOrUrl);
        }
    }
}
