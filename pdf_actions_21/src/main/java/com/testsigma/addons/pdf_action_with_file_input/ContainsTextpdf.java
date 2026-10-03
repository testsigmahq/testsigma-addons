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
@Action(actionText = "Verify if the pdf at file path pdf_file_path contains text test_data",
        description = "Extracts the text from the PDF (file path or URL as testdata) and verifies it contains the given text",
        applicationType = ApplicationType.WEB)
public class ContainsTextpdf extends PdfFileInputBase {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        logger.debug("pdf_file_path: " + pdfFilePath.getValue() + ", test_data: " + testData.getValue());
        String pathOrUrl = pdfFilePath.getValue().toString().trim();
        File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
        if (!verifyPdfFile(basePDF)) {
            return Result.FAILED;
        }
        try (PDDocument doc = loadPdfDocument(basePDF)) {
            String text = new PDFTextStripper().getText(doc);
            if (text.contains((CharSequence) testData.getValue())) {
                setSuccessMessage(SUCCESS_MESSAGE + " " + testData.getValue());
                logger.info(SUCCESS_MESSAGE + " " + testData.getValue());
                return Result.SUCCESS;
            } else {
                setErrorMessage(ERROR_MESSAGE + " " + testData.getValue());
                logger.warn(ERROR_MESSAGE + " " + testData.getValue() + ":" + text);
                return Result.FAILED;
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            setErrorMessage(ERROR_MESSAGE + " " + testData.getValue() + "   Cause of Exception:" + errorMessage);
            logger.warn(ERROR_MESSAGE + " " + testData.getValue() + "   Cause of Exception:" + errorMessage);
            return Result.FAILED;
        }
    }
}
