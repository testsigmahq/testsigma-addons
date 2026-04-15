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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

@Data
@Action(actionText = "Update the xml filepath using tagname from old_value to new_value with given index",
        description = "Update the xml file path using tagname from old_value to new_value with given index",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)

public class UpdateXmlWithNewData extends WebAction {
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
        Result result = Result.SUCCESS;
        try {
                logger.info("first level");
                File inputFile = new File(testData1.getValue().toString());
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                logger.info("second level");
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(inputFile);
                logger.info("third level");
                // Get the first element by tag name "Currency"
                Element elementValue = (Element) doc.getElementsByTagName(testData2.getValue().toString()).item(Integer.parseInt(testData5.getValue().toString()));
               logger.info("first level"+elementValue);
                // Set the new value (replacing the text content inside the element)
                if (elementValue != null) {
                    elementValue.setTextContent(testData4.getValue().toString());
                } else {
                    logger.info("Element with tag <" + testData2.getValue().toString() + "> not found!");
                }

                // Save the changes to the XML file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult resultFile = new StreamResult(inputFile);
                transformer.transform(source, resultFile);
                setSuccessMessage("XML file updated successfully.");
                return result;
            } catch (Exception e) {
                String errorMessage = ExceptionUtils.getStackTrace(e);
                result = Result.FAILED;
                setErrorMessage(errorMessage);
                logger.warn(errorMessage);
            return result;
            }



        }
    }



