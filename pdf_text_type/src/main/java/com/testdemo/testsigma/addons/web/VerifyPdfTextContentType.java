package com.testdemo.testsigma.addons.web;

import com.testdemo.testsigma.addons.utils.PdfUtils;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

@Data
@Action(actionText = "Verify if the text test-data from pdf pdf-file-path is in bold format",
        description = "validates if the pdf file is in the correct format",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class VerifyPdfTextContentType extends WebAction {

    @TestData(reference = "pdf-file-path")
    private com.testsigma.sdk.TestData testData;

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData2;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        String pdfFilePath = testData.getValue().toString();
        String searchText = testData2.getValue().toString();
        logger.info("PDF File Path: " + pdfFilePath + " Search Text: " + searchText);
        PdfUtils pdfUtils = new PdfUtils(driver, logger);
        if(pdfUtils.validateContentTypeForSearchText(searchText, pdfFilePath, "bold")) {
            logger.info("The text format is as expected");
            setSuccessMessage("Successfully verified the given text <b>" + searchText +
                    "</b> format is in <b>BOLD</b> format in the PDF file");
        } else {
            logger.info("The text format is not as expected");
            setErrorMessage(String.format("The given text <b>" + searchText +
                    "</b> format is not in <b>BOLD</b> format"));
            return com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }


}