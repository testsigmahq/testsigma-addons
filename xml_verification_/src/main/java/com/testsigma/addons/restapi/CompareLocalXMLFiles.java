package com.testsigma.addons.restapi;

import com.testsigma.sdk.*;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Data
@Action(actionText = "xml: Verify if local files with filepath filepath1 and filepath filepath2 are equal while ignoring specific XPaths X-Paths",
        description = "Verifies if two XML files are structurally and content-wise equal, reporting differences with line numbers. " +
                "Allows ignoring specific parts of the documents based on their XPath (separate by comma). " +
                "Example ignored XPaths: /Xpath1,/Xpath2",
        applicationType = ApplicationType.REST_API)
public class CompareLocalXMLFiles extends WebAction {

    @TestData(reference = "filepath1")
    private com.testsigma.sdk.TestData testData1;

    @TestData(reference = "filepath2")
    private com.testsigma.sdk.TestData testData2;

    @TestData(reference = "X-Paths")
    private com.testsigma.sdk.TestData testData3;

    private static final String LINE_NUMBER_KEY = "lineNumber";
    @Override
    protected Result execute() throws NoSuchElementException {
        logger.info("initiating execution");
        Result result;
        File baseFile = null;
        File actualFile = null;

        try {
            // 1. Get File Paths and Ignored XPaths
            String filePath1 = testData1.getValue().toString();
            String filePath2 = testData2.getValue().toString();
            String xPathsToIgnoreRaw = testData3.getValue().toString();

            logger.info("Base file path/URL: " + filePath1);
            logger.info("Actual file path/URL: " + filePath2);
            logger.info("Raw XPaths to ignore: '" + xPathsToIgnoreRaw + "'");

            // Prepare the set of ignored paths for efficient lookup
            Set<String> ignoredPathsSet = new HashSet<>();
            if (xPathsToIgnoreRaw != null && !xPathsToIgnoreRaw.trim().isEmpty()) {
                String[] xpathArray = xPathsToIgnoreRaw.split(",");
                for (String xpath : xpathArray) {
                    String trimmedPath = xpath.trim();
                    if (!trimmedPath.isEmpty()) {
                        ignoredPathsSet.add(trimmedPath);
                        logger.info("Adding ignored XPath prefix: " + trimmedPath);
                    }
                }
            } else {
                logger.info("No XPaths specified to ignore.");
            }


            // 2. Convert URLs/Paths to Temporary Files
            baseFile = File.createTempFile("baseXml_", ".xml");
            actualFile = File.createTempFile("actualXml_", ".xml");
            logger.info("Created temporary base file: " + baseFile.getAbsolutePath());
            logger.info("Created temporary actual file: " + actualFile.getAbsolutePath());

            // Use the utility to handle potential URLs or local paths
            baseFile = urlToFileConverter(filePath1);
            actualFile = urlToFileConverter(filePath2);
            logger.info("Prepared base & actual file content.");

            // Ensure temp files are deleted on exit (redundant if urlToFileConverter does it, but safe)
            baseFile.deleteOnExit();
            actualFile.deleteOnExit();

            // 3. Parse XML Files with Line Number Tracking
            logger.info("Parsing base XML file...");
            Document doc1 = parseXMLWithLineNumbers(baseFile);
            logger.info("Parsing actual XML file...");
            Document doc2 = parseXMLWithLineNumbers(actualFile);
            logger.info("Successfully parsed both XML files with line number tracking.");


            // 4. Find Differences, Respecting Ignored Paths
            logger.info("Starting XML comparison...");
            List<String> differences = findDifferencesWithIgnore(
                    doc1.getDocumentElement(),
                    doc2.getDocumentElement(),
                    baseFile.getName() + " (Base)",
                    actualFile.getName() + " (Actual)",
                    ignoredPathsSet
            );
            logger.info("Comparison finished.");


            // 5. Report Results
            if (differences.isEmpty()) {
                setSuccessMessage(String.format("<b>The given XML files are equal</b> (considering ignored XPaths).",
                        filePath1, filePath2));
                result = Result.SUCCESS;
            } else {
                // Define the character limit
                final int MAX_DETAILS_LENGTH = 350;

                // Build the difference message
                StringBuilder diffDetails = new StringBuilder();
                diffDetails.append(String.format("Found <b>%d difference(s)</b> between given xml files:\n",
                        differences.size()));

                for (int i = 0; i < differences.size(); i++) {
                    // Append the current difference detail
                    diffDetails.append(String.format("%d. %s\n", i + 1, differences.get(i)));

                    // Check if the total length now exceeds the limit after adding the latest difference
                    if (diffDetails.length() > MAX_DETAILS_LENGTH) {
                        diffDetails.append("(see Addon NLP Logs for more differences)\n");
                        break; // Stop adding more differences
                    }
                }

                logger.debug("XML Comparison Failed. Differences:\n" + diffDetails.toString());
                setErrorMessage(diffDetails.toString());
                result = Result.FAILED;
            }

        } catch (Exception e) {
            logger.debug("Error during XML comparison: " + e.getMessage() + e);
            String errorMessage = "An error occurred during XML comparison: " + ExceptionUtils.getRootCauseMessage(e);
            if (e instanceof SAXParseException) {
                errorMessage = String.format("XML Parsing Error in file around Line %d: %s",
                        ((SAXParseException) e).getLineNumber(), e.getMessage());
            } else if (e instanceof IOException) {
                errorMessage = "Error accessing or reading XML file: " + e.getMessage();
            }
            setErrorMessage(errorMessage);
            logger.debug(ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;

        } finally {
            if (baseFile != null && baseFile.exists()) { baseFile.delete(); }
            if (actualFile != null && actualFile.exists()) { actualFile.delete(); }
        }
        return result;
    }

    public File urlToFileConverter(String url) throws IOException, Exception {
        logger.info("url: " + url);
        if (url.startsWith("https://")) {
            logger.info("Given is s3 url ...File name:");
            URL urlObject = new URL(url);
            File tempFile = File.createTempFile("tempXMLFile_", "." + "xml");
            logger.info("created temporary file: " + tempFile.getAbsolutePath());
            FileUtils.copyURLToFile(urlObject, tempFile);
            logger.info("Temp file created with name for s3 file" + tempFile.getName()
                    + " at path " + tempFile.getAbsolutePath());
            return tempFile;
        } else {
            // Ignoring " and ' from file path...
            url = url.replaceAll("[\"']", "");
            logger.info("Given is local file path.. Creating temp file");
            File tempFile = File.createTempFile("tempLocalFile_", new File(url).getName());
            FileUtils.copyFile(new File(url), tempFile);
            logger.info("Temp file created for local file with name " + tempFile.getName()
                    + " at path " + tempFile.getAbsolutePath());
            return tempFile;
        }
    }

    public Document parseXMLWithLineNumbers(File file) throws ParserConfigurationException, SAXException, IOException {
        logger.info("Parsing XML with line numbers: " + file.getAbsolutePath());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Important for accurate parsing and XPath
        DocumentBuilder builder = factory.newDocumentBuilder();

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        SAXParser saxParser = saxFactory.newSAXParser();

        DomBuilderWithLineNumbers handler = new DomBuilderWithLineNumbers(builder, logger); // Pass logger

        try {
            saxParser.parse(file, handler);
        } catch (SAXParseException e) {
            logger.debug(String.format("SAX Parsing Error at Line %d, Column %d: %s",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
            throw e; // Re-throw after logging context
        } catch (SAXException | IOException e) {
            logger.debug("Error parsing XML with line numbers: " + e.getMessage() + e);
            throw e;
        }

        Document doc = handler.getDocument();
        if (doc == null || doc.getDocumentElement() == null) {
            logger.debug("Failed to build DOM from XML file: " + file.getName());
            throw new SAXException("Failed to build DOM, possibly due to empty or malformed XML.");
        }
        logger.info("Successfully parsed XML with line numbers: " + file.getName());
        return doc;
    }

    // ---  Inner Class: SAX Handler to Build DOM with Line Numbers ---
    private static class DomBuilderWithLineNumbers extends DefaultHandler {
        private Document doc;
        private final DocumentBuilder docBuilder;
        private Locator locator;
        private final Stack<Element> elementStack = new Stack<>();
        private final StringBuilder textBuffer = new StringBuilder();
        private final Logger logger; // Added logger

        public DomBuilderWithLineNumbers(DocumentBuilder builder, Logger logger) {
            this.docBuilder = builder;
            this.logger = logger; // Store logger
        }

        public Document getDocument() {
            return doc;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startDocument() throws SAXException {
            doc = docBuilder.newDocument();
            logger.debug("DOM Builder: Starting document");
        }

        @Override
        public void endDocument() throws SAXException {
            logger.debug("DOM Builder: Finished document");
            // Check if anything was added - helps diagnose empty XML issues
            if (doc != null && !doc.hasChildNodes()) {
                logger.warn("DOM Builder: Document created but appears empty.");
            }
        }


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            flushTextBuffer(); // Add pending text before new element

            //logger.debug("DOM Builder: Start Element: " + qName + (uri != null && !uri.isEmpty() ? " URI: " + uri : ""));
            Element element = doc.createElementNS(uri, qName); // Namespace aware

            if (locator != null) {
                int line = locator.getLineNumber();
                element.setUserData(LINE_NUMBER_KEY, line, null);
                //logger.debug("DOM Builder: Attaching Line: " + line + " to Element: " + qName);
            } else {
                logger.warn("DOM Builder: Locator not available for element " + qName + ", line numbers will be missing.");
            }


            for (int i = 0; i < attributes.getLength(); i++) {
                element.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
            }

            if (elementStack.isEmpty()) {
                doc.appendChild(element);
            } else {
                elementStack.peek().appendChild(element);
            }
            elementStack.push(element);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            flushTextBuffer(); // Add any final text for this element
            //logger.debug("DOM Builder: End Element: " + qName);
            if (!elementStack.isEmpty()) {
                elementStack.pop();
            } else {
                logger.warn("DOM Builder: Element stack empty on endElement for " + qName);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            textBuffer.append(ch, start, length);
            // logger.debug("DOM Builder: Characters received: length " + length);
        }

        private void flushTextBuffer() {
            if (textBuffer.length() > 0 && !elementStack.isEmpty()) {
                Element parent = elementStack.peek();
                //logger.debug("DOM Builder: Flushing text buffer: '" + textBuffer.toString().trim() + "' into element: " + parent.getNodeName());
                Text textNode = doc.createTextNode(textBuffer.toString());
                // Don't usually add line numbers to text nodes directly, use parent's
                parent.appendChild(textNode);
                textBuffer.setLength(0); // Clear buffer
            } else if (textBuffer.length() > 0 && elementStack.isEmpty()) {
                logger.warn("DOM Builder: Text found outside of root element: '" + textBuffer.toString().trim() + "' - Ignoring.");
                textBuffer.setLength(0); // Discard text outside root
            }
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            flushTextBuffer();
            ProcessingInstruction pi = doc.createProcessingInstruction(target, data);
            if (locator != null) pi.setUserData(LINE_NUMBER_KEY, locator.getLineNumber(), null);

            if (elementStack.isEmpty()) {
                doc.appendChild(pi); // Before root
            } else {
                elementStack.peek().appendChild(pi); // Inside element
            }
        }

        // Error Handling within SAX Parser
        @Override
        public void warning(SAXParseException e) throws SAXException {
            logger.warn(String.format("SAX Parser Warning: Line %d, Col %d: %s",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            logger.debug(String.format("SAX Parser Error: Line %d, Col %d: %s",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
            throw e; // Throw to potentially stop parsing on error
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            logger.debug(String.format("SAX Parser Fatal Error: Line %d, Col %d: %s",
                    e.getLineNumber(), e.getColumnNumber(), e.getMessage()));
            throw e; // Definitely stop parsing
        }
    }

    // ---  Core Difference Finding Logic ---
    public List<String> findDifferencesWithIgnore(Node node1, Node node2, String file1Name, String file2Name, Set<String> ignoredXPathPrefixes) {
        List<String> differences = new ArrayList<>();
        String currentXPath = getXPath(node1 != null ? node1 : node2); // Get XPath based on available node

        // *** Check if the current path should be ignored ***
        if (isPathIgnored(currentXPath, ignoredXPathPrefixes)) {
            logger.debug("Ignoring path: " + currentXPath);
            return differences; // Don't compare this node or its children
        }

        // --- Handling null nodes (Structure difference) ---
        if (node1 == null && node2 != null) {
            // Check if the non-null node (node2) would have been ignored
            if (!isPathIgnored(getXPath(node2), ignoredXPathPrefixes)) {
                differences.add(String.format("Structural Difference: Node [%s] exists in %s but is missing in %s at the corresponding position (Path: %s, Line in %s: %d)",
                        node2.getNodeName(), file2Name, file1Name, getXPath(node2), file2Name, getLineNumber(node2)));
            }
            return differences;
        }
        if (node1 != null && node2 == null) {
            // Check if the non-null node (node1) would have been ignored
            if (!isPathIgnored(getXPath(node1), ignoredXPathPrefixes)) {
                differences.add(String.format("Structural Difference: Node [%s] exists in %s but is missing in %s at the corresponding position (Path: %s, Line in %s: %d)",
                        node1.getNodeName(), file1Name, file2Name, getXPath(node1), file1Name, getLineNumber(node1)));
            }
            return differences;
        }
        if (node1 == null && node2 == null) {
            return differences; // Both null, no difference here
        }

        // --- Filter insignificant nodes (e.g., comments, whitespace text) ---
        // Important: Do this *after* null checks but *before* comparing content
        if (isIgnorableNode(node1) && isIgnorableNode(node2)) {
            return differences; // Both ignorable, treat as no difference
        }
        // Handle cases where one is ignorable and the other isn't (could be difference or just formatting)
        // This logic depends on how strictly you want to compare. For now, we proceed.

        // --- Compare Node Names ---
        if (!Objects.equals(node1.getNodeName(), node2.getNodeName())) {
            differences.add(String.format("Node Name Difference: Mismatch at Path: %s. File1 (%s): '%s' (Line: %d) vs File2 (%s): '%s' (Line: %d)",
                    currentXPath, file1Name, node1.getNodeName(), getLineNumber(node1), file2Name, node2.getNodeName(), getLineNumber(node2)));
            return differences; // Stop comparing this branch
        }

        // --- Compare Node Types ---
        if (node1.getNodeType() != node2.getNodeType()) {
            differences.add(String.format("Node Type Difference: Mismatch at Path: %s. File1 (%s): Type %d (Line: %d) vs File2 (%s): Type %d (Line: %d)",
                    currentXPath, file1Name, node1.getNodeType(), getLineNumber(node1), file2Name, node2.getNodeType(), getLineNumber(node2)));
            return differences; // Stop comparing this branch
        }

        // --- Compare Attributes (if element node) ---
        if (node1.getNodeType() == Node.ELEMENT_NODE) {
            compareAttributes(node1, node2, file1Name, file2Name, currentXPath, differences, ignoredXPathPrefixes); // Pass ignored paths
        }

        // --- Compare Text Content (if text/cdata node) ---
        if (node1.getNodeType() == Node.TEXT_NODE || node1.getNodeType() == Node.CDATA_SECTION_NODE) {
            // Trim whitespace? Decide based on requirements. Often useful.
            // String val1 = node1.getNodeValue() != null ? node1.getNodeValue().trim() : "";
            // String val2 = node2.getNodeValue() != null ? node2.getNodeValue().trim() : "";
            String val1 = node1.getNodeValue() != null ? node1.getNodeValue() : "";
            String val2 = node2.getNodeValue() != null ? node2.getNodeValue() : "";

            // Only report difference if the text itself isn't just whitespace
            if (!val1.trim().isEmpty() || !val2.trim().isEmpty()) {
                if (!Objects.equals(val1, val2)) {
                    // Report difference based on the parent element's context if possible
                    Node parent1 = node1.getParentNode();
                    Node parent2 = node2.getParentNode();
                    String parentPath = getXPath(parent1 != null ? parent1 : node1); // Fallback to node's path
                    int line1 = getLineNumber(parent1 != null ? parent1 : node1); // Use parent's line number
                    int line2 = getLineNumber(parent2 != null ? parent2 : node2);

                    differences.add(String.format("Text Content Difference: Path: <b>%s</b>. (Line: <b>%d</b>) vs  (Line: <b>%d</b>)",
                            parentPath, line1, line2));
                }
            }
        }

        // --- Recursively check children ---
        compareChildNodes(node1, node2, file1Name, file2Name, differences, ignoredXPathPrefixes); // Pass ignored paths

        return differences;
    }

    private boolean isPathIgnored(String currentXPath, Set<String> ignoredXPathPrefixes) {
        if (currentXPath == null || currentXPath.equals("/") || ignoredXPathPrefixes == null || ignoredXPathPrefixes.isEmpty()) {
            return false;
        }
        for (String prefix : ignoredXPathPrefixes) {
            if (prefix != null && !prefix.trim().isEmpty()) {
                // Check if the current path starts with the ignored prefix.
                // Add ending '/' to prefix ensure "/root/a" doesn't ignore "/root/ab"
                // Or ensure the ignored path is an exact match.
                String trimmedPrefix = prefix.trim();
                if (currentXPath.equals(trimmedPrefix) || currentXPath.startsWith(trimmedPrefix + "/")) {
                    return true;
                }
                // Add more sophisticated matching logic here if needed (e.g., regex, XPath engine)
            }
        }
        return false;
    }

    // --- NEW/MODIFIED: Child Node Comparison (passes ignored paths down) ---
    private void compareChildNodes(Node node1, Node node2, String file1Name, String file2Name, List<String> differences, Set<String> ignoredXPathPrefixes) {
        NodeList children1 = node1.getChildNodes();
        NodeList children2 = node2.getChildNodes();

        // Normalize node lists: Filter out insignificant nodes BEFORE comparison
        List<Node> list1 = normalizeNodeList(children1);
        List<Node> list2 = normalizeNodeList(children2);

        int len1 = list1.size();
        int len2 = list2.size();
        int maxLen = Math.max(len1, len2);

        for (int i = 0; i < maxLen; i++) {
            Node child1 = (i < len1) ? list1.get(i) : null;
            Node child2 = (i < len2) ? list2.get(i) : null;

            // Recursive call, passing the ignored paths down
            // The recursive call itself will handle null checks and ignoring based on path
            differences.addAll(findDifferencesWithIgnore(child1, child2, file1Name, file2Name, ignoredXPathPrefixes));
        }
    }

    // ---  XPath Generation ---
    public String getXPath(Node node) {
        // Use StringBuilder for efficiency
        StringBuilder pathBuilder = new StringBuilder();
        return buildXPath(node, pathBuilder).toString();
    }

    private StringBuilder buildXPath(Node node, StringBuilder pathBuilder) {
        if (node == null) {
            return pathBuilder.append("null"); // Or handle differently
        }

        Node parent = node.getParentNode();

        // Base cases
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return pathBuilder.append("/");
        }
        if (parent == null || parent.getNodeType() == Node.DOCUMENT_NODE) {
            // Reached the top or near the top
            pathBuilder.append("/");
            appendNodeXPathStep(node, pathBuilder);
            return pathBuilder;
        }


        // Recursive call for parent path first
        buildXPath(parent, pathBuilder);

        // Ensure trailing slash if not already present (should be added by parent step)
        if (pathBuilder.length() > 0 && pathBuilder.charAt(pathBuilder.length() - 1) != '/') {
            pathBuilder.append("/");
        }

        // Append current node step
        appendNodeXPathStep(node, pathBuilder);

        return pathBuilder;
    }
    // ---  Helper to determine if a node is insignificant for comparison ---
    private boolean isIgnorableNode(Node node) {
        if (node == null) return true; // Treat null as ignorable in this context
        short type = node.getNodeType();
        // Ignore comments and processing instructions by default
        if (type == Node.COMMENT_NODE || type == Node.PROCESSING_INSTRUCTION_NODE) {
            return true;
        }
        // Ignore whitespace-only text nodes
        if (type == Node.TEXT_NODE && node.getNodeValue().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    // Appends a single step like "nodename[index]" or "text()[index]"
    private void appendNodeXPathStep(Node node, StringBuilder pathBuilder) {
        String nodeName = getNodeNameForXPath(node);
        pathBuilder.append(nodeName);

        // Add index [n] only if it's ambiguous (multiple siblings with same name/type)
        int index = getNodeIndex(node);
        // Optimization: only add [index] if it's > 1 OR if needed to distinguish
        // For simplicity and consistency with previous code, we'll always add it for now.
        // More advanced: Check if siblings exist with the same name/type.
        pathBuilder.append("[").append(index).append("]");
    }

    // Helper to get a simple name for XPath (handles Text/CDATA nodes)
    private String getNodeNameForXPath(Node node) {
        if (node == null) return "null-node";
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
                return "text()";
            case Node.CDATA_SECTION_NODE:
                return "text()"; // Treat CDATA as text for path
            case Node.COMMENT_NODE:
                return "comment()";
            case Node.PROCESSING_INSTRUCTION_NODE:
                return "processing-instruction('" + node.getNodeName() + "')";
            case Node.ATTRIBUTE_NODE:
                return "@" + node.getNodeName();
            case Node.ELEMENT_NODE:
                return node.getNodeName();
            default:
                return node.getNodeName(); // Or handle other types
        }
    }

    // Calculates the 1-based index among siblings of the *same significant type and name*
    private int getNodeIndex(Node node) {
        if (node == null || node.getParentNode() == null) return 1;

        Node parent = node.getParentNode();
        NodeList siblings = parent.getChildNodes();
        int index = 1;
        short nodeType = node.getNodeType();
        String nodeName = node.getNodeName(); // Relevant for Elements, PIs

        for (int i = 0; i < siblings.getLength(); i++) {
            Node sibling = siblings.item(i);

            // --- Match based on Node Type and potentially Name ---
            boolean typesMatch = sibling.getNodeType() == nodeType;
            boolean namesMatch = true; // Assume true unless type requires name check

            if (typesMatch) {
                // For Elements and PIs, names must also match
                if (nodeType == Node.ELEMENT_NODE || nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
                    namesMatch = Objects.equals(sibling.getNodeName(), nodeName);
                }
                // For Text/CDATA, treat them as equivalent types for indexing
                else if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                    typesMatch = (sibling.getNodeType() == Node.TEXT_NODE || sibling.getNodeType() == Node.CDATA_SECTION_NODE);
                    // Optional: only count non-empty text nodes if normalizing elsewhere
                    if (typesMatch && isIgnorableNode(sibling)) continue; // Skip ignorable siblings
                }
                // For comments, just match type
                else if (nodeType == Node.COMMENT_NODE) {
                    // Type already matched
                }
                // If other types need specific name matching, add here
            }

            // If it's a comparable sibling
            if (typesMatch && namesMatch) {
                if (sibling == node) {
                    return index; // Found the node
                }
                index++; // Increment index for this type/name
            }
        }
        // Fallback - should technically be found
        logger.warn("Could not definitively determine node index for: " + getXPath(node) + ". Returning calculated index: " + index);
        return index;
    }

    // ---  Retrieves the line number stored in user data ---
    public int getLineNumber(Node node) {
        if (node == null) return -1;
        Object lineNumber = node.getUserData(LINE_NUMBER_KEY);
        // Check type before casting
        return (lineNumber instanceof Integer) ? (Integer) lineNumber : -1;
    }

    // --- NEW/MODIFIED: Attribute Comparison (checks ignored paths) ---
    private void compareAttributes(Node node1, Node node2, String file1Name, String file2Name, String elementXPath, List<String> differences, Set<String> ignoredXPathPrefixes) {
        NamedNodeMap attrs1 = node1.getAttributes();
        NamedNodeMap attrs2 = node2.getAttributes();

        Map<String, String> attrMap1 = mapAttributes(attrs1);
        Map<String, String> attrMap2 = mapAttributes(attrs2);

        int line1 = getLineNumber(node1);
        int line2 = getLineNumber(node2); // Line number of the element

        // Check for attributes missing in target or different values
        for (Map.Entry<String, String> entry : attrMap1.entrySet()) {
            String attrName = entry.getKey();
            String attrXPath = elementXPath + "/@" + attrName; // Construct XPath for the attribute

            // *** Check if attribute path is ignored ***
            if (isPathIgnored(attrXPath, ignoredXPathPrefixes)) {
                logger.debug("Ignoring attribute path: " + attrXPath);
                continue;
            }

            String value1 = entry.getValue();
            if (!attrMap2.containsKey(attrName)) {
                differences.add(String.format("Attribute Difference: Attribute '%s' missing in File2 (%s). Exists in File1 (%s) at Path: %s (Element Line: %d)",
                        attrName, file2Name, file1Name, attrXPath, line1));
            } else {
                String value2 = attrMap2.get(attrName);
                if (!Objects.equals(value1, value2)) {
                    differences.add(String.format("Attribute Value Difference: Path: %s. File1 (%s): '%s' (Line: ~%d) vs File2 (%s): '%s' (Line: ~%d)",
                            attrXPath, file1Name, value1, line1, file2Name, value2, line2));
                }
            }
        }

        // Check for attributes present only in target
        for (Map.Entry<String, String> entry : attrMap2.entrySet()) {
            String attrName = entry.getKey();
            String attrXPath = elementXPath + "/@" + attrName; // Construct XPath for the attribute

            // *** Check if attribute path is ignored ***
            if (isPathIgnored(attrXPath, ignoredXPathPrefixes)) {
                // Already checked during the first loop if it existed in both
                // Only need to log ignore if it's *only* in the second file
                if (!attrMap1.containsKey(attrName)) {
                    logger.debug("Ignoring attribute path (present only in File2): " + attrXPath);
                }
                continue;
            }

            if (!attrMap1.containsKey(attrName)) {
                differences.add(String.format("Attribute Difference: Attribute '%s' missing in File1 (%s). Exists in File2 (%s) at Path: %s (Element Line: %d)",
                        attrName, file1Name, file2Name, attrXPath, line2));
            }
        }
    }

    // Helper to convert NamedNodeMap to a Map, ignoring xmlns attributes
    private Map<String, String> mapAttributes(NamedNodeMap attrs) {
        Map<String, String> attrMap = new HashMap<>();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String name = attr.getNodeName();
                if (!name.startsWith("xmlns:") && !name.equals("xmlns")) {
                    attrMap.put(name, attr.getNodeValue());
                }
            }
        }
        return attrMap;
    }

    // ---  Helper to filter node list (Removes comments, PIs, whitespace text) ---
    private List<Node> normalizeNodeList(NodeList nodeList) {
        List<Node> normalized = new ArrayList<>();
        if (nodeList == null) return normalized;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!isIgnorableNode(node)) {
                normalized.add(node);
            }
        }
        return normalized;
    }
}