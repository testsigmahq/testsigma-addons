package com.testsigma.addons.pdf.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.testng.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

@Data
@Action(actionText = "Verify if the pdf contains text test-data using username Username_Value and password Password_Value",
        description = "This action will validate whether given test-data present in pdf or not using given username and password",
        applicationType = ApplicationType.WEB)
public class ContainsTextPDFWithAuth extends WebAction {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";
    com.testsigma.sdk.Result result;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "Username_Value")
    private com.testsigma.sdk.TestData username;
    @TestData(reference = "Password_Value")
    private com.testsigma.sdk.TestData password;

    @Override
    public com.testsigma.sdk.Result execute() {
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
            URL url;
            BufferedInputStream fileParse;
            PDDocument doc = null;
            try {
                url = new URL(driver.getCurrentUrl());
                InputStream is = url.openStream();   //Converting url as input
                fileParse = new BufferedInputStream(is);   //reads data from file
                doc = PDDocument.load(fileParse); //loads the file as pdf
                sb.append(new PDFTextStripper().getText(doc));
                Assert.assertTrue(sb.toString().contains((CharSequence) testData.getValue()));
                setSuccessMessage(String.format(SUCCESS_MESSAGE + " " + testData.getValue()));
                return Result.SUCCESS;
            } catch (Exception e) {

                String errorMessage = ExceptionUtils.getStackTrace(e);
                logger.info(errorMessage);
                setErrorMessage(String.format(ERROR_MESSAGE + " " + testData.getValue() + "   " + "Cause of Exception:"
                        +errorMessage));
                return Result.FAILED;
            }
        }
        catch (Exception e){
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage(ERROR_MESSAGE + ": "+ errorMessage);
            return Result.FAILED;
        }
    }
}

