package com.testsigma.addons.utilities;

import com.testsigma.sdk.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class XMLUtility {

    WebDriver driver;
    Logger logger;

    public XMLUtility(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }
    public Document parseXML(String xmlContent) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
    }

    public Document parseXML(File file) throws Exception {
        if (file == null || !file.exists() || file.length() == 0) {
            logger.warn("Invalid file: " + (file == null ? "null" : file.getAbsolutePath()));
            throw new Exception("Invalid file: " + (file == null ? "null" : file.getAbsolutePath()));
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Enable namespace awareness
        DocumentBuilder builder = factory.newDocumentBuilder();

        logger.info("Parsing XML file: " + file.getAbsolutePath());
        Document document = builder.parse(file);

        document.getDocumentElement().normalize(); // Normalize the document
        logger.info("Root element: " + document.getDocumentElement().getNodeName());
        return document;
    }


    public void removeNodesByXPath(Document doc, String xpathExpression) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression xPathExpr = xpath.compile(xpathExpression);
        NodeList nodes = (NodeList) xPathExpr.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node != null && node.getParentNode() != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }


    public File urlToFileConverter(String fileName, String url) throws IOException {
        if (url.startsWith("https://")) {
            logger.info("Given is s3 url ...File name:" + fileName);
            URL urlObject = new URL(url);
            File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
            FileUtils.copyURLToFile(urlObject,tempFile);
            logger.info("Temp file created with name for s3 file" + tempFile.getName() + " at path " + tempFile.getAbsolutePath());
            return tempFile;
        } else {
            logger.info("Given is local file path..");
            return new File(url);
        }
    }
}
