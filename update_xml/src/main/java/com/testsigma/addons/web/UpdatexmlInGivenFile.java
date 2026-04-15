package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Data
@Action(actionText = "Update the given xml file absolutepath on the element value tagname by index indexvalue with attributename and attributevalue",
		description = "updating the xml value using element tagname,index with atributename and value",
		applicationType = ApplicationType.WEB,
		useCustomScreenshot = false)
public class UpdatexmlInGivenFile extends WebAction {

	@TestData(reference = "absolutepath")
	private com.testsigma.sdk.TestData testData1;
	@TestData(reference = "tagname")
	private com.testsigma.sdk.TestData testData2;
	@TestData(reference = "indexvalue")
	private com.testsigma.sdk.TestData testData3;
	@TestData(reference = "attributename")
	private com.testsigma.sdk.TestData testData4;
	@TestData(reference = "attributevalue")
	private com.testsigma.sdk.TestData testData5;


	@Override
	public com.testsigma.sdk.Result execute() throws NoSuchElementException {
		logger.info("Initiating execution");
		com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
		try {
			String filePath = testData1.getValue().toString();
			String tagName = testData2.getValue().toString();
			int index = Integer.parseInt(testData3.getValue().toString());
			String attributeName = testData4.getValue().toString();
			String attributeValue = testData5.getValue().toString();

			logger.info("Parameters - filepath: " + filePath + ", tagname: " + tagName + ", index: " + index + ", attributename: " + attributeName + ", attributevalue: " + attributeValue);

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

			org.w3c.dom.NodeList nodes = doc.getElementsByTagName(tagName);
			logger.info("Total <" + tagName + "> elements found in document: " + nodes.getLength());

			Element elementvalue = (Element) nodes.item(index);
			if (elementvalue == null) {
				logger.warn("No <" + tagName + "> element found at index " + index);
				setErrorMessage("No <" + tagName + "> element found at index " + index + ".");
				return com.testsigma.sdk.Result.FAILED;
			}

			logger.info("Setting attribute '" + attributeName + "' = '" + attributeValue + "' on element <" + tagName + "> at index " + index);
			elementvalue.setAttribute(attributeName, attributeValue);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File tempFile = File.createTempFile("xml_update", ".tmp", inputFile.getParentFile());
			StreamResult resultfile = new StreamResult(tempFile);
			transformer.transform(source, resultfile);
			Files.move(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			logger.info("XML file saved successfully at: " + inputFile.getAbsolutePath());
			setSuccessMessage("XML file updated successfully.");
		}catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);
		}
		return result;
	}
}