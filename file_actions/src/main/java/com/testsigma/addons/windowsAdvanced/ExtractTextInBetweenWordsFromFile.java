package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.utils.TextExtractionHelper;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;


@Data
@Action(actionText = "Store text in between word start word start-word and end word end-word from file file-path" +
        " into a runtime variable variable variable-name",
        description = "Extracts text that is located between the specified start and end words in the given file." +
                " The extracted text is stored in a variable for later use. Supports various file types including HTML files.",
        applicationType = com.testsigma.sdk.ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = false,
        displayName = "Extract Text In Between Words From File")
public class ExtractTextInBetweenWordsFromFile extends WindowsAdvancedAction {

    @TestData(reference = "start-word")
    private com.testsigma.sdk.TestData startWord;
    @TestData(reference = "end-word")
    private com.testsigma.sdk.TestData endWord;
    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;


    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;

        try {
            logger.info("Initiating text extraction between words from file");
            logger.debug("Start word: " + startWord.getValue() +
                    ", End word: " + endWord.getValue() +
                    ", File path: " + filePath.getValue() +
                    ", Variable name: " + variableName.getValue());

            String startWordStr = startWord.getValue().toString();
            String endWordStr = endWord.getValue().toString();
            String filePathStr = filePath.getValue().toString();
            String variableNameStr = variableName.getValue().toString();

            // Validate inputs
            if (startWordStr.isEmpty() || endWordStr.isEmpty() || filePathStr.isEmpty() || variableNameStr.isEmpty()) {
                result = Result.FAILED;
                setErrorMessage("All input fields must be provided and cannot be empty");
                return result;
            }

            // Extract text between start and end words from the file
            String extractedText = TextExtractionHelper.extractTextBetweenWords(logger, filePathStr, startWordStr, endWordStr);

            if (extractedText == null) {
                result = Result.FAILED;
                setErrorMessage("Failed to extract text between the specified words." +
                        " Please check the file and the provided words.");
                return result;
            }

            // Store the extracted text in a runtime variable
            runTimeData.setKey(variableNameStr);
            runTimeData.setValue(extractedText);
            logger.info("Extracted text stored in variable '" + variableNameStr + "': " + extractedText);

        } catch (Exception e) {
            logger.warn("An error occurred while extracting text between words from file" + e);
            result = Result.FAILED;
            setErrorMessage("An error occurred: " + e.getMessage());
        }

        return result;
    }


}
