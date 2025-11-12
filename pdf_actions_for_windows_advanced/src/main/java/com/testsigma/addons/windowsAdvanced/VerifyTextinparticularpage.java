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

@Action(actionText = "Verify if page number pages_from to page number pages_to contains text text-to-verify in the PDF file-path",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Verify text inside a pdf file with particular page range")
public class VerifyTextinparticularpage extends WindowsAdvancedAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    Result result;
    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "pages_from")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "pages_to")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;

    @Override
    public Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        try {
            // Get the file path from the user input
            String filePathValue = filePath.getValue().toString();
            logger.debug("File path: " + filePathValue);
            
            // Load PDF from local file path
            PDDocument doc = Loader.loadPDF(new java.io.File(filePathValue));

            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(Integer.parseInt(testData2.getValue().toString()));
            reader.setEndPage(Integer.parseInt(testData3.getValue().toString()));
            String content = reader.getText(doc);
            doc.close(); // Close the document to free resources

            if (content.contains(testData.getValue().toString())) {
                setSuccessMessage(SUCCESS_MESSAGE + testData.getValue().toString() + " in given range of pages ");
                logger.info(content);
                return Result.SUCCESS;

            } else {
                setErrorMessage(ERROR_MESSAGE + testData.getValue().toString() + " in given range of pages ");
                logger.warn(content);
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + " " + "Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
    }
}
