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
@Action(actionText = "Verify if the pdf contains text test_data",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class ContainsTextpdf extends WebAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    com.testsigma.sdk.Result result;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public com.testsigma.sdk.Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        logger.debug("test-data: " + this.testData.getValue());
        StringBuffer sb = new StringBuffer();
        URL url;
        BufferedInputStream fileParse;
        PDDocument doc = null;
        try {
            url = new URL(driver.getCurrentUrl());
            InputStream is = url.openStream();   //Converting url as input
            fileParse = new BufferedInputStream(is);   //reads data from file
            doc = PDDocument.load(fileParse); //loads the file as pdf
            sb.append(new PDFTextStripper().getText(doc));
            Assert.assertTrue(sb.toString().contains((CharSequence) testData.getValue()));
            setSuccessMessage(String.format(SUCCESS_MESSAGE + " " + testData.getValue()));
           return Result.SUCCESS;
        } catch (IOException e) {
        	
            e.printStackTrace();
            setErrorMessage(String.format(ERROR_MESSAGE + "" + testData.getValue() + "   " + "Cause of Exception:" + e.getCause().toString()));
            return Result.FAILED;
        }
        

       
    }
}
  
