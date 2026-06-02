package com.testsigma.addons.web;

import com.testsigma.addons.web.utils.DocxDocUtilities;
import com.testsigma.addons.web.utils.DocxDocUtilitiesFactory;
import com.testsigma.addons.web.utils.WordTextExtractor;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@Data
@Action(actionText = "DOCX: Extract content from latest file in the downloads and store it in runtime-variable variable-name",
        description = "Extracts content from the latest downloaded docx file in the downloads and stores that content in a runtime variable",
        applicationType = ApplicationType.WEB)
public class ExtractLatestDownloadedDocxFileContent extends WebAction {

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
            File downloadedDocxFile = docxUtilities.copyFileFromDownloads("docx", null);
            String fileContent = WordTextExtractor.extract(downloadedDocxFile, true);

            logger.info("Local path: " + downloadedDocxFile.getAbsolutePath());
            runTimeData.setKey(variable.getValue().toString());
            runTimeData.setValue(fileContent);
            logger.info("File content: " + fileContent);
            setSuccessMessage("Successfully extracted the data in the file and stored in run time variable "
                    + variable.getValue().toString() + ". " + variable.getValue().toString() + " = " + fileContent);
        } catch (RuntimeException e) {
            logger.info("Unable to find the latest file in the downloads" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to find the latest file in the downloads");
            result = Result.FAILED;
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to read the content in the given docx file");
            result = Result.FAILED;
        }
        return result;
    }
}
