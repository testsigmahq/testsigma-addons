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
@Action(actionText = "Extract No_Of_Words words after occurrence Occurrence of search_pattern from PDF using match type Match_Type and store in a runtime variable Variable_Name",
        description = "Extracts N words after the specified occurrence of search_pattern. Match_Type: choose 'Text' for literal text match or 'Regex' for regex pattern. Occurrence: 1=first, 2=second, etc.",
        applicationType = ApplicationType.WEB)
public class ExtractWordsAfterByOccurrence extends WebAction {

    private static final String ERROR_MESSAGE = "Issue in the Operation.";
    private static final String OCCURRENCE_NOT_FOUND = "Occurrence %d of '%s' not found in PDF. Found only %d occurrence(s).";

    @TestData(reference = "No_Of_Words")
    private com.testsigma.sdk.TestData noOfWords;
    @TestData(reference = "search_pattern")
    private com.testsigma.sdk.TestData searchPattern;
    @TestData(reference = "Occurrence")
    private com.testsigma.sdk.TestData occurrence;
    @TestData(reference = "Match_Type")
    private com.testsigma.sdk.TestData matchType;
    @TestData(reference = "Variable_Name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        try {
            PDDocument doc = PDFUtils.getPDDocument(driver.getCurrentUrl());
            String content = new PDFTextStripper().getText(doc);
            String pattern = searchPattern.getValue().toString();
            int occurrenceNum = parseOccurrence(occurrence);
            boolean useRegex = parseMatchType(matchType);

            MatchResult match = PdfExtractionUtil.findOccurrence(content, pattern, occurrenceNum, useRegex);
            if (match == null) {
                int count = PdfExtractionUtil.countOccurrences(content, pattern, useRegex);
                setErrorMessage(String.format(OCCURRENCE_NOT_FOUND, occurrenceNum, pattern, count));
                return Result.FAILED;
            }

            int numWords = Integer.parseInt(noOfWords.getValue().toString());
            String extracted = PdfExtractionUtil.extractWordsAfter(content, match, numWords);

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(extracted);
            runTimeData.setKey(variableName.getValue().toString());
            setSuccessMessage("Extracted " + numWords + " words after occurrence " + occurrenceNum + " i.e " + extracted + " and stored in runtime variable " + variableName.getValue());
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.debug("Exception: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage(ERROR_MESSAGE + " Cause: " + (e.getCause() != null ? e.getCause().toString() : e.getMessage()));
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

    /** Match_Type allowed values: "Text" (or "text input") = literal match, "Regex" (or "regex input") = regex match. */
    private static boolean parseMatchType(com.testsigma.sdk.TestData matchType) {
        if (matchType == null || matchType.getValue() == null) return false;
        String v = matchType.getValue().toString().trim().toLowerCase();
        return "regex".equals(v) || "regex input".equals(v);
    }
}
