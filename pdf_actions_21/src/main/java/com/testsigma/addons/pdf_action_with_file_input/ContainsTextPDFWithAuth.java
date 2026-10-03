package com.testsigma.addons.pdf_action_with_file_input;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Data
@Action(actionText = "Verify if the pdf at file path pdf_file_path contains text test-data using username Username_Value and password Password_Value",
        description = "Validates whether the given test-data is present in the PDF (file path or URL, with optional auth for URLs)",
        applicationType = ApplicationType.WEB)
public class ContainsTextPDFWithAuth extends PdfFileInputBase {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "Username_Value")
    private com.testsigma.sdk.TestData username;
    @TestData(reference = "Password_Value")
    private com.testsigma.sdk.TestData password;

    @Override
    public Result execute() {
        try {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username.getValue().toString(),
                            password.getValue().toString().toCharArray());
                }
            });

            logger.info("Initiating execution");
            logger.debug("pdf_file_path: " + pdfFilePath.getValue() + ", test-data: " + testData.getValue());
            String pathOrUrl = pdfFilePath.getValue().toString().trim();
            File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
            if (!verifyPdfFile(basePDF)) {
                return Result.FAILED;
            }
            try (PDDocument doc = loadPdfDocument(basePDF)) {
                String text = new PDFTextStripper().getText(doc);
                if (text.contains((CharSequence) testData.getValue())) {
                    setSuccessMessage(SUCCESS_MESSAGE + " " + testData.getValue());
                    return Result.SUCCESS;
                } else {
                    setErrorMessage(ERROR_MESSAGE + " " + testData.getValue());
                    logger.warn("Error message: " + text);
                    return Result.FAILED;
                }
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage(ERROR_MESSAGE + ": " + errorMessage);
            return Result.FAILED;
        }
    }
}
