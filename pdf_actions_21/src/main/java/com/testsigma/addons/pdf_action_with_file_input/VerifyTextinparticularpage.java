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
@Action(actionText = "Verify if page number pages_from to page number pages_to contains text test_data in the PDF at file path pdf_file_path",
        description = "Extracts the text from the given page range of the PDF (file path or URL) and verifies it contains the given text",
        applicationType = ApplicationType.WEB)
public class VerifyTextinparticularpage extends PdfFileInputBase {

    private static final String SUCCESS_MESSAGE = "Assertion passed PDF content contains test data";
    private static final String ERROR_MESSAGE = "Assertion failed PDF content does not contain test data";

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "pages_from")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "pages_to")
    private com.testsigma.sdk.TestData testData3;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        logger.debug("pdf_file_path: " + pdfFilePath.getValue() + ", test_data: " + testData.getValue() + ", pages: " + testData2.getValue() + "-" + testData3.getValue());
        String pathOrUrl = pdfFilePath.getValue().toString().trim();
        File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
        if (!verifyPdfFile(basePDF)) {
            return Result.FAILED;
        }
        try (PDDocument doc = loadPdfDocument(basePDF)) {
            PDFTextStripper reader = new PDFTextStripper();
            reader.setStartPage(Integer.parseInt(testData2.getValue().toString()));
            reader.setEndPage(Integer.parseInt(testData3.getValue().toString()));
            String content = reader.getText(doc);

            if (content.contains(testData.getValue().toString())) {
                setSuccessMessage(SUCCESS_MESSAGE + " " + testData.getValue().toString() + " in given range of pages ");
                logger.info(content);
                return Result.SUCCESS;
            } else {
                setErrorMessage(ERROR_MESSAGE + " " + testData.getValue().toString() + " in given range of pages ");
                logger.warn(content);
                return Result.FAILED;
            }
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + " Cause of Exception: " + (e.getCause() != null ? e.getCause().toString() : e.getMessage()));
            return Result.FAILED;
        }
    }
}
