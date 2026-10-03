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
import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Data
@Action(actionText = "Read the content from pdf at file path pdf_file_path using username Username_Value and password Password_Value and store the text into a runtime-variable Variable_Name",
        description = "Extracts the text from the PDF (file path or URL, with optional auth for URLs) and stores the extracted text into a run time variable",
        applicationType = ApplicationType.WEB)
public class ReadPdfContentWithAuth extends PdfFileInputBase {

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "Variable_Name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "Username_Value")
    private com.testsigma.sdk.TestData username;
    @TestData(reference = "Password_Value")
    private com.testsigma.sdk.TestData password;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

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
            logger.debug("pdf_file_path: " + pdfFilePath.getValue() + ", variable: " + testData.getValue());
            String pathOrUrl = pdfFilePath.getValue().toString().trim();
            File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
            if (!verifyPdfFile(basePDF)) {
                return Result.FAILED;
            }
            try (PDDocument doc = loadPdfDocument(basePDF)) {
                String st = new PDFTextStripper().getText(doc);
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(st.trim());
                runTimeData.setKey(testData.getValue().toString());
                setSuccessMessage("The Stored content in PDF is : " + st.trim() + " and stored in the runtime variable : " + testData.getValue());
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.info(errorMessage);
            setErrorMessage("Error occurred while reading content from pdf: " + errorMessage);
            return Result.FAILED;
        }
    }
}
