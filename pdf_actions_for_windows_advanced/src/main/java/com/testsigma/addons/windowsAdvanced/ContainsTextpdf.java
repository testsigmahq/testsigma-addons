package com.testsigma.addons.windowsAdvanced;


import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


@Action(actionText = "Verify if the pdf file-path contains text test_data",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
    displayName = "Verify if Pdf file contains text")
public class ContainsTextpdf extends WindowsAdvancedAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    Result result;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @Override
    public Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        logger.debug("test-data: " + this.testData.getValue());
        StringBuffer sb = new StringBuffer();
        try {
            // Get the file path from the user input
            String filePathValue = filePath.getValue().toString();
            logger.debug("File path: " + filePathValue);
            
            // Load PDF from local file path
            PDDocument doc = Loader.loadPDF(new java.io.File(filePathValue));
            sb.append(new PDFTextStripper().getText(doc));
            doc.close(); // Close the document to free resources
            
            if (sb.toString().contains((CharSequence) testData.getValue())) {
            	setSuccessMessage(SUCCESS_MESSAGE + " " + testData.getValue());
            	logger.info(SUCCESS_MESSAGE + " " + testData.getValue());
                return Result.SUCCESS;
            } else {
            	setErrorMessage(ERROR_MESSAGE + " " + testData.getValue());
            	logger.warn(ERROR_MESSAGE + " " + testData.getValue() + " in file: " + filePathValue);
                return Result.FAILED;
            }
        } catch (Exception e) {
    		String errorMessage = ExceptionUtils.getStackTrace(e);
    		setErrorMessage(ERROR_MESSAGE + " " + testData.getValue() + " in file: " + filePath.getValue() + "   " + "Cause of Exception:" + errorMessage);
    		logger.warn(ERROR_MESSAGE + " " + testData.getValue() + " in file: " + filePath.getValue() + "   " + "Cause of Exception:" + errorMessage);
    		return Result.FAILED;
    	} 
    }
}