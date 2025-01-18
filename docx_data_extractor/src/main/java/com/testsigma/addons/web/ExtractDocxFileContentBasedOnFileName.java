package com.testsigma.addons.web;


import com.testsigma.addons.web.utils.DocxDocUtilities;
import com.testsigma.addons.web.utils.DocxDocUtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;


@Data
@Action(actionText = "DOCX: Extract content from the file file-name from the downloads and store it in runtime-variable variable-name",
        description = "Extracts content by accessing the given docx file in the downloads and stores that content in a runtime variable",
        applicationType = ApplicationType.WEB)
public class ExtractDocxFileContentBasedOnFileName extends WebAction {

    @TestData(reference = "file-name")
    private com.testsigma.sdk.TestData fileName;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;


    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        DocxDocUtilities docxUtilities = DocxDocUtilitiesFactory.create(driver, logger);
        try {
            logger.info("Initiated execution");
            File downloadedDocxFile = docxUtilities.copyFileFromDownloads("docx", fileName.getValue().toString());
            FileInputStream fis = new FileInputStream(downloadedDocxFile);
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String fileContent = extractor.getText();

            logger.info("Local path: " + downloadedDocxFile.getAbsolutePath());
            runTimeData.setKey(variable.getValue().toString());
            runTimeData.setValue(fileContent);
            logger.info("File content: " + fileContent);
            setSuccessMessage("Successfully extracted the data in the file and stored in run time variable " + variable.getValue().toString() + ". " + variable.getValue().toString() + " = " + fileContent);
        } catch (RuntimeException e) {
            logger.info("Unable to find the given file in the downloads" + ExceptionUtils.getStackTrace(e));
            setErrorMessage(e.getMessage());
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to read the data in the given file");
            result = Result.FAILED;
        }
        return result;
    }
}