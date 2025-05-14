package com.testdemo.testsigma.addons.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.openqa.selenium.WebDriver;
import com.testsigma.sdk.Logger;
import org.apache.commons.io.FileUtils;


public class PdfUtils {
    WebDriver driver;
    Logger logger;

    public PdfUtils(WebDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    public File urlToFileConverter(String url) {
        try {
            logger.info("url " + url);
            logger.info(String.valueOf(url.startsWith("https://") || url.startsWith("http://")));
            if (url.startsWith("https://") || url.startsWith("http://")) {
                // file name use the current time stamp
                logger.info("Given is s3 url");
                URL urlObject = new URL(url);
                File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".pdf");
                tempFile.deleteOnExit();
                FileUtils.copyURLToFile(urlObject, tempFile);
                logger.info("Temp file created with name for s3 file" + tempFile.getName()
                        + " at path " + tempFile.getAbsolutePath());
                return tempFile;
            } else {
                logger.info("Given is local file path..");
                return new File(url);
            }
        } catch (Exception e) {
            logger.info("Error while accessing: " + url);
            logger.debug(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Unable to access the given pdf, please check the given input" + e.getMessage());
        }
    }


    public boolean validateContentTypeForSearchText(String searchText, String filePath, String expectedFormat) {
        try {
            File pdfFile = urlToFileConverter(filePath);
            PDDocument document = Loader.loadPDF(pdfFile);

            // using a custom text stripper to analyze text formatting
            PdfUtils.FormattingTextStripper formattingStripper = new FormattingTextStripper();
            formattingStripper.setSearchText(searchText);
            formattingStripper.setExpectedFormat(expectedFormat);
            formattingStripper.setSortByPosition(true);
            formattingStripper.getText(document);

            boolean isFormatted = formattingStripper.isTextFormattedAsExpected();

            document.close();
            return isFormatted;

        } catch (IOException e) {
            throw new RuntimeException("Error loading PDF document: " + e.getMessage());
        }
    }

    private static class FormattingTextStripper extends PDFTextStripper {
        private String searchText;
        private String expectedFormat;
        private boolean foundFormattedText = false;
        private StringBuilder currentTextBuilder = new StringBuilder();
        private boolean currentIsBold = false;
        private boolean currentIsItalic = false;
        public FormattingTextStripper() throws IOException {
            super();
        }

        public void setSearchText(String searchText) {
            this.searchText = searchText;
        }

        public void setExpectedFormat(String expectedFormat) {
            this.expectedFormat = expectedFormat.toLowerCase();
        }

        public boolean isTextFormattedAsExpected() {
            return foundFormattedText;
        }

        @Override
        protected void processTextPosition(TextPosition text) {
            PDFont font = text.getFont();
            String fontName = font.getName().toLowerCase();

            // Check format attributes based on font name and rendering mode
            boolean isBold = fontName.contains("bold") || fontName.contains("heavy") || fontName.contains("black");
            boolean isItalic = fontName.contains("italic") || fontName.contains("oblique");

            // Check for changes in formatting
            if (isBold != currentIsBold || isItalic != currentIsItalic) {
                // Format changed, check if previous text matches our search
                checkCurrentTextForMatch();

                // Reset for new format
                currentTextBuilder.setLength(0);
                currentIsBold = isBold;
                currentIsItalic = isItalic;
            }

            // Add current character to the builder
            currentTextBuilder.append(text.getUnicode());

            // Call the parent method to properly build the text
            super.processTextPosition(text);
        }

        @Override
        protected void endPage(PDPage page) throws IOException {
            // Check the final text block on the page
            checkCurrentTextForMatch();
            currentTextBuilder.setLength(0);

            super.endPage(page);
        }

        private void checkCurrentTextForMatch() {
            String currentText = currentTextBuilder.toString().trim();

            // If the text contains our search string
            if (currentText.contains(searchText)) {
                // Extract the exact position of the search text in the current block
                int startIndex = currentText.indexOf(searchText);
                int endIndex = startIndex + searchText.length();

                // Check if the entire search text is within a single formatted block
                // This ensures the whole searchText has the same formatting
                if (startIndex >= 0 && endIndex <= currentText.length()) {
                    boolean hasExpectedFormat = false;

                    switch (expectedFormat) {
                        case "bold":
                            hasExpectedFormat = currentIsBold;
                            break;
                        case "italic":
                            hasExpectedFormat = currentIsItalic;
                            break;
                    }

                    if (hasExpectedFormat) {
                        foundFormattedText = true;
                    }
                }
            }
        }
    }

}
