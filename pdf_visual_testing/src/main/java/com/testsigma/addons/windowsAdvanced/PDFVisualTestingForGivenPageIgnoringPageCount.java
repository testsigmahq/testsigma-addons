package com.testsigma.addons.windowsAdvanced;

import com.testsigma.addons.windowsAdvanced.util.GivenPageComparisonEngine;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAdvancedAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the base pdf base-pdf-file-path and the actual pdf actual-pdf-file-path is" +
        " same for page page-number by performing visual analysis (ignoring page count)",
        description = "Verifies that the base pdf and the actual pdf is same by performing visual" +
                " analysis. (indexing starts from 1)",
        displayName = "PDF Visual Testing For Given Page Ignoring Page Count",
        applicationType = ApplicationType.WINDOWS_ADVANCED,
        useCustomScreenshot = true)
public class PDFVisualTestingForGivenPageIgnoringPageCount extends WindowsAdvancedAction {

    @TestData(reference = "base-pdf-file-path")
    private com.testsigma.sdk.TestData basePdfPath_;

    @TestData(reference = "actual-pdf-file-path")
    private com.testsigma.sdk.TestData actualPdfPath_;

    @TestData(reference = "page-number")
    private com.testsigma.sdk.TestData pageNumber_;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        try {
            GivenPageComparisonEngine.Outcome outcome = new GivenPageComparisonEngine(logger).compareGivenPage(
                    basePdfPath_.getValue().toString(),
                    actualPdfPath_.getValue().toString(),
                    pageNumber_.getValue().toString(),
                    false,
                    testStepResult.getScreenshotUrl());
            if (outcome.result == Result.SUCCESS) {
                setSuccessMessage(outcome.message);
            } else {
                setErrorMessage(outcome.message);
            }
            return outcome.result;
        } catch (RuntimeException e) {
            logger.info("Runtime Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform the operation : " + e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            logger.info("Exception : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Unable to perform the operation : " + e.getMessage());
            return Result.FAILED;
        }
    }
}
