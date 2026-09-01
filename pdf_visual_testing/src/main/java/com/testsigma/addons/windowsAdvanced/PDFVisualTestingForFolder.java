package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.web.util.PdfFolderComparisonEngine;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.openqa.selenium.NoSuchElementException;

@Action(actionText = "Perform visual testing on all pdf files in base folder base-folder-path against the" +
        " pdf files in actual folder actual-folder-path, pairing files by pairing-strategy",
        description = "Iterates over every pdf file in the base folder and its corresponding pdf file in the" +
                " actual folder (paired either by matching filename or by matching sorted position, controlled" +
                " by pairing-strategy), performs page-by-page visual testing for each pair, and generates a" +
                " single consolidated pdf report containing side-by-side comparison images for every page" +
                " checked. The local path of this report is included in both the success and the error" +
                " message, along with the number of files that passed and the number that failed.",
        displayName = "PDF Visual Testing For Folder",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = false)
public class PDFVisualTestingForFolder extends WindowsAdvancedAction {

    @TestData(reference = "base-folder-path")
    private com.testsigma.sdk.TestData baseFolderPath_;

    @TestData(reference = "actual-folder-path")
    private com.testsigma.sdk.TestData actualFolderPath_;

    @TestData(reference = "pairing-strategy", allowedValues = {"filename", "position"})
    private com.testsigma.sdk.TestData pairingStrategy_;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        String baseFolderPath = baseFolderPath_.getValue().toString();
        String actualFolderPath = actualFolderPath_.getValue().toString();
        String pairingStrategy = pairingStrategy_.getValue().toString();
        logger.info("Base folder path: " + baseFolderPath + ", Actual folder path: " + actualFolderPath +
                ", Pairing strategy: " + pairingStrategy);

        PdfFolderComparisonEngine.Outcome outcome;
        try {
            outcome = new PdfFolderComparisonEngine(logger)
                    .compareFolders(baseFolderPath, actualFolderPath, pairingStrategy);
        } catch (PdfFolderComparisonEngine.ReportGenerationException e) {
            setErrorMessage(e.getMessage());
            return Result.FAILED;
        }

        String reportLink = PdfFolderComparisonEngine.buildReportLink(outcome.reportFile);

        if (outcome.setupError != null) {
            setErrorMessage(outcome.setupError + ". Consolidated report: <b>" + reportLink + "</b>");
            return Result.FAILED;
        }

        int totalFiles = outcome.filesPassed + outcome.filesFailed;
        if (outcome.filesFailed == 0) {
            setSuccessMessage(String.format("Visual testing completed for all <b>%d</b> file(s) - <b>%d passed</b>," +
                            " <b>%d failed</b>. Consolidated report: <b>%s</b>",
                    totalFiles, outcome.filesPassed, outcome.filesFailed, reportLink));
            return Result.SUCCESS;
        } else {
            setErrorMessage(String.format("Visual testing completed for <b>%d</b> file(s) - <b>%d passed</b>," +
                            " <b>%d failed</b>. Consolidated report: <b>%s</b>. Failures: %s",
                    totalFiles, outcome.filesPassed, outcome.filesFailed, reportLink, outcome.failureDetails));
            return Result.FAILED;
        }
    }
}
