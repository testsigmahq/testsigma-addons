package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.utils.TextExtractionHelper;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

@Data
@Action(actionText = "Store the word after position number position-number occurrences of delimiter-type delimiter" +
        " from file file-path into a runtime variable variable variable-name",
        description = "Extracts the word found after a specified number of delimiter occurrences in the given file." +
                " Supported delimiters: comma (,), period (.), tab (\\t or tab), space. " +
                "position-number=0 returns the token before any delimiter; position-number=N returns the token after the Nth delimiter.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = false,
        displayName = "Extract Word By Delimiter Position From File")
public class ExtractWordByDelimiterPositionFromFile extends WindowsAdvancedAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "delimiter-type", allowedValues = {"comma", ".", "tab", "space", "multi-space"},
            description = "Accepted values: comma or ,, period or ., tab or \\t, space, multi-space (2+ consecutive spaces — use for fixed-width files)")
    private com.testsigma.sdk.TestData delimiterType;

    @TestData(reference = "position-number")
    private com.testsigma.sdk.TestData positionNumber;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;

        try {
            logger.info("Initiating word extraction by delimiter position from file");

            String filePathStr = filePath.getValue().toString().trim();
            String delimiterTypeStr = delimiterType.getValue().toString().trim();
            String positionStr = positionNumber.getValue().toString().trim();
            String variableNameStr = variableName.getValue().toString().trim();

            if (filePathStr.isEmpty() || delimiterTypeStr.isEmpty() || positionStr.isEmpty() || variableNameStr.isEmpty()) {
                setErrorMessage("All input fields must be provided and cannot be empty");
                return Result.FAILED;
            }

            int position;
            try {
                position = Integer.parseInt(positionStr);
            } catch (NumberFormatException e) {
                setErrorMessage("position-number must be a valid integer, got: " + positionStr);
                return Result.FAILED;
            }

            if (position < 0) {
                setErrorMessage("position-number must be 0 or greater, got: " + position);
                return Result.FAILED;
            }

            if (TextExtractionHelper.resolveDelimiter(delimiterTypeStr) == null) {
                setErrorMessage("Unsupported delimiter-type: '" + delimiterTypeStr +
                        "'. Accepted values: comma (,)  period (.)  tab (\\t)  space  multi-space");
                return Result.FAILED;
            }

            logger.debug("File: " + filePathStr + ", delimiter: '" + delimiterTypeStr +
                    "', position: " + position + ", variable: " + variableNameStr);

            String extractedWord = TextExtractionHelper.extractWordAtDelimiterPosition(
                    logger, filePathStr, delimiterTypeStr, position);

            if (extractedWord == null) {
                setErrorMessage("Could not extract word at position " + position +
                        " using delimiter '" + delimiterTypeStr + "'. " +
                        "Check that the file exists and the position is within range.");
                return Result.FAILED;
            }

            runTimeData.setKey(variableNameStr);
            runTimeData.setValue(extractedWord);
            logger.info("Stored extracted word '" + extractedWord + "' in variable '" + variableNameStr + "'");
            setSuccessMessage("Successfully extracted word at position " + position + " using delimiter '" + delimiterTypeStr +
                    "' and stored in variable '" + variableNameStr + "': " + extractedWord);
        } catch (Exception e) {
            logger.warn("An error occurred while extracting word by delimiter position from file" + e);
            result = Result.FAILED;
            setErrorMessage("An error occurred: " + e.getMessage());
        }

        return result;
    }
}
