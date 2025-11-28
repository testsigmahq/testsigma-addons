package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "Read xml value from filepath-or-upload for tagName at index index-value, store the result in runtime variable variable-name",
        description = "Reads an XML node value based on tag name and index and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class ReadXmlValueByIndex extends WebAction {

    @TestData(reference = "filepath-or-upload")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "tagName")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "index-value")
    private com.testsigma.sdk.TestData testData3;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData4;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Starting XML read addon...");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String filePath = testData1.getValue().toString();
            String tagName = testData2.getValue().toString();
            int indexValue = Integer.parseInt(testData3.getValue().toString());
            String variableName = testData4.getValue().toString();

            File xmlFile = (filePath.startsWith("http://") || filePath.startsWith("https://"))
                    ? downloadFile(filePath)
                    : new File(filePath);

            logger.info("XML file: " + xmlFile.getAbsolutePath());

            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            NodeList nodes = document.getElementsByTagName(tagName);

            if (nodes == null || nodes.getLength() == 0) {
                throw new Exception("Tag <" + tagName + "> not found in XML.");
            }

            if (indexValue < 0 || indexValue >= nodes.getLength()) {
                throw new IndexOutOfBoundsException(
                        "Index " + indexValue + " out of range. Available: " + nodes.getLength()
                );
            }

            Node targetNode = nodes.item(indexValue);
            String value = targetNode.getTextContent().trim();

            // Store result
            runTimeData.setKey(variableName);
            runTimeData.setValue(value);

            logger.info("Extracted XML value: " + value);

            setSuccessMessage("Successfully read XML value: " + value);

        } catch (Exception e) {
            String error = ExceptionUtils.getStackTrace(e);
            logger.warn(error);
            setErrorMessage(error);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String originalName = Paths.get(url.getPath()).getFileName().toString();
        File tempFile = File.createTempFile("xml_read_", "_" + originalName);

        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int len;

            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return tempFile;
    }
}
