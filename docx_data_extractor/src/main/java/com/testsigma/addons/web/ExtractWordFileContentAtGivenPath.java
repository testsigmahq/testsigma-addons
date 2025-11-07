package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

@Data
@Action(actionText = "Word: Extract content from doc/docx file file-path and store it in runtime-variable variable-name",
        description = "Extracts content from given Word document (.doc or .docx) and stores that content in a runtime variable",
        applicationType = ApplicationType.WEB)
public class ExtractWordFileContentAtGivenPath extends WebAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath_;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiated execution");
        Result result = Result.SUCCESS;
        String originalFilePath = filePath_.getValue().toString();
        File wordFile = null;
        boolean isTempFile = false;

        try {
            String cleanPath = originalFilePath;
            int queryIndex = originalFilePath.indexOf('?');
            if (queryIndex != -1) {
                cleanPath = originalFilePath.substring(0, queryIndex);
            }
            logger.info("Cleaned path for extension check: " + cleanPath);

            final boolean isDocx;
            if (cleanPath.toLowerCase().endsWith(".docx")) {
                isDocx = true;
            } else if (cleanPath.toLowerCase().endsWith(".doc")) {
                isDocx = false;
            } else {
                throw new IllegalArgumentException("Invalid file type. Only .doc and .docx are supported.");
            }

            if (originalFilePath.toLowerCase().startsWith("http://") || originalFilePath.toLowerCase().startsWith("https://")) {
                String fileName = isDocx ? "input.docx" : "input.doc";
                wordFile = urlToFileConverter(fileName, originalFilePath);
                isTempFile = true;
            } else {
                wordFile = new File(originalFilePath);
            }

            if (!wordFile.exists()) {
                throw new IOException("File not found at the specified path: " + originalFilePath);
            }

            String text;
            try (FileInputStream fis = new FileInputStream(wordFile)) {
                if (isDocx) {
                    XWPFDocument document = new XWPFDocument(fis);
                    XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                    text = extractor.getText();
                } else {
                    HWPFDocument document = new HWPFDocument(fis);
                    WordExtractor extractor = new WordExtractor(document);
                    text = extractor.getText();
                }
            }

            logger.debug("Extracted text: " + text);

            String runtimeVariableKey = runtimeVariable.getValue().toString();
            runTimeData.setKey(runtimeVariableKey);
            runTimeData.setValue(text);

            String successMessage = "Successfully extracted data from the file and stored it in runtime variable '"
                    + runtimeVariableKey + "'. Value : " + runTimeData.getValue();
            logger.info(successMessage);
            setSuccessMessage(successMessage);

        } catch (Exception e) {
            logger.warn("Unexpected error: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("An unexpected error occurred while processing the file: " + e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }

    private File urlToFileConverter(String fileName, String url) throws IOException {
        logger.info("Given is a URL. Downloading file...");
        URL urlObject = new URL(url);

        String baseName = "download";
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            baseName = fileName.substring(0, dotIndex);
            extension = "." + fileName.substring(dotIndex + 1);
        }

        File tempFile = File.createTempFile(baseName, extension);
        FileUtils.copyURLToFile(urlObject, tempFile);
        logger.info("Temp file created for remote file " + tempFile.getName() + " at " + tempFile.getAbsolutePath());
        return tempFile;
    }
}