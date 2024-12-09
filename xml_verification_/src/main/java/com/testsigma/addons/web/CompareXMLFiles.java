package com.testsigma.addons.web;

import com.testsigma.addons.utilities.XMLUtility;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.w3c.dom.Document;

import java.io.File;

@Data
@Action(actionText = "Xml: Verify that the base xml base-xml-file-path and the actual xml actual-xml-file-path are equal while ignoring specific XPaths X-Paths",
        description = "Verifies if files with given filepath are equal while ignoring given xPaths " +
                "(separate xPaths by comma) e.g: xpath1, xpath2 this works for both local and cloud executions",
        applicationType = ApplicationType.WEB)
public class CompareXMLFiles extends WebAction {

    @TestData(reference = "base-xml-file-path")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "actual-xml-file-path")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "X-Paths")
    private com.testsigma.sdk.TestData testData3;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result ;
        try {
            XMLUtility xmlUtility = new XMLUtility(driver, logger);

            // Get the file paths
            String xPathsToIgnore = testData3.getValue().toString();
            logger.info("xPaths to be ignored: " + xPathsToIgnore);

            File baseFile = File.createTempFile("tempFile1_", ".xml");
            File actualFile = File.createTempFile("tempFile2_", ".xml");

            logger.info("created temp files");
            baseFile  = xmlUtility.urlToFileConverter(baseFile.getName(), testData1.getValue().toString());
            actualFile = xmlUtility.urlToFileConverter(actualFile.getName(), testData2.getValue().toString());

            logger.info("converted temp files");
            // deleting files on completion of execution
            baseFile.deleteOnExit();
            actualFile.deleteOnExit();

            // Parsing XML files
            Document doc1 = xmlUtility.parseXML(baseFile);
            Document doc2 = xmlUtility.parseXML(actualFile);
            logger.info("Parsed the xml files");
            // Removing nodes based on XPath expressions
            String[] xpathArray = xPathsToIgnore.split(",");
            for (String xpathExpression : xpathArray) {
                xmlUtility.removeNodesByXPath(doc1, xpathExpression.trim());
                xmlUtility.removeNodesByXPath(doc2, xpathExpression.trim());
            }

            logger.info("successfully Ignored xPaths of the given xml files");

            // Compare the modified documents
            boolean isEqual = doc1.isEqualNode(doc2);
            if (isEqual) {
                setSuccessMessage("The XML files are equal.");
                result = Result.SUCCESS;
            } else {
                setErrorMessage("The base XML file is not same as the actual XML file.");
                result = Result.FAILED;
            }
            return result;

        } catch (Exception e) {
            setErrorMessage("An error occurred, please check if the Xml file is not damaged " + ExceptionUtils.getMessage(e));
            logger.info(ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}
