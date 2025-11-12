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
import java.util.Arrays;
import java.util.List;

@Action(actionText = "Extract No_Of_Words words after text text-to-verify from PDF file-path and store in a runtime variable runtime-variable",
        description = "Extracts the text from the PDF and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        displayName = "Extract words after text from PDF")
public class NoofwordsAftertext extends WindowsAdvancedAction {

    private static final String SUCCESS_MESSAGE = "Stored desired data.";
    private static final String ERROR_MESSAGE = "Issue in the Operation.";
    Result result;
    @TestData(reference = "No_Of_Words")
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
            setSuccessMessage(SUCCESS_MESSAGE + "" + "Next " + testData1.getValue() + " words are " + list2.toString().replaceAll("[^a-zA-Z0-9]", " ") + " which is stored in runtime variable " + testData3.getValue().toString());
            System.out.println(SUCCESS_MESSAGE + "" + "Next " + testData1.getValue() + " words are " + list2.toString().replaceAll("[^a-zA-Z0-9]", " ") + " which is stored in runtime variable " + testData3.getValue().toString());
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.warn("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + "" + "Cause of Exception:" + e.getCause().toString());
            return Result.FAILED;
        }
    }
}
