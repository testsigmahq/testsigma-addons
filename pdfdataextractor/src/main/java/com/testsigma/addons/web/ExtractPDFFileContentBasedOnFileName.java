package com.testsigma.addons.web;

import com.testsigma.addons.web.util.PdfAndDocUtilities;
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
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(actionText = "PDF: Extract content from the file file-name from the downloads and store it in runtime-variable variable-name",
        description = "Extracts content by accessing the given file in the downloads and stores that content in a run time variable",
        applicationType = ApplicationType.WEB)
public class ExtractPDFFileContentBasedOnFileName extends WebAction {
    @TestData(reference = "file-name")
    private com.testsigma.sdk.TestData fileName;

    @TestData(reference = "variable-name",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;


    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        PdfAndDocUtilities pdfAndDocUtilities = new PdfAndDocUtilities(driver,logger);
        try {
            logger.info("Initiated execution");
            File downloadedExcelFile = pdfAndDocUtilities.copyFileFromDownloads("pdf",fileName.getValue().toString());
            PDDocument document = PDDocument.load(downloadedExcelFile);
            String fileContent = "";
            if (!document.isEncrypted()) {
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                fileContent = pdfTextStripper.getText(document);
            } else {
                throw new Exception("The file in the downloads is encrypted one, Unable to access it");
            }
            logger.info("Local path"+downloadedExcelFile.getAbsolutePath());
            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(fileContent);
            logger.info("File content:"+fileContent);
            setSuccessMessage("Successfully extracted the data in the file and stored in run time variable "+runTimeData.getKey());
        } catch (RuntimeException e){
            logger.info("Unable to find the given file in the downloads"+ ExceptionUtils.getStackTrace(e));
            setErrorMessage(e.getMessage());
            result = Result.FAILED;
        }
        catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to read the data in the given file");
            result =  Result.FAILED;
        }
        return result;
    }

}