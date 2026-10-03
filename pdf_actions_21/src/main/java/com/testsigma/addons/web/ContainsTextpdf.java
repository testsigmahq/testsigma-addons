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
@Action(actionText = "Verify if the pdf contains text test_data",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class ContainsTextpdf extends WebAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    Result result;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        logger.debug("test-data: " + this.testData.getValue());
        StringBuffer sb = new StringBuffer();
        try {
            PDDocument doc = PDFUtils.getPDDocument(driver.getCurrentUrl());
            sb.append(new PDFTextStripper().getText(doc));
            if (sb.toString().contains((CharSequence) testData.getValue())) {
            	setSuccessMessage(SUCCESS_MESSAGE + " " + testData.getValue());
            	logger.info(SUCCESS_MESSAGE + " " + testData.getValue());
                return Result.SUCCESS;
            } else {
            	setErrorMessage(ERROR_MESSAGE + " " + testData.getValue());
            	logger.warn(ERROR_MESSAGE + " " + testData.getValue() +":" +sb.toString());
                return Result.FAILED;
            }
        } catch (Exception e) {
    		String errorMessage = ExceptionUtils.getStackTrace(e);
    		setErrorMessage(ERROR_MESSAGE + "" + testData.getValue() + "   " + "Cause of Exception:" + errorMessage);
    		logger.warn(ERROR_MESSAGE + "" + testData.getValue() + "   " + "Cause of Exception:" + errorMessage);
    		return Result.FAILED;
    	} 
    }
}