package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Data
@Action(actionText = "Update the given xml filepath by finding tagname with old_value at index and replacing with new_value",
        description = "Update the xml file by finding the Nth (index) element matching tagname and old_value, then replacing its content with new_value",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)

public class UpdateXmlInGivenFileWithNewData extends WebAction {
    @TestData(reference = "filepath")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "tagname")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "old_value")
    private com.testsigma.sdk.TestData testData3;
    @TestData(reference = "new_value")
    private com.testsigma.sdk.TestData testData4;
    @TestData(reference = "index")
    private com.testsigma.sdk.TestData testData5;
    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            String filePath = testData1.getValue().toString();
            String tagName = testData2.getValue().toString();
            String oldValue = testData3.getValue().toString();
            String newValue = testData4.getValue().toString();
            int targetIndex = Integer.parseInt(testData5.getValue().toString());

            logger.info("Parameters - filepath: " + filePath + ", tagname: " + tagName + ", old_value: " + oldValue + ", new_value: " + newValue + ", index: " + targetIndex);

            File inputFile = new File(filePath);
            if (!inputFile.exists()) {
                logger.warn("File not found: " + filePath);
                setErrorMessage("File not found: " + filePath);
                return com.testsigma.sdk.Result.FAILED;
            }

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputFile);
            logger.info("XML file parsed successfully");

            NodeList nodes = doc.getElementsByTagName(tagName);
            logger.info("Total <" + tagName + "> elements found in document: " + nodes.getLength());

            Element matchedElement = null;
            int matchCount = 0;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getTextContent().equals(oldValue)) {
                    if (matchCount == targetIndex) {
                        matchedElement = el;
                        break;
                    }
                    matchCount++;
                }
            }

            logger.info("Search: tag=<" + tagName + ">, old_value='" + oldValue + "', index=" + targetIndex + " -> " + (matchedElement != null ? "found" : "not found"));

            if (matchedElement != null) {
                logger.info("Updating element content from '" + oldValue + "' to '" + newValue + "'");
                matchedElement.setTextContent(newValue);
            } else {
                logger.warn("No <" + tagName + "> element with value '" + oldValue + "' found at index " + targetIndex);
                setErrorMessage("No <" + tagName + "> element with value '" + oldValue + "' found at index " + targetIndex + ".");
                return com.testsigma.sdk.Result.FAILED;
            }

            // Save the changes to the XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File tempFile = File.createTempFile("xml_update", ".tmp", inputFile.getParentFile());
            StreamResult resultFile = new StreamResult(tempFile);
            transformer.transform(source, resultFile);
            Files.move(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("XML file saved successfully at: " + inputFile.getAbsolutePath());
            setSuccessMessage("XML file updated successfully.");
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
        }

        return result;

    }
}



