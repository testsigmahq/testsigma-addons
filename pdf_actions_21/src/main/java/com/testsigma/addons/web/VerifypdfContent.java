package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import com.testsigma.addons.util.PDFUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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
        String url = driver.getCurrentUrl();
        if (url.contains(".pdf")) {
            setSuccessMessage("pdf file detected");
            System.out.println("PDF detected");
        } else {
            setErrorMessage("Not a pdf file");
            System.out.println("PDF not detected");
            return Result.FAILED;
        }
        try {
            PDDocument doc = PDFUtils.getPDDocument(url);
            String st=new PDFTextStripper().getText(doc);
            
            if(st.trim().contains(testData.getValue().toString())) {
            	setSuccessMessage("Assertion passed PDF content contains test data" + testData.getValue());
            	logger.info("Assertion passed PDF content contains test data" + testData.getValue());
            }else {
            	setErrorMessage("Assertion failed PDF content does not contain test data" +testData.getValue());
            	logger.warn("Assertion failed PDF content does not contain test data" +st.trim());
            	return Result.FAILED;
            }
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
        return result;
    }
}