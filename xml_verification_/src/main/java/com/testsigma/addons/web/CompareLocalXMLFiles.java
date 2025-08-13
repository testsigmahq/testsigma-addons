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
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Action(actionText = "xml: Verify if local files with filepath filepath1 and filepath filepath2 are equal while ignoring specific XPaths X-Paths",
        description = "Verifies if two XML files are structurally and content-wise equal, reporting differences with line numbers. " +
                "Allows ignoring specific parts of the documents based on their XPath (separate by comma). " +
                "Example ignored XPaths: /Xpath1,/Xpath2",
        applicationType = ApplicationType.WEB)
public class CompareLocalXMLFiles extends WebAction {

    @TestData(reference = "filepath1")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "filepath2")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "X-Paths")
    private com.testsigma.sdk.TestData testData3;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("initiating execution");
        Result result;
        XMLUtility xmlUtility = new XMLUtility(driver, logger); // Assuming XMLUtility needs driver/logger
        File baseFile = null;
        File actualFile = null;

        try {
            // 1. Get File Paths and Ignored XPaths
            String filePath1 = testData1.getValue().toString();
            String filePath2 = testData2.getValue().toString();
            String xPathsToIgnoreRaw = testData3.getValue().toString();

            logger.info("Base file path/URL: " + filePath1);
            logger.info("Actual file path/URL: " + filePath2);
            logger.info("Raw XPaths to ignore: '" + xPathsToIgnoreRaw + "'");

            // Prepare the set of ignored paths for efficient lookup
            Set<String> ignoredPathsSet = new HashSet<>();
            if (xPathsToIgnoreRaw != null && !xPathsToIgnoreRaw.trim().isEmpty()) {
                String[] xpathArray = xPathsToIgnoreRaw.split(",");
                for (String xpath : xpathArray) {
                    String trimmedPath = xpath.trim();
                    if (!trimmedPath.isEmpty()) {
                        ignoredPathsSet.add(trimmedPath);
                        logger.info("Adding ignored XPath prefix: " + trimmedPath);
                    }
                }
            } else {
                logger.info("No XPaths specified to ignore.");
            }


            // 2. Convert URLs/Paths to Temporary Files
            baseFile = File.createTempFile("baseXml_", ".xml");
            actualFile = File.createTempFile("actualXml_", ".xml");
            logger.info("Created temporary base file: " + baseFile.getAbsolutePath());
            logger.info("Created temporary actual file: " + actualFile.getAbsolutePath());

            // Use the utility to handle potential URLs or local paths
            baseFile = xmlUtility.urlToFileConverter(filePath1);
            actualFile = xmlUtility.urlToFileConverter(filePath2);
            logger.info("Prepared base & actual file content.");

            // Ensure temp files are deleted on exit (redundant if urlToFileConverter does it, but safe)
            baseFile.deleteOnExit();
            actualFile.deleteOnExit();

            // 3. Parse XML Files with Line Number Tracking
            logger.info("Parsing base XML file...");
            Document doc1 = xmlUtility.parseXMLWithLineNumbers(baseFile);
            logger.info("Parsing actual XML file...");
            Document doc2 = xmlUtility.parseXMLWithLineNumbers(actualFile);
            logger.info("Successfully parsed both XML files with line number tracking.");


            // 4. Find Differences, Respecting Ignored Paths
            logger.info("Starting XML comparison...");
            List<String> differences = xmlUtility.findDifferencesWithIgnore(
                    doc1.getDocumentElement(),
                    doc2.getDocumentElement(),
                    baseFile.getName() + " (Base)",
                    actualFile.getName() + " (Actual)",
                    ignoredPathsSet
            );
            logger.info("Comparison finished.");


            // 5. Report Results
            if (differences.isEmpty()) {
                setSuccessMessage(String.format("<b>The given XML files are equal</b> (considering ignored XPaths).",
                        filePath1, filePath2));
                result = Result.SUCCESS;
            } else {
                // Define the character limit
                final int MAX_DETAILS_LENGTH = 350;

                // Build the difference message
                StringBuilder diffDetails = new StringBuilder();
                diffDetails.append(String.format("Found <b>%d difference(s)</b> between given xml files:\n",
                        differences.size()));

                for (int i = 0; i < differences.size(); i++) {
                    // Append the current difference detail
                    diffDetails.append(String.format("%d. %s\n", i + 1, differences.get(i)));

                    // Check if the total length now exceeds the limit after adding the latest difference
                    if (diffDetails.length() > MAX_DETAILS_LENGTH) {
                        diffDetails.append("(see Addon NLP Logs for more differences)\n");
                        break; // Stop adding more differences
                    }
                }

                logger.debug("XML Comparison Failed. Differences:\n" + diffDetails.toString());
                setErrorMessage(diffDetails.toString());
                result = Result.FAILED;
            }

        } catch (Exception e) {
            logger.debug("Error during XML comparison: " + e.getMessage() + e);
            String errorMessage = "An error occurred during XML comparison: " + ExceptionUtils.getRootCauseMessage(e);
            if (e instanceof SAXParseException) {
                errorMessage = String.format("XML Parsing Error in file around Line %d: %s",
                        ((SAXParseException) e).getLineNumber(), e.getMessage());
            } else if (e instanceof IOException) {
                errorMessage = "Error accessing or reading XML file: " + e.getMessage();
            }
            setErrorMessage(errorMessage);
            logger.debug(ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;

        } finally {
            if (baseFile != null && baseFile.exists()) { baseFile.delete(); }
            if (actualFile != null && actualFile.exists()) { actualFile.delete(); }
        }
        return result;
    }
}