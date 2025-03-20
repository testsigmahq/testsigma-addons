package com.testsigma.addons.web;


import com.testsigma.addons.web.util.PDFUtils;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(actionText = "store the page count of the pdf file pdf-file-path in runtime variable variable_name",
        description = "Verifies that the base pdf and the actual pdf is same by performing visual" +
                " analysis",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class GetThePageCountOfPDF extends WebAction {
    @TestData(reference = "pdf-file-path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        PDFUtils pdfUtils = new PDFUtils(driver, logger);
        String basePdfPath = pdfFilePath.getValue().toString();

        File basePDF = pdfUtils.urlToFileConverter("base.pdf", basePdfPath);

        if (!basePDF.getName().endsWith(".pdf")) {
            setErrorMessage("Unsupported file type give only pdf file as input");
            throw new RuntimeException("Unsupported file types");
        }

        if (!basePDF.exists()) {
            setErrorMessage("Base PDF does not exist : " + basePDF.getAbsolutePath() +
                    ", please given valid file input");
            throw new RuntimeException("Base PDF not found");
        }

        try {
            logger.info("getting page count of the pdf file");
            int numberOfPagesInPdf = pdfUtils.getPdfPageCount(basePDF);
            logger.info("Number of pages in the pdf file: " + numberOfPagesInPdf);
            runTimeData.setValue(String.valueOf(numberOfPagesInPdf));
            runTimeData.setKey(runtimeVariable.getValue().toString());
            setSuccessMessage("Successfully stored the number of pages of a pdf file in runtime variable <b>"
                    + runTimeData.getKey() + " = " + runTimeData.getValue() + "</b> " +
                    "\n Screenshot might not be available for this step");
        } catch (Exception e) {
            logger.debug("Error creating temp directories and files" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unexpected Error: " + ExceptionUtils.getStackTrace(e));
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
