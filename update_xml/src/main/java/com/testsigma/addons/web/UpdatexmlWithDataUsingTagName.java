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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(
        actionText = "Update the xml file filepath-or-upload on the element using tag tagName by index index-value with valueToUpdate , store the path in runtime variable variable-name",
        description = "Updating an XML node using tag name, index, and new value",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class UpdatexmlWithDataUsingTagName extends WebAction {

    @TestData(reference = "filepath-or-upload")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "tagName")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "index-value")
    private com.testsigma.sdk.TestData testData3;

    @TestData(reference = "valueToUpdate")
    private com.testsigma.sdk.TestData testData5;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData6;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Starting XML update addon...");

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String filePath = testData1.getValue().toString();
            String tagName = testData2.getValue().toString();
            int indexValue = Integer.parseInt(testData3.getValue().toString());
            String newValue = testData5.getValue().toString();
            String variableName = testData6.getValue().toString();

            File xmlFile = (filePath.startsWith("http://") || filePath.startsWith("https://"))
                    ? downloadFile(filePath)
                    : new File(filePath);

            logger.info("XML file: " + xmlFile.getAbsolutePath());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            NodeList nodes = document.getElementsByTagName(tagName);

            if (indexValue < 0 || indexValue >= nodes.getLength()) {
                throw new IndexOutOfBoundsException(
                        "Index out of range. Total nodes: " + nodes.getLength()
                );
            }

            Node targetNode = nodes.item(indexValue);
            String oldValue = targetNode.getTextContent();

            logger.info("Old value: " + oldValue);
            logger.info("New value: " + newValue);

            targetNode.setTextContent(newValue);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(xmlFile));

            runTimeData.setKey(variableName);
            runTimeData.setValue(xmlFile.getAbsolutePath());

            setSuccessMessage(
                    "Updated tag '" + tagName + "' at index " + indexValue +
                            ". Before: '" + oldValue + "', After: '" + newValue + "'. Path: '" + xmlFile.getAbsolutePath() + "'"
            );

        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            logger.warn(errorMessage);
            setErrorMessage(errorMessage);
            result = com.testsigma.sdk.Result.FAILED;
        }

        return result;
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        String originalName = Paths.get(url.getPath()).getFileName().toString();

        File tempFile = File.createTempFile("xml_download_", "_" + originalName);

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
