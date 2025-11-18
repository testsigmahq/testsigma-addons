package com.testsigma.addons.web;

import com.testsigma.addons.util.BrokenLinksUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Action(
        actionText = "Find broken links in the url test-data and its child pages with timeout test-data2 seconds",
        description = "This action identifies and lists all broken links present in the" +
                " given URL and its child pages up to depth 4.",
        applicationType = ApplicationType.WEB)
public class FindBrokenLinksInChildPagesDepth4 extends WebAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData testData2;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            // Get the URL to check
            String urlToCheck = testData.getValue().toString();
            
            // Parse the timeout value
            int connectionTimeout = 0;
            try {
                connectionTimeout = Integer.parseInt(testData2.getValue().toString()) * 1000;
            } catch (NumberFormatException e) {
                logger.warn("Invalid connection timeout value provided: " + testData2.getValue().toString() +
                        ". Using default timeout.");
            }
            
            // Navigate to the URL if not already there
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl == null || !currentUrl.equals(urlToCheck)) {
                driver.get(urlToCheck);
            }
            
            // Call the util to find broken links with depth = 4
            BrokenLinksUtil brokenLinksUtil = new BrokenLinksUtil(logger, driver);
            var brokenLinks = brokenLinksUtil.findAllBrokenLinksWithDepth(urlToCheck, 4, connectionTimeout);
            
            // Update the result based on the broken links
            if (brokenLinks.isEmpty()) {
                logger.info("No broken links found in the URL and its child pages (depth 4): " + urlToCheck);
                setSuccessMessage("No broken links found in the URL and its child pages (depth 4): " + urlToCheck);
            } else {
                logger.info("Broken links found in the URL and its child pages (depth 4): " + urlToCheck + " - " + brokenLinks.size());
                for (String link : brokenLinks) {
                    logger.info(link);
                }
                setErrorMessage("<b>" + brokenLinks.size() + "</b> broken links found in the URL and its child pages (depth 4) " + urlToCheck + ": <br>"
                        + String.join("<br>", brokenLinks));
                result = Result.FAILED;
            }
            
            // Navigate back to the original URL if needed
            if (currentUrl != null && !currentUrl.equals(driver.getCurrentUrl())) {
                driver.get(currentUrl);
            }
        } catch (Exception e) {
            logger.warn("An error occurred while finding broken links: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("An error occurred while finding broken links: " + e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }
}

