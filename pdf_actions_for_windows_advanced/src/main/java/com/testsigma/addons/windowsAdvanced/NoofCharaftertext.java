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

import java.util.ArrayList;
import java.util.List;

@Action(actionText = "Extract No_Of_Character characters after text text-to-verify from PDF file-path and store in a runtime variable runtime-variable",
        description = "Extracts the characters from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Extract characters after text from PDF")
public class NoofCharaftertext extends WindowsAdvancedAction {

    private static final String ERROR_MESSAGE = "Issue in the Operation";
    Result result;
    @TestData(reference = "No_Of_Character")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "text-to-verify")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
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
            // Get the file path from the user input
            String filePathValue = filePath.getValue().toString();
            logger.debug("File path: " + filePathValue);
            
            // Load PDF from local file path
            PDDocument doc = Loader.loadPDF(new java.io.File(filePathValue));
            sb.append(new PDFTextStripper().getText(doc));
            doc.close(); // Close the document to free resources
            
            String hj = sb.toString().replaceAll(" ", "");
            int lengthofdata = testData2.getValue().toString().length();
            int indexofword = hj.indexOf(testData2.getValue().toString());
            int sum = indexofword + lengthofdata;
            List<String> list2 = new ArrayList<String>();
            int noofchar = Integer.parseInt(testData1.getValue().toString());
            for (int i = 0; i < noofchar; i++) {
                char newc = hj.charAt(sum + i);
                list2.add(String.valueOf(newc));
            }

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(list2.toString().replaceAll("[^a-zA-Z0-9]", ""));
            runTimeData.setKey(testData3.getValue().toString());
            setSuccessMessage("Extracted " + testData1.getValue() + " characters i.e " + list2.toString().replaceAll("[^a-zA-Z0-9]", "") + " and stored it in runtime variable " + testData3.getValue().toString());
            return Result.SUCCESS;

        } catch (Exception e) {
            logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + "" + "Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
    }
}
