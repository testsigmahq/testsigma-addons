package com.testsigma.addons.windowsAdvanced;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Action(actionText = "Read the content from pdf file-path and store the text into a runtime variable runtime-variable",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Read PDF content")
public class ReadpdfContent extends WindowsAdvancedAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

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
            
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(st.trim());
            runTimeData.setKey(testData.getValue().toString());
            setSuccessMessage("The Stored content in PDF is : " + st.trim() + " and stored in the runtime variable :" + testData.getValue());
        } catch (Exception e) {
            logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
        return result;
    }
}
