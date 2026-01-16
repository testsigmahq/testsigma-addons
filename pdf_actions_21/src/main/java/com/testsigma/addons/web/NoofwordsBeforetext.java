package com.testsigma.addons.web;

import com.testsigma.addons.util.PDFUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Action(actionText = "Extract No_Of_Words words before text test_data from PDF and store in a runtime variable Variable_Name",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class NoofwordsBeforetext extends WebAction {

    private static final String SUCCESS_MESSAGE = "Stored desired data. ";
    private static final String ERROR_MESSAGE = "Issue in the Operation";
    Result result;
    @TestData(reference = "No_Of_Words")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "Variable_Name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        //Your Awesome code starts here

        logger.info("Initiating execution");
        logger.debug("test-data1: " + this.testData1.getValue() + "test-data2: " + this.testData2.getValue() + "test-data3: " + this.testData3);
        //StringBuffer sb = new StringBuffer();
        try {
            PDDocument doc = PDFUtils.getPDDocument(driver.getCurrentUrl());
            String str=new PDFTextStripper().getText(doc);
            //sb.append(new PDFTextStripper().getText(doc));
           
            String[] as = str.toString().split("\\W+");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(as));
            int a = list.indexOf(testData2.getValue());
            int wordcount = Integer.parseInt((String) testData1.getValue());
            List<String> list2 = new ArrayList<String>();

            for (int i = wordcount; i >= 1; i--) {
                list2.add(list.get(a - i));
            }
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(list2.toString().replaceAll("[^a-zA-Z0-9]", " "));
            runTimeData.setKey(testData3.getValue().toString());
           
          
          setSuccessMessage(SUCCESS_MESSAGE  +" "+"Previous " + testData1.getValue() + " words are " + list2.toString().replaceAll("[^a-zA-Z0-9]", " ")+"stored in runtime variable "+testData3.getValue().toString());
           System.out.println(SUCCESS_MESSAGE  +" "+"Previous " + testData1.getValue() + " words are " + list2.toString().replaceAll("[^a-zA-Z0-9]", " ")+"stored in runtime variable "+testData3.getValue().toString());
          return Result.SUCCESS;

        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE+ "" + "Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
    }
}
  
