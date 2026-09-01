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
import java.util.List;

@Data
@Action(actionText = "Extract No_Of_Character characters before text test_data from PDF and store in a runtime variable Variable_Name",
        description = "Extracts the characters from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class NoofCharbeforetext extends WebAction {

    private static final String SUCCESS_MESSAGE = "Characters extracted before text and Stored in the runtime variable";
    private static final String ERROR_MESSAGE = "Issue in the Operation";
    Result result;
    @TestData(reference = "No_Of_Character")
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
        StringBuffer sb = new StringBuffer();
        try {
            PDDocument doc = PDFUtils.getPDDocument(driver.getCurrentUrl());
            sb.append(new PDFTextStripper().getText(doc));
            String hj = sb.toString().replaceAll(" ", "");
            System.out.println(hj);
            int indextofword = hj.indexOf(testData2.getValue().toString());
            int postionofdesiredchar = Integer.parseInt(testData1.getValue().toString());
            List<String> list2 = new ArrayList<String>();

            for (int i = postionofdesiredchar; i >= 1; i--) {
                char newc = hj.charAt(indextofword - i);
                list2.add(String.valueOf(newc));
            }

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(list2.toString().replaceAll("[^a-zA-Z0-9]", ""));
            runTimeData.setKey(testData3.getValue().toString());
            setSuccessMessage("Extracted "+testData1.getValue().toString()+" characters before test data "+testData2.getValue().toString()+" and stored extacted characters i.e "+list2.toString().replaceAll("[^a-zA-Z0-9]", "")+"  in a runtime variable "+testData3.getValue().toString());
            System.out.println("Extracted "+testData1.getValue().toString()+" characters before test data "+testData2.getValue().toString()+" and stored extacted characters i.e "+list2.toString().replaceAll("[^a-zA-Z0-9]", "")+" in a runtime variable "+testData3.getValue().toString());
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage( ERROR_MESSAGE);
            //System.out.println(ERROR_MESSAGE + "" + "Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }

      
    }
}
  
