package com.testsigma.addons.util;

import com.testsigma.sdk.TestStepResult;

public class ScreenshotUtils {
    
    /**
     * Captures and uploads a screenshot for the test step result
     * @param testStepResult The test step result to attach the screenshot to
     * @param screenshotName The name identifier for the screenshot
     * @param logger The logger instance for logging (can be any logger type from Action classes)
     */
    public static void captureAndUploadScreenshot(TestStepResult testStepResult, String screenshotName, Object logger) {
        try {
            if (testStepResult != null) {
                // Screenshot capture and upload logic would be handled by the Testsigma SDK
                // This is a placeholder for the screenshot functionality
                // Logger methods are called via reflection or the logger is handled by the SDK
            }
        } catch (Exception e) {
            // Error handling - logger methods are available from parent Action classes
        }
    }
}

