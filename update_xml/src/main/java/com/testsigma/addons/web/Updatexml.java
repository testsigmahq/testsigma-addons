package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
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
@Action(actionText = "Update the xml file absolutepath on the element value tagname by index indexvalue with attributename and attributevalue",
description = "updating the xml value using element tagname,index with atributename and value",
applicationType = ApplicationType.WEB,
useCustomScreenshot = false)
public class Updatexml extends WebAction {

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
			File inputFile = new File(testData1.getValue().toString());
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(inputFile);
			Element elementvalue = (Element) doc.getElementsByTagName(testData2.getValue().toString()).item(Integer.parseInt(testData3.getValue().toString()));

			elementvalue.setAttribute(testData4.getValue().toString(),testData5.getValue().toString());

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult resultfile = new StreamResult(inputFile);
			transformer.transform(source, resultfile);
		}catch (Exception e) {
			String errorMessage = ExceptionUtils.getStackTrace(e);
			result = com.testsigma.sdk.Result.FAILED;
			setErrorMessage(errorMessage);
			logger.warn(errorMessage);	
		} 
		setSuccessMessage("XML file updated successfully.");
		return result;
	}
}