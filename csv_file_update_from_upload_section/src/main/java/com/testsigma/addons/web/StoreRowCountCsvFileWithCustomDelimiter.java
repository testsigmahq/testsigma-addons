package com.testsigma.addons.web;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

@Data
@Action(
        actionText = "Read row count from CSV file where filepath is file-path-or-upload with delimiter delimiter-value and store in into a runtime variable variable-name",
        description = "Reading row count from a delimiter-separated file (e.g. pipe | for pipe-separated files, comma , for standard CSV).",
        applicationType = ApplicationType.WEB
)
public class StoreRowCountCsvFileWithCustomDelimiter extends WebAction {

    @TestData(reference = "file-path-or-upload")
    private com.testsigma.sdk.TestData filePathOrUpload;

    @TestData(reference = "delimiter-value")
    private com.testsigma.sdk.TestData delimiter;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating CSV row count execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        String inputPath = filePathOrUpload.getValue().toString();
        String delimiterStr = delimiter.getValue().toString().trim();
        if (delimiterStr.isEmpty()) delimiterStr = ",";
        char sep = delimiterStr.charAt(0);

        File csvFile;

        try {
            csvFile = convertToFile(inputPath);
            logger.info("Resolved file path: " + csvFile.getAbsolutePath());

            if (!csvFile.exists()) {
                logger.warn("CSV file does not exist at path: " + csvFile.getAbsolutePath());
                setErrorMessage("CSV file not found: " + inputPath);
                return com.testsigma.sdk.Result.FAILED;
            }

            int rowCount;
            try (Reader reader = new FileReader(csvFile);
                 CSVReader csvReader = new CSVReaderBuilder(reader)
                         .withCSVParser(new CSVParserBuilder().withSeparator(sep).build())
                         .build()) {

                List<String[]> rows = csvReader.readAll();
                rowCount = rows.size();
            }
            logger.info("Row count from CSV file is: " + rowCount);

            String runtimeKey = variableName.getValue().toString();
            runTimeData.setKey(runtimeKey);
            runTimeData.setValue(String.valueOf(rowCount));

            logger.info("CSV row count '" + rowCount + "' stored in runtime variable '" + runtimeKey + "'");
            setSuccessMessage("Successfully read CSV file. Total rows: " + rowCount
                    + " and stored in runtime variable '" + runtimeKey + "'");

        } catch (Exception e) {
            logger.warn("Exception occurred while reading CSV file: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception occurred: " + ExceptionUtils.getMessage(e));
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private File convertToFile(String pathOrUrl) throws IOException {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            String originalFileName = FilenameUtils.getName(new URL(pathOrUrl).getPath());
            String uniqueFileName = "temp_" + System.currentTimeMillis() + ".csv";

            String tempPath = FileUtils.getTempDirectoryPath() + File.separator + uniqueFileName;
            File tempFile = new File(tempPath);
            FileUtils.copyURLToFile(new URL(pathOrUrl), tempFile, 10000, 10000);

            logger.info("Downloaded CSV from URL to temp location: " + tempPath);
            return tempFile;
        }

        logger.info("Input provided as local file path");
        return new File(pathOrUrl);
    }
}
