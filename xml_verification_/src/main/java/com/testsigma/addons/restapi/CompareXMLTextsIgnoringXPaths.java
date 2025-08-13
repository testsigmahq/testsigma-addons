package com.testsigma.addons.restapi;

import com.testsigma.addons.utilities.XMLUtility;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import java.util.NoSuchElementException;

@Data
@Action(actionText = "Xml: Verify if xmlText text1 is equal to xmlText text2 while ignoring specific XPaths X-paths",
        description = "Verifies if texts with given xml content are equal while ignoring given xPaths" +
                " (separate xPaths by comma) e.g: xpath1, xpath2",
        applicationType = ApplicationType.REST_API)
public class CompareXMLTextsIgnoringXPaths extends RestApiAction {

    @TestData(reference = "text1")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "text2")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "X-paths")
    private com.testsigma.sdk.TestData testData3;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            String filePath1 = testData1.getValue().toString();
            String filePath2 = testData2.getValue().toString();
            String xPathsToIgnore = testData3.getValue().toString();
            logger.info("xPathsToIgnore: " + xPathsToIgnore);
            Document doc1 = parseXML(filePath1);
            Document doc2 = parseXML(filePath2);
            logger.info("Text1 after parsing into xml " + doc1);

            // Remove nodes based on XPath expressions
            String[] xpathArray = xPathsToIgnore.split(",");
            for (String xpathExpression : xpathArray) {
                removeNodesByXPath(doc1, xpathExpression.trim());
                removeNodesByXPath(doc2, xpathExpression.trim());
            }

            logger.info("Text1 after ignoring xPaths " + doc1);
            logger.info("Text2 after parsing xPaths " + doc2);
            // Compare the modified documents
            boolean isEqual = doc1.isEqualNode(doc2);
            if (isEqual) {
                setSuccessMessage("the given texts are equal");
                result = Result.SUCCESS;
            } else {
                setErrorMessage("the given texts are not equal");
                result = Result.FAILED;
            }
            return result;

        } catch (Exception e) {
            setErrorMessage("Error occurred while comparing: " + ExceptionUtils.getStackTrace(e));
            logger.info(ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }

    public Document parseXML(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
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

}
