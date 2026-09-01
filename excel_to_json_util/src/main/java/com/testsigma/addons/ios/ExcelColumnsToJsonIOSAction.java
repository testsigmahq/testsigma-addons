package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.IOSAction;
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
@Action(actionText = "store the values of columns column-identifiers from row row-number in sheet sheet-name-or-index from excel/csvfile file-path in format into the runtime variable variable-name",
        description = "Gets multiple column values from a specific row and converts to JSON. Columns can be identified by index (0-based), column name (A, B, C), or header value. Separate multiple columns with commas",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class ExcelColumnsToJsonIOSAction extends IOSAction {

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    
    @TestData(reference = "column-identifiers")
    private com.testsigma.sdk.TestData columnIdentifiers;
    
    @TestData(reference = "sheet-name-or-index")
    private com.testsigma.sdk.TestData sheetNameOrIndex;
    
    @TestData(reference = "format", allowedValues = {"JSON string", "JSON file"})
    private com.testsigma.sdk.TestData format;
    
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Row number: " + rowNumber.getValue() + ", Columns: " + columnIdentifiers.getValue() + ", File path: " + filePath.getValue());

        try {
            String filePathStr = filePath.getValue().toString();
            int rowNum = Integer.parseInt(rowNumber.getValue().toString());
            String colIds = columnIdentifiers.getValue().toString().trim();
            String varName = variableName.getValue().toString();
            String sheetInput = sheetNameOrIndex.getValue().toString().trim();

            // Validate row number (should be >= 1, where 1 is the first data row after header)
            if (rowNum < 1) {
                setErrorMessage("Row number must be greater than 0. Row 1 is the first data row (header is row 0).");
                return Result.FAILED;
            }

            // Parse sheet name or index
            String sheetName = null;
            int sheetIndex = -1;
            
            // Try to parse as integer (sheet index)
            try {
                sheetIndex = Integer.parseInt(sheetInput);
                if (sheetIndex < 0) {
                    setErrorMessage("Sheet index must be 0 or greater (0-based indexing)");
                    return Result.FAILED;
                }
            } catch (NumberFormatException e) {
                // Not a number, treat as sheet name
                sheetName = sheetInput;
            }

            // Use utility class to get columns as JSON
            ExcelFileUtils excelFileUtils = new ExcelFileUtils(logger);
            String jsonResult = excelFileUtils.getColumnsToJson(filePathStr, rowNum, colIds, sheetName, sheetIndex);

            // Store result in runtime data
            if (format.getValue().toString().equals("JSON string")) {
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(varName);
                runTimeData.setValue(jsonResult);
                setSuccessMessage("Successfully retrieved columns '" + colIds + "' from row " + rowNum + " as JSON string and stored in variable: " + varName);
            } else if (format.getValue().toString().equals("JSON file")) {
                // create a temp json file with the json result
                File tempFile = File.createTempFile("temp", ".json");
                FileWriter fileWriter = new FileWriter(tempFile);
                fileWriter.write(jsonResult);
                fileWriter.close();
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(varName);
                runTimeData.setValue(tempFile.getAbsolutePath());
                setSuccessMessage("Successfully retrieved columns '" + colIds + "' from row " + rowNum + " as JSON file and stored in variable: " + varName);
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

