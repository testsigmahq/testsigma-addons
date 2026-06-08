package com.testsigma.addons.web;

import com.testsigma.addons.utils.DocxUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify if the text test-data from docx docx-file-path is format-type",
        description = "Validates if the given text in a DOCX file has the specified format (bold, italic, underline, or any combination)",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyTextFormatInDocx extends WebAction {

    @TestData(reference = "docx-file-path")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData2;

    @TestData(
        reference = "format-type",
        description = "One or more formats to verify, comma-separated",
        allowedValues = {
            "bold",
            "italic",
            "underline",
            "bold and underline",
            "bold and italic",
            "italic and underline",
            "bold and italic and underline"
        }
    )
    private com.testsigma.sdk.TestData testData3;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        String docxFilePath = testData.getValue().toString();
        String searchText   = testData2.getValue().toString();
        String formatInput  = testData3.getValue().toString();
        logger.info("DOCX: " + docxFilePath + " | Text: " + searchText + " | Format: " + formatInput);

        String[] formats = parseFormats(formatInput);
        DocxUtils docxUtils = new DocxUtils(driver, logger);
        try {
            if (docxUtils.validateContentTypeForSearchText(searchText, docxFilePath, formats)) {
                setSuccessMessage("The text <b>" + searchText + "</b> is verified as <b>" + formatInput + "</b>");
                logger.info("Format verified: " + formatInput);
                return Result.SUCCESS;
            } else {
                setErrorMessage("The text <b>" + searchText + "</b> does not have format: <b>" + formatInput + "</b>");
                logger.info("Format not found: " + formatInput);
                return Result.FAILED;
            }
        } catch (Exception e) {
            setErrorMessage("Exception while validating \"" + searchText + "\": " + ExceptionUtils.getMessage(e));
            logger.info("Exception: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    private String[] parseFormats(String input) {
        String[] parts = input.toLowerCase().split("\\band\\b");
        String[] result = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parts[i].trim();
        }
        return result;
    }
}
