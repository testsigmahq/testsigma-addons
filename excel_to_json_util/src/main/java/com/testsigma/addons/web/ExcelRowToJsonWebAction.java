package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.addons.util.ExcelFileUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openqa.selenium.NoSuchElementException;

import java.io.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "store the entire row row-number from excel/csvfile file-path in format into the runtime variable variable-name",
        description = "Converts a specified row from Excel (XLS/XLSX) or CSV file to JSON format using headers as keys",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ExcelRowToJsonWebAction extends WebAction {

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    
    @TestData(reference = "format", allowedValues = {"JSON string", "JSON file"})
    private com.testsigma.sdk.TestData format;
    
    @TestData(reference = "variable-name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Row number: " + rowNumber.getValue() + ", File path: " + filePath.getValue());

        try {
            String filePathStr = filePath.getValue().toString();
            int rowNum = Integer.parseInt(rowNumber.getValue().toString());
            String varName = variableName.getValue().toString();

            // Validate row number (should be >= 1, where 1 is the first data row after header)
            if (rowNum < 1) {
                setErrorMessage("Row number must be greater than 0. Row 1 is the first data row (header is row 0).");
                return Result.FAILED;
            }

            // Use utility class directly to convert row to JSON (handles URL/local path and file format detection)
            ExcelFileUtils excelFileUtils = new ExcelFileUtils(logger);
            String jsonResult = excelFileUtils.convertRowToJson(filePathStr, rowNum);

            // Store result in runtime data
            if (format.getValue().toString().equals("JSON string")) {
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(varName);
                runTimeData.setValue(jsonResult);
                setSuccessMessage("Successfully converted row " + rowNum + " from file to JSON string and stored in variable: " + varName);
            } else if (format.getValue().toString().equals("JSON file")) {
                // create a temp json file with the json result
                File tempFile = File.createTempFile("temp", ".json");
                FileWriter fileWriter = new FileWriter(tempFile);
                fileWriter.write(jsonResult);
                fileWriter.close();
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(varName);
                runTimeData.setValue(tempFile.getAbsolutePath());
                setSuccessMessage("Successfully converted row " + rowNum + " from file to JSON file and stored in variable: " + varName);
            }

        } catch (NumberFormatException e) {
            result = Result.FAILED;
            setErrorMessage("Invalid row number format: " + rowNumber.getValue());
            logger.warn("Error parsing row number: " + e.getMessage());
        } catch (IOException e) {
            result = Result.FAILED;
            setErrorMessage("Error reading file: " + e.getMessage());
            logger.warn("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            result = Result.FAILED;
            setErrorMessage("Unexpected error: " + e.getMessage());
            logger.warn("Unexpected error: " + e.getMessage());
        }

        return result;
    }
}
