package com.testsigma.addons.web;

import com.testsigma.addons.web.util.PdfDocUtilities;
import com.testsigma.addons.web.util.PdfDocUtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(
        actionText = "PDF: Extract content from latest file in the downloads with password password-value and store it in runtime-variable variable-name",
        description = "Extracts content from latest downloaded file in the downloads and stores that content in a run time variable",
        applicationType = ApplicationType.WEB
)
public class ExtractLatestDownloadedPDFFileContentUsingPassword extends WebAction {

    @TestData(reference = "password-value")
    private com.testsigma.sdk.TestData passwordValue;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        PdfDocUtilities pdfAndDocUtilities = PdfDocUtilitiesFactory.create(driver, logger);

        try {
            logger.info("Initiated execution");

            // Get the latest downloaded PDF file
            File downloadedPdfFile = pdfAndDocUtilities.copyFileFromDownloads("pdf", null);
            logger.info("Local path: " + downloadedPdfFile.getAbsolutePath());

            // Load PDF with password
            PDDocument document = Loader.loadPDF(downloadedPdfFile, passwordValue.getValue().toString());
            try {
                if (document.isEncrypted()) {
                    logger.info("PDF was encrypted, but opened successfully with the provided password.");
                }

                // Extract content
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                String fileContent = pdfTextStripper.getText(document);

                // Store in runtime variable
                runTimeData.setKey(runtimeVariable.getValue().toString());
                runTimeData.setValue(fileContent);

                logger.info("File content: " + fileContent);
                setSuccessMessage("Successfully extracted the data in the file and stored in run time variable "
                        + runTimeData.getKey() + " value : " + runTimeData.getValue());
            } finally {
                document.close();
            }

        } catch (RuntimeException e) {
            logger.info("Unable to find the latest file in the downloads " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to find the latest file in the downloads");
            result = Result.FAILED;

        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to read the content in the given pdf file");
            result = Result.FAILED;
        }

        return result;
    }
}
