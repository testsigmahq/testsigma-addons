package com.testsigma.addons.pdf_action_with_file_input;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

@Data
@Action(actionText = "Read the content from pdf at file path pdf_file_path and store the text into a runtime-variable variable",
        description = "Extracts the text from the PDF (file path or URL) and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class ReadpdfContent extends PdfFileInputBase {

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        logger.debug("pdf_file_path: " + pdfFilePath.getValue() + ", variable: " + variableName.getValue());
        String pathOrUrl = pdfFilePath.getValue().toString().trim();
        File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
        if (!verifyPdfFile(basePDF)) {
            return Result.FAILED;
        }
        try (PDDocument doc = loadPdfDocument(basePDF)) {
            String st = new PDFTextStripper().getText(doc);
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(st.trim());
            runTimeData.setKey(variableName.getValue().toString());
            setSuccessMessage("The Stored content in PDF is : " + st.trim() + " and stored in the runtime variable : " + variableName.getValue());
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Cause of Exception: " + (e.getCause() != null ? e.getCause().toString() : e.getMessage()));
            return Result.FAILED;
        }
    }
}
