package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.addons.util.PDFUtils;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Data
@Action(actionText = "Read the content from pdf and store the text into a runtime-variable variable",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class ReadpdfContent extends WebAction {

    @TestData(reference = "variable",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        //Your Awesome code starts here
        Result result = Result.SUCCESS;
        logger.info("Initiating execution");
        logger.debug("test-data: " + this.testData.getValue());
        String url = driver.getCurrentUrl();
        //StringBuffer sb = new StringBuffer();
        if (url.contains(".pdf")) {
            setSuccessMessage("pdf file detected");
            System.out.println("PDF detected");
        } else {
            setErrorMessage("Not a pdf file");
            System.out.println("PDF not detected");
            return Result.FAILED;
        }
        try {
            PDDocument doc = PDFUtils.getPDDocument(url);
            String st=new PDFTextStripper().getText(doc);
            //sb.append(new PDFTextStripper().getText(doc));
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(st.trim());
            runTimeData.setKey(testData.getValue().toString());
            setSuccessMessage("The Stored content in PDF is : " + st.trim() + "and stored in the runtime variable :" + testData.getValue());
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
        return result;
    }
}