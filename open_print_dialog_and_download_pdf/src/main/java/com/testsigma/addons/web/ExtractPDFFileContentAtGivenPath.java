package com.cyclelove.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Data
@Action(actionText = "PDF: Extract content from pdf file file-path and store it in runtime-variable variable-name",
        description = "Extracts content from given file and stores that content in a runtime variable",
        applicationType = ApplicationType.WEB)
public class ExtractPDFFileContentAtGivenPath extends WebAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath_;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        String filePath = filePath_.getValue().toString();
        try {
            logger.info("Initiated execution");
            File downloadedExcelFile = urlToFileConverter("input.pdf", filePath);
            PDDocument document = Loader.loadPDF(downloadedExcelFile);
            String fileContent = "";
            if (!document.isEncrypted()) {
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                fileContent = pdfTextStripper.getText(document);
            } else {
                throw new IOException("The file in the downloads is encrypted, Unable to access it");
            }
            logger.info("Local path: " + downloadedExcelFile.getAbsolutePath());

            String runtimeVariableKey = runtimeVariable.getValue().toString();
            runTimeData.setKey(runtimeVariableKey);
            runTimeData.setValue(fileContent);

            // Create and log the success message
            String successMessage = "Successfully extracted the data in the file and stored in runtime variable "
                    + runtimeVariableKey + ". Value: " + fileContent;
            logger.info(successMessage);
            setSuccessMessage(successMessage);

        } catch (RuntimeException e) {
            logger.info("Unable to find the latest file in the downloads: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to find the latest file in the downloads");
            result = Result.FAILED;
        } catch (IOException e) {
            logger.info("IOException: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to read the content in the given pdf file");
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("An unexpected error occurred while processing the file");
            result = Result.FAILED;
        }
        return result;
    }

    public File urlToFileConverter(String fileName, String url) throws IOException {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            logger.info("Given is an S3 URL ... File name: " + fileName);
            URL urlObject = new URL(url);
            File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
            FileUtils.copyURLToFile(urlObject, tempFile);
            logger.info("Temp file created with name for S3 file " + tempFile.getName() + " at path " + tempFile.getAbsolutePath());
            return tempFile;
        } else {
            logger.info("Given is a local file path..");
            return new File(url);
        }
    }
}
