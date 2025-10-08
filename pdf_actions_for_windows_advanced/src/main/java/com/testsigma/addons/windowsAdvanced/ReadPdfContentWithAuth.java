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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Action(actionText = "Read the content from pdf file-path using username Username_Value and password Password_Value and store the text into a runtime-variable Variable_Name",
        description = "Extracts the text from the PDF using given username, password and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "store PDF content with authentication in a runtime variable")
public class ReadPdfContentWithAuth extends WindowsAdvancedAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "Variable_Name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "Username_Value")
    private com.testsigma.sdk.TestData username;
    @TestData(reference = "Password_Value")
    private com.testsigma.sdk.TestData password;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        try {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username.getValue().toString(),
                            password.getValue().toString().toCharArray());
                }
            });
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
                String errorMessage = ExceptionUtils.getStackTrace(e);
                logger.warn(errorMessage);
                setErrorMessage("Error occurred while reading content from pdf: " + errorMessage);
                return Result.FAILED;
            }
            return result;
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage("Error occurred while reading content from pdf: " + errorMessage);
            return Result.FAILED;
        }
    }
}
