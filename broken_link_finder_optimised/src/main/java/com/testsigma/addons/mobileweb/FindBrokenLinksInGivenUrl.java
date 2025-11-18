package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.BrokenLinksUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Action(
        actionText = "Find broken links in given url test-data with timeout test-data2 seconds",
        description = "This action identifies and lists all broken links present on the specified URL.",
        applicationType = ApplicationType.MOBILE_WEB)
public class FindBrokenLinksInGivenUrl extends WebAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData testData2;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        try {
            // get the current url
            String currentUrl = driver.getCurrentUrl();
            int connectionTimeout = 0; // default timeout
            try {
                connectionTimeout = Integer.parseInt(testData2.getValue().toString()) * 1000;
            } catch (NumberFormatException e) {
                logger.warn("Invalid connection timeout value provided: " + testData2.getValue().toString() +
                        ". Using default timeout.");
            }
            assert currentUrl != null;
            String urlToCheck = testData.getValue().toString();
            if (!currentUrl.equals(urlToCheck)) {
                driver.get(urlToCheck);
            }
            // call util to find broken links (depth=0 means only current page, no child pages)
            BrokenLinksUtil brokenLinksUtil = new BrokenLinksUtil(logger, driver);
            var brokenLinks = brokenLinksUtil.findAllBrokenLinksWithDepth(urlToCheck, 0, connectionTimeout);

            // update result based on broken links found
            if (brokenLinks.isEmpty()) {
                logger.info("No broken links found on the page: " + testData.getValue().toString());
                setSuccessMessage("No broken links found on the page: " + testData.getValue().toString());
            } else {
                logger.info("Broken links found on the page: " + testData.getValue().toString() + " - " + brokenLinks.size());
                for (String link : brokenLinks) {
                    logger.info(link);
                }
                setErrorMessage("<b>" + brokenLinks.size() + "</b> broken links found on the page " + testData.getValue().toString() + ": <br>"
                        + String.join("<br>", brokenLinks));
                result = Result.FAILED;
            }
            // navigate back to the original url
            if(driver.getCurrentUrl() != null && !driver.getCurrentUrl().equals(currentUrl)) {
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

