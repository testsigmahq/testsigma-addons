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

@Data
@Action(actionText = "Verify the content from pdf at file path pdf_file_path contains text testdata",
        description = "Extracts the text from the PDF (file path or URL) and verifies it contains the given text",
        applicationType = ApplicationType.WEB)
public class VerifypdfContent extends PdfFileInputBase {

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        logger.debug("pdf_file_path: " + pdfFilePath.getValue() + ", testdata: " + testData.getValue());
        String pathOrUrl = pdfFilePath.getValue().toString().trim();
        File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
        if (!verifyPdfFile(basePDF)) {
            return Result.FAILED;
        }
        try (PDDocument doc = loadPdfDocument(basePDF)) {
            String st = new PDFTextStripper().getText(doc);
            if (st.trim().contains(testData.getValue().toString())) {
                setSuccessMessage("Assertion passed PDF content contains test data " + testData.getValue());
                logger.info("Assertion passed PDF content contains test data " + testData.getValue());
                return Result.SUCCESS;
            } else {
                setErrorMessage("Assertion failed PDF content does not contain test data " + testData.getValue());
                logger.warn("Assertion failed PDF content does not contain test data. Content: " + st.trim());
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Cause of Exception: " + (e.getCause() != null ? e.getCause().toString() : e.getMessage()));
            return Result.FAILED;
        }
    }
}
