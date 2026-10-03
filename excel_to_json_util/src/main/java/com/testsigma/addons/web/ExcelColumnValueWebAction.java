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
@Action(actionText = "store the value of column column-identifier from row row-number in sheet sheet-name-or-index from excel/csvfile file-path into the runtime variable variable-name",
        description = "Gets a single column value from a specific row. Column can be identified by index (0-based), column name (A, B, C), or header value",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class ExcelColumnValueWebAction extends WebAction {

    @TestData(reference = "row-number")
    private com.testsigma.sdk.TestData rowNumber;

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    
    @TestData(reference = "column-identifier")
    private com.testsigma.sdk.TestData columnIdentifier;
    
    @TestData(reference = "sheet-name-or-index")
    private com.testsigma.sdk.TestData sheetNameOrIndex;
    
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("Row number: " + rowNumber.getValue() + ", Column: " + columnIdentifier.getValue() + ", File path: " + filePath.getValue());

        try {
            String filePathStr = filePath.getValue().toString();
            int rowNum = Integer.parseInt(rowNumber.getValue().toString());
            String colId = columnIdentifier.getValue().toString().trim();
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

            // Use utility class to get column value
            ExcelFileUtils excelFileUtils = new ExcelFileUtils(logger);
            String columnValue = excelFileUtils.getColumnValue(filePathStr, rowNum, colId, sheetName, sheetIndex);

            // Store result in runtime data
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(varName);
            runTimeData.setValue(columnValue);
            setSuccessMessage("Successfully retrieved column '" + colId + "' value from row " + rowNum + " and stored in variable: " + varName);

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

