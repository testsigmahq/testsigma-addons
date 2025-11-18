package com.testsigma.addons.mobileweb;

import com.testsigma.addons.util.BrokenLinksUtil;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.util.HashSet;
import java.util.Set;

@Data
@Action(
        actionText = "Find broken links in the current page with timeout test-data seconds",
        description = "This action identifies and lists all broken links present on the current web page.",
        applicationType = ApplicationType.MOBILE_WEB)
public class FindBrokenLinksInTheCurrentPage extends WebAction {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        Set<String> brokenLinks = new HashSet<>();
        try {
            logger.info("=== Starting to find broken links in the current page ===");

            int connectionTimeout = 0; // default timeout
            try {
                connectionTimeout = Integer.parseInt(testData.getValue().toString()) * 1000;
            } catch (NumberFormatException e) {
                logger.warn("Invalid connection timeout value provided: " + testData.getValue().toString() +
                        ". Using default timeout.");
            }

            // Get current page URL
            String currentPageUrl = driver.getCurrentUrl();
            
            // call util to find broken links (depth=0 means only current page, no child pages)
            BrokenLinksUtil brokenLinksUtil = new BrokenLinksUtil(logger, driver);
            brokenLinks = brokenLinksUtil.findAllBrokenLinksWithDepth(currentPageUrl, 0, connectionTimeout);

            // update result based on broken links found
            if (brokenLinks.isEmpty()) {
                logger.info("No broken links found on the current page.");
                setSuccessMessage("No broken links found on the current page.");
            } else {
                logger.info("Broken links found on the current page:" + brokenLinks.size());
                for (String link : brokenLinks) {
                    logger.info(link);
                }
                setErrorMessage("<b>" + brokenLinks.size() + "</b> broken links found on the current page: <br>"
                        + String.join("<br>", brokenLinks));
                result = Result.FAILED;
            }
        } catch (Exception e) {
            logger.warn("An error occurred while finding broken links: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("An error occurred while finding broken links: " + ExceptionUtils.getStackTrace(e));
            result = Result.FAILED;
        }

        return result;
    }
}

