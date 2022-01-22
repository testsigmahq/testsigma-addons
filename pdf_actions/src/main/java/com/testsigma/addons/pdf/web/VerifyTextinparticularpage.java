package com.testsigma.addons.pdf.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.testng.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Data
@Action(actionText = "Verify if page number pages_from  to page number pages_to contains text test_data in the PDF",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class VerifyTextinparticularpage extends WebAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    com.testsigma.sdk.Result result;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "pages_from")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "pages_to")
    private com.testsigma.sdk.TestData testData3;

    @Override
    public com.testsigma.sdk.Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        //logger.debug("test-data: "+ this.testData.getValue());
        URL url;
        try {
            url = new URL(driver.getCurrentUrl());
            InputStream is = url.openStream();
            BufferedInputStream fileParse = new BufferedInputStream(is);
            PDDocument doc = null;
            doc = PDDocument.load(fileParse);
            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(Integer.parseInt(testData2.getValue().toString()));
            reader.setEndPage(Integer.parseInt(testData3.getValue().toString()));
            String content = reader.getText(doc);
            Assert.assertTrue(content.contains(testData.getValue().toString()));
            setSuccessMessage(String.format(SUCCESS_MESSAGE +testData.getValue().toString()+" in given range of pages "));
            logger.info(content);
            return Result.SUCCESS;

        } catch (IOException e) {
            e.printStackTrace();
            setErrorMessage(String.format(ERROR_MESSAGE + " " + "Cause of Exception:" + e.getCause().toString()));
            return Result.FAILED;
        }

    }
}
  
