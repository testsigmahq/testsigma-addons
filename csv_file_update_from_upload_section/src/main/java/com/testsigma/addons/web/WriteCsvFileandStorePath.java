package com.testsigma.addons.web;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
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
@Action(actionText = "Write row number and column number into CSV file with testdata where absolutepath is test_data and store filepath in runtime variable variable-name (It supports file from upload section)",
        description = "Write a particular row and column into CSV file where absolutepath. Can accept local file paths or URLs for the CSV file. Stores the file path in a runtime variable.It supports file from upload section.",
        applicationType = ApplicationType.WEB)
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
        int targetRow = Integer.parseInt(row.getValue().toString());
        int targetColumn = Integer.parseInt(column.getValue().toString());

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
                csvReader = new CSVReader(new FileReader(tempCsvFile));
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

                writer = new CSVWriter(new FileWriter(tempCsvFile));
                writer.writeAll(data);
                writer.flush();
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

            // Store the path of the new updated file
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(tempCsvFile.getAbsolutePath()); // Store absolute path of the new file

            setSuccessMessage("Data is updated successfully in the CSV file. Updated data is " + replace + ". File path stored in runtime variable: " + variableName.getValue().toString() + " = " + tempCsvFile.getAbsolutePath());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Operation Failed: " + e.getMessage());
            logger.warn("Error during CSV processing: " + e.getMessage() + e);
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("http://")) {
            // Extract the original file name from the URL
            String originalFileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
            // Generate a unique file name by appending a timestamp
            String uniqueFileName = "temp_" + System.currentTimeMillis() + "_" + originalFileName;
            logger.info("Given is a URL... Original file name: " + originalFileName + ", Unique file name: " + uniqueFileName);

            // Create the full path for the temporary file
            String filePath = String.format("%s%s%s", FileUtils.getTempDirectoryPath(), File.separator, uniqueFileName);
            File tempFile = new File(filePath);

            // Download the file from the URL to the temporary location
            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile, 10000, 10000); // Adding timeouts
            logger.info("Temp file created for URL file: " + uniqueFileName + " at path " + filePath);

            return tempFile;
        } else {
            logger.info("Given is a local file path...");
            return new File(pathOrUrl);
        }
    }
}