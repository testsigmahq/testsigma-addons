package com.testsigma.addons.web;

import com.testsigma.addons.util.PDFUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Data
@Action(actionText = "Verify if page number pages_from  to page number pages_to contains text test_data in the PDF",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class VerifyTextinparticularpage extends WebAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    Result result;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "pages_from")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "pages_to")
    private com.testsigma.sdk.TestData testData3;

    @Override
    public Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        //logger.debug("test-data: "+ this.testData.getValue());
        try {
            PDDocument doc = PDFUtils.getPDDocument(driver.getCurrentUrl());
            
            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(Integer.parseInt(testData2.getValue().toString()));
            reader.setEndPage(Integer.parseInt(testData3.getValue().toString()));
            String content = reader.getText(doc);
            
            if(content.contains(testData.getValue().toString())){
            	setSuccessMessage(SUCCESS_MESSAGE +testData.getValue().toString()+" in given range of pages ");
                logger.info(content);
                return Result.SUCCESS;
            	
            }else {
            	setErrorMessage(ERROR_MESSAGE +testData.getValue().toString()+" in given range of pages ");
                logger.warn(content);
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + " " + "Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }

    }
}
  
