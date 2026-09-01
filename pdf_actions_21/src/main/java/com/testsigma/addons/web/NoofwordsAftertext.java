package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.addons.util.PDFUtils;
import com.testsigma.addons.util.PdfExtractionUtil;
import com.testsigma.addons.util.PdfExtractionUtil.MatchResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Data
@Action(actionText = "Extract No_Of_Words words after occurrence Occurrence of text test_data from PDF and store in a runtime variable Variable_Name",
        description = "Extracts the words from the PDF after the specified occurrence of test_data (supports literal or regex match). Use Occurrence 1 for first, 2 for second, etc. Use_Regex true for regex pattern.",
        applicationType = ApplicationType.WEB)
public class NoofwordsAftertext extends WebAction {

    private static final String SUCCESS_MESSAGE = "Stored desired data.";
    private static final String ERROR_MESSAGE = "Issue in the Operation.";
    private static final String OCCURRENCE_NOT_FOUND = "Occurrence %d of '%s' not found in PDF. Found only %d occurrence(s).";

    @TestData(reference = "No_Of_Words")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "test_data")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "Occurrence")
    private com.testsigma.sdk.TestData occurrence;
    @TestData(reference = "Use_Regex")
    private com.testsigma.sdk.TestData useRegex;
    @TestData(reference = "Variable_Name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData3;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        logger.debug("test-data1: " + testData1.getValue() + " test-data2: " + testData2.getValue() + " test-data3: " + testData3.getValue());
        try {
            PDDocument doc = PDFUtils.getPDDocument(driver.getCurrentUrl());
            String content = new PDFTextStripper().getText(doc);
            String pattern = testData2.getValue().toString();
            int occurrenceNum = parseOccurrence(occurrence);
            boolean regex = parseUseRegex(useRegex);

            MatchResult match = PdfExtractionUtil.findOccurrence(content, pattern, occurrenceNum, regex);
            if (match == null) {
                int count = PdfExtractionUtil.countOccurrences(content, pattern, regex);
                setErrorMessage(String.format(OCCURRENCE_NOT_FOUND, occurrenceNum, pattern, count));
                return Result.FAILED;
            }

            int wordcount = Integer.parseInt(testData1.getValue().toString());
            String extracted = PdfExtractionUtil.extractWordsAfter(content, match, wordcount);

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(extracted);
            runTimeData.setKey(testData3.getValue().toString());
            setSuccessMessage(SUCCESS_MESSAGE + " Next " + testData1.getValue() + " words are " + extracted + " which is stored in runtime variable " + testData3.getValue().toString());
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + " Cause of Exception: " + (e.getCause() != null ? e.getCause().toString() : e.getMessage()));
            return Result.FAILED;
        }
    }

    private static int parseOccurrence(com.testsigma.sdk.TestData occurrence) {
        if (occurrence == null || occurrence.getValue() == null || occurrence.getValue().toString().trim().isEmpty()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(occurrence.getValue().toString().trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static boolean parseUseRegex(com.testsigma.sdk.TestData useRegex) {
        if (useRegex == null || useRegex.getValue() == null) return false;
        String v = useRegex.getValue().toString().trim().toLowerCase();
        return "true".equals(v) || "1".equals(v) || "yes".equals(v);
    }
}
