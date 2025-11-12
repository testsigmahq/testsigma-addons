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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Action(actionText = "Verify if the pdf file-path contains text text-to-verify using username Username_Value and password Password_Value",
        description = "This action will validate whether given text is present in pdf or not using given username and password",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Verify if Pdf file contains text with authentication")
public class ContainsTextPDFWithAuth extends WindowsAdvancedAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    Result result;
    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "Username_Value")
    private com.testsigma.sdk.TestData username;
    @TestData(reference = "Password_Value")
    private com.testsigma.sdk.TestData password;

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

            logger.info("Initiating execution");
            logger.debug("test-data: " + this.testData.getValue());
            StringBuffer sb = new StringBuffer();
            
            // Get the file path from the user input
            String filePathValue = filePath.getValue().toString();
            logger.debug("File path: " + filePathValue);
            
            // Load PDF from local file path
            PDDocument doc = Loader.loadPDF(new java.io.File(filePathValue));
            try {
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
                logger.warn("Error message:" + errorMessage + ":" + sb.toString());
                setErrorMessage(ERROR_MESSAGE + " " + testData.getValue() + " in file: " + filePath.getValue() + "   " + "Cause of Exception:" + errorMessage);
                return Result.FAILED;
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage(ERROR_MESSAGE + " in file: " + filePath.getValue() + ": " + errorMessage);
            return Result.FAILED;
        }
    }
}
