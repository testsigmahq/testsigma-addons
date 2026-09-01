package com.testsigma.addons.pdf_action_with_file_input;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.addons.util.PdfExtractionUtil;
import com.testsigma.addons.util.PdfExtractionUtil.MatchResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

@Data
@Action(actionText = "Extract No_Of_Character characters before occurrence Occurrence of search_pattern from PDF at file path pdf_file_path using match type Match_Type and store in a runtime variable Variable_Name",
        description = "Extracts N characters before the specified occurrence of search_pattern. Match_Type: choose 'Text' for literal text match or 'Regex' for regex pattern. Occurrence: 1=first, 2=second, etc.",
        applicationType = ApplicationType.WEB)
public class ExtractCharsBeforeByOccurrence extends PdfFileInputBase {

    private static final String ERROR_MESSAGE = "Issue in the Operation";
    private static final String OCCURRENCE_NOT_FOUND = "Occurrence %d of '%s' not found in PDF. Found only %d occurrence(s).";

    @TestData(reference = "pdf_file_path")
    private com.testsigma.sdk.TestData pdfFilePath;
    @TestData(reference = "No_Of_Character")
    private com.testsigma.sdk.TestData noOfCharacters;
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
        String pathOrUrl = pdfFilePath.getValue().toString().trim();
        File basePDF = urlToFileConverter("base.pdf", pathOrUrl);
        if (!verifyPdfFile(basePDF)) {
            return Result.FAILED;
        }
        try (PDDocument doc = loadPdfDocument(basePDF)) {
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

            int numChars = Integer.parseInt(noOfCharacters.getValue().toString());
            String extracted = PdfExtractionUtil.extractCharsBefore(content, match, numChars);

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(extracted);
            runTimeData.setKey(variableName.getValue().toString());
            setSuccessMessage("Extracted " + numChars + " characters before occurrence " + occurrenceNum + " i.e " + extracted + " and stored in runtime variable " + variableName.getValue());
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
