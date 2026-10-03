package com.testsigma.addons.web;


import com.testsigma.addons.util.ImageComparisonUtils;
import com.testsigma.addons.util.StringToImageConverter;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;



@Action(actionText = "Display string test-data as Image in step result", 
    description = "Display string test-data as Image in step result",
    applicationType = ApplicationType.WEB,
    useCustomScreenshot = true
)
public class DisplayTestDataAsStepResultScreenShot extends WebAction {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData inputTestData;

    @TestStepResult
    private com.testsigma.sdk.TestStepResult testStepResult;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            logger.debug("initiating execution");
            String testData = inputTestData.getValue().toString();
            logger.debug("Test Data: " + testData);

            File screenshotFile = StringToImageConverter.convertToFile(testData);
            logger.debug("Created screenshot image at: " + screenshotFile.getAbsolutePath());

            // upload the screenshot to the test step result (same pattern as VerifyIfTwoImagesAreSimilar)
            String s3Url = testStepResult.getScreenshotUrl();
            if (s3Url != null && !s3Url.isEmpty()) {
                ImageComparisonUtils imageComparisonUtils = new ImageComparisonUtils(driver, logger);
                boolean uploadResult = imageComparisonUtils.uploadFile(s3Url, screenshotFile.getAbsolutePath());
                if (!uploadResult) {
                    logger.debug("Error uploading custom screenshot to S3; step result may not show the image.");
                    setErrorMessage("Error uploading custom screenshot, step result may not show the image.");
                    result = Result.FAILED;
                } else {
                    logger.debug("Custom screenshot uploaded successfully.");
                    setSuccessMessage("Successfully displayed test data as image in step result");
                }
            }
            screenshotFile.deleteOnExit();
        } catch (Exception e) {
            result = Result.FAILURE;
            logger.debug("Error while displaying test data as image in step result: " + ExceptionUtils.getStackTrace(e));
        }
        return result;
    }


    
}
