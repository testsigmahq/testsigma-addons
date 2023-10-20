package com.testsigma.addons.pdf.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Action(actionText = "Extract No_Of_Words words after text test_data from PDF and store in a runtime variable Variable_Name",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class NoofwordsAftertext extends WebAction {

    private static final String SUCCESS_MESSAGE = "Stored desired data.";
    private static final String ERROR_MESSAGE = "Issue in the Operation.";
    com.testsigma.sdk.Result result;
    @TestData(reference = "No_Of_Words")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "Variable_Name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        logger.debug("test-data1: " + this.testData1.getValue() + "test-data2: " + this.testData2.getValue() + "test-data3: " + this.testData3);
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
           // System.out.println(sb.toString());
            String[] as = sb.toString().split("\\W+");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(as));
            int a = list.indexOf(testData2.getValue());
            int wordcount = Integer.parseInt((String) testData1.getValue());
            List<String> list2 = new ArrayList<String>();
            for (int i = 1; i <= wordcount; i++) {
                list2.add(list.get(a + (i)));
            }
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(list2.toString().replaceAll("[^a-zA-Z0-9]", " "));
            runTimeData.setKey(testData3.getValue().toString());
            setSuccessMessage(String.format(SUCCESS_MESSAGE  +""+"Next " + testData1.getValue() + " words are " + list2.toString().replaceAll("[^a-zA-Z0-9]", " ")+" which is stored in runtime variable "+testData3.getValue().toString()));
            System.out.println(String.format(SUCCESS_MESSAGE  +""+"Next " + testData1.getValue() + " words are " + list2.toString().replaceAll("[^a-zA-Z0-9]", " ")+" which is stored in runtime variable "+testData3.getValue().toString()));
            return Result.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            setErrorMessage(String.format(ERROR_MESSAGE + "" + "Cause of Exception:" + e.getCause().toString()));
            return Result.FAILED;
        }

       
    }
}
  
