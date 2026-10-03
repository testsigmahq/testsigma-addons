package com.testsigma.addons.pdf.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Data
@Action(actionText = "Verify the content from pdf is contains text testdata",
        description = "Extracts the text from the PDF and verify the text",
        applicationType = ApplicationType.WEB)
public class VerifypdfContent extends WebAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() {
        //Your Awesome code starts here
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("test-data: " + this.testData.getValue());
        //StringBuffer sb = new StringBuffer();
        URL url;
        BufferedInputStream fileParse;
        if (driver.getCurrentUrl().contains(".pdf")) {
            setSuccessMessage("pdf file detected");
            System.out.println("PDF detected");
        } else {
            setErrorMessage("Not a pdf file");
            System.out.println("PDF not detected");
            return Result.FAILED;
        }
        try {
            url = new URL(driver.getCurrentUrl());
            InputStream is = url.openStream();   //Converting url as input
            fileParse = new BufferedInputStream(is);   //reads data from file
            PDDocument doc = null;
            doc = PDDocument.load(fileParse); //loads the file as pdf
            String st=new PDFTextStripper().getText(doc);
            
            if(st.trim().contains(testData.getValue().toString())) {
            	setSuccessMessage("Assertion passed PDF content contains test data" + testData.getValue());
            	logger.info("Assertion passed PDF content contains test data" + testData.getValue());
            }else {
            	setErrorMessage("Assertion failed PDF content does not contain test data" +testData.getValue());
            	logger.warn("Assertion failed PDF content does not contain test data" +st.trim());
            	return Result.FAILED;
            }
        } catch (IOException e) {
            e.printStackTrace();
            setErrorMessage("Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
        return result;
    }
}