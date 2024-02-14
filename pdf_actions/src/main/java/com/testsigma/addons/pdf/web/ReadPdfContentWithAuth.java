package com.testsigma.addons.pdf.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

@Data
@Action(actionText = "Read the content from pdf using username Username_Value and password Password_Value and store the text into a runtime-variable Variable_Name",
        description = "Extracts the text from the PDF using given username, password and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class ReadPdfContentWithAuth extends WebAction {

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
        try{
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
            //StringBuffer sb = new StringBuffer();
            URL url;
            BufferedInputStream fileParse;
            if (driver.getCurrentUrl().contains(".pdf")) {
                setSuccessMessage("pdf file detected");
                System.out.println("PDF detected");
            } else {
                setErrorMessage("Not a pdf file");
                System.out.println("PDF not detected");
                return Result.FAILED;
            }
            try {
                url = new URL(driver.getCurrentUrl());
                InputStream is = url.openStream();   //Converting url as input
                fileParse = new BufferedInputStream(is);   //reads data from file
                PDDocument doc = null;
                doc = PDDocument.load(fileParse); //loads the file as pdf
                String st = new PDFTextStripper().getText(doc);
                //sb.append(new PDFTextStripper().getText(doc));
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(st.trim());
                runTimeData.setKey(testData.getValue().toString());
                setSuccessMessage("The Stored content in PDF is : " + st.trim() + "and stored in the runtime variable :"
                        + testData.getValue());
            } catch (Exception e) {
                String errorMessage = ExceptionUtils.getStackTrace(e);
                logger.info(errorMessage);
                setErrorMessage("Error occurred while reading content from pdf: "+ errorMessage);
                return Result.FAILED;
            }
            return result;
        } catch (Exception e){
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage("Error occurred while reading content from pdf: "+ errorMessage);
            return Result.FAILED;
        }
    }
}