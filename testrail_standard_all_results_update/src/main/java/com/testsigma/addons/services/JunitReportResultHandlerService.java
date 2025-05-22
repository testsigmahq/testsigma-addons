package com.testsigma.addons.services;

import org.springframework.stereotype.Service;
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

@Service
public class JunitReportResultHandlerService {

  private final LoggerService logger = new LoggerService();

  public void markStoppedResultAsBlocked() {
    logger.printLogs("Marking stopped result as blocked");

    String junitFilePath = GlobalStateService.getInstance().getJunitFilePath();
    File junitFile = new File(junitFilePath);

    if (!junitFile.exists()) {
      logger.printLogs("JUnit file not found at: " + junitFilePath);
      return;
    }

    try {
      // Parse the XML file
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(junitFile);

      // Get all <testcase> elements
      NodeList testCaseNodes = document.getElementsByTagName("testcase");

      for (int i = 0; i < testCaseNodes.getLength(); i++) {
        Element testCase = (Element) testCaseNodes.item(i);
        String result = testCase.getAttribute("result");

        // Update result="STOPPED" to result="BLOCKED"
        if ("STOPPED".equalsIgnoreCase(result)) {
          testCase.setAttribute("result", "BLOCKED");
        }
      }

      // Save the updated XML back to the file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(junitFile);
      transformer.transform(source, result);

      logger.printLogs("Completed marking stopped result as blocked");
    } catch (Exception e) {
      logger.printLogs("Error while processing JUnit file: " + e.getMessage());
    }
  }

}
