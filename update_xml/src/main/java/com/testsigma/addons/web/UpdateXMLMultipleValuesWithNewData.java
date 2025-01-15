package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Action(actionText = "Update XML file filepath using tagnames from old_value to new_values and store the path in runtime variable variable-name",
        description = "Updates an XML file by changing the value of specified tags, and stores the modified file path in a runtime variable.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class UpdateXMLMultipleValuesWithNewData extends WebAction {

    @TestData(reference = "filepath")
    private com.testsigma.sdk.TestData filePathTestData;

    @TestData(reference = "tagnames")
    private com.testsigma.sdk.TestData tagNamesTestData;

    @TestData(reference = "old_value")
    private com.testsigma.sdk.TestData oldValueTestData;


    @TestData(reference = "new_values")
    private com.testsigma.sdk.TestData newValuesTestData;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableNameTestData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("Initiating XML update action.");
        Result result = Result.SUCCESS;
        File xmlFileToUse = null;
        String filePath = filePathTestData.getValue().toString();

        try {
            // Download the file if it's a URL
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                xmlFileToUse = downloadFile(filePath);
            } else {
                xmlFileToUse = new File(filePath);
            }
            logger.info("XML file loaded: " + xmlFileToUse.getAbsolutePath());

            // Parse XML using JDOM2
            SAXBuilder saxBuilder = new SAXBuilder();
            Document doc = saxBuilder.build(xmlFileToUse);
            logger.info("XML Document parsed successfully.");

            // Log the entire XML document for inspection
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            String xmlString = xmlOutputter.outputString(doc);

            // Extract tag names and new values
            String tagnamesStr = tagNamesTestData.getValue().toString();
            String newValuesStr = newValuesTestData.getValue().toString();

            List<String> tagnames = Arrays.stream(tagnamesStr.split(",")).map(String::trim).collect(Collectors.toList());
            List<String> newValues = Arrays.stream(newValuesStr.split(",")).map(String::trim).collect(Collectors.toList());


            if (tagnames.size() != newValues.size()) {
                throw new IllegalArgumentException("The number of tag names must match the number of new values.");
            }

            // Iterate through tagnames and newValues to update the XML content
            for (int i = 0; i < tagnames.size(); i++) {
                String tagname = tagnames.get(i);
                String newValue = newValues.get(i);
                logger.info("Processing tag: " + tagname + ", setting value to: " + newValue);
                updateTagValue(doc.getRootElement(), tagname, newValue);
            }


            // Write the updated document back to the file
            xmlOutputter.output(doc, new FileOutputStream(xmlFileToUse));
            logger.info("XML file updated successfully.");


            // Store the modified file path in runtime variable
            String modifiedFilePath = xmlFileToUse.getAbsolutePath();
            runTimeData.setKey(variableNameTestData.getValue().toString());
            runTimeData.setValue(modifiedFilePath);

            setSuccessMessage("XML file updated successfully and stored the path in  runtime variable : " + variableNameTestData.getValue().toString() + " = " + modifiedFilePath);
            logger.info("Runtime variable set: " + variableNameTestData.getValue().toString() + "=" + modifiedFilePath);


            // Delete temp file if it was downloaded
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                xmlFileToUse.delete();
                logger.info("Temporary downloaded file deleted.");
            }

            return result;

        } catch (Exception e) {
            // Delete the downloaded file in case of an error
            if (filePath.startsWith("http://") || filePath.startsWith("https://") && xmlFileToUse != null) {
                xmlFileToUse.delete();
                logger.warn("Temporary downloaded file deleted due to error.");
            }
            String errorMessage = "Failed to update XML file: " + ExceptionUtils.getStackTrace(e);
            setErrorMessage(errorMessage);
            logger.warn(errorMessage);
            result = Result.FAILED;
            return result;
        }
    }

    // Recursive method to traverse the XML tree and update the tag's value
    private void updateTagValue(Element currentElement, String tagname, String newValue) {
        // Search for elements with the given tagname at the current level
        List<Element> elements = currentElement.getChildren(tagname);

        // If elements are found, update their text content
        if (!elements.isEmpty()) {
            for (Element element : elements) {
                logger.info("Found <" + tagname + ">, updating value to: " + newValue);
                element.setText(newValue);
            }
        }

        // Recursively search through all children of the current element
        List<Element> children = currentElement.getChildren();
        for (Element child : children) {
            updateTagValue(child, tagname, newValue);  // Recursive call on each child element
        }
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String fileName = UUID.randomUUID().toString() + "-" + Paths.get(url.getPath()).getFileName().toString();
        File tempFile = File.createTempFile("downloaded-", fileName);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}