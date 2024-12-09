package com.testsigma.addons.web;

import com.testsigma.addons.utilities.XMLUtility;
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
import java.nio.file.Files;

@Data
@Action(actionText = "Xml: Extract and store the content in the runtime-variable for XML file filepath",
        description = "Extracts the content of xml and stores it in the runtime variable only for cloud  executions",
        applicationType = ApplicationType.WEB)
public class ExtractTheContentOfXML extends WebAction {

    @TestData(reference = "filepath")
    private com.testsigma.sdk.TestData filepath;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData targetVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        final String SUCCESS_MESSAGE = "Successfully extracted content and stored it in runtime variable";
        final String FAILURE_MESSAGE = "Failed to extract content";
        logger.info("Extracting the content of XML filepath and storing it in the runtime-variable");
        logger.info("Test Data (Filepath): " + filepath.getValue().toString());
        Result result = Result.SUCCESS;

        try {
            XMLUtility xmlUtility = new XMLUtility(driver, logger);
            File temporaryXMLFile;
            temporaryXMLFile = xmlUtility.urlToFileConverter("filepath", filepath.getValue().toString());

            String xmlContent;
            xmlContent = new String(Files.readAllBytes(temporaryXMLFile.toPath()));

            logger.info("Extracted XML Content: " + xmlContent);
            runTimeData.setValue(xmlContent);
            runTimeData.setKey(targetVariable.getValue().toString());
            setSuccessMessage(SUCCESS_MESSAGE);

        } catch (Exception e) {
            logger.info("Error occurred while extracting XML content: " + ExceptionUtils.getMessage(e));
            setErrorMessage(FAILURE_MESSAGE);
            result = Result.FAILED;
        }

        return result;
    }

}
