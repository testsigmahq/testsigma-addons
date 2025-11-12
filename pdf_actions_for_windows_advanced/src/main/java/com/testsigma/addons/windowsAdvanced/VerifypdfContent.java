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

@Action(actionText = "Verify the content from pdf file-path contains text testdata",
        description = "Extracts the text from the PDF and verify the text",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Verify PDF content contains text")
public class VerifypdfContent extends WindowsAdvancedAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() {
        //Your Awesome code starts here
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("test-data: " + this.testData.getValue());
        
        // Get the file path from the user input
        String filePathValue = filePath.getValue().toString();
        logger.debug("File path: " + filePathValue);
        
        // Check if file has .pdf extension
        if (filePathValue.toLowerCase().contains(".pdf")) {
            setSuccessMessage("PDF file detected");
            System.out.println("PDF detected");
        } else {
            setErrorMessage("Not a PDF file");
            System.out.println("PDF not detected");
            return Result.FAILED;
        }
        try {
            // Load PDF from local file path
            PDDocument doc = Loader.loadPDF(new java.io.File(filePathValue));
            String st = new PDFTextStripper().getText(doc);
            doc.close(); // Close the document to free resources

            if (st.trim().contains(testData.getValue().toString())) {
                setSuccessMessage("Assertion passed PDF content contains test data" + testData.getValue());
                logger.info("Assertion passed PDF content contains test data" + testData.getValue());
            } else {
                setErrorMessage("Assertion failed PDF content does not contain test data" + testData.getValue());
                logger.warn("Assertion failed PDF content does not contain test data" + st.trim());
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
        return result;
    }
}
