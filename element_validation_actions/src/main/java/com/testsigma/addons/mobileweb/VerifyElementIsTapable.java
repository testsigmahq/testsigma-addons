package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.ApplicationType;
import lombok.Data;
import com.testsigma.sdk.Result;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Verifies that a UI element is tapable/clickable in the mobile web application.
 * Behavior is consistent with web clickable validation: checks visibility, enabled state,
 * and that the element is not obscured (via Selenium's elementToBeClickable).
 * Works across supported mobile browsers (e.g., Chrome, Safari).
 */
@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Action(actionText = "Verify that the element element-locator is tapable",
        description = "Verifies that the element is tapable (visible, enabled, and not blocked by another element) in the mobile web application. Use for consistent validation across mobile browsers.",
        applicationType = ApplicationType.MOBILE_WEB,
        useCustomScreenshot = false)
public class VerifyElementIsTapable extends WebAction {

    private static final int DEFAULT_WAIT_SECONDS = 30;

    @Element(reference = "element-locator")
    private com.testsigma.sdk.Element element;

    @Override
    public Result execute() throws NoSuchElementException {
        logger.info("Verifying element is tapable (mobile web)");
        logger.debug("Element locator: " + this.element.getValue() + " by: " + this.element.getBy());

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS));
            WebElement webElement = wait.until(ExpectedConditions.elementToBeClickable(element.getBy()));

            if (webElement == null) {
                setErrorMessage("Element is not tapable: element not found or not interactable within " + DEFAULT_WAIT_SECONDS + " seconds.");
                logger.warn(getErrorMessage());
                return Result.FAILED;
            }

            setSuccessMessage("Element is tapable (visible, enabled, and ready for interaction).");
            logger.info(getSuccessMessage());
            return Result.SUCCESS;

        } catch (org.openqa.selenium.TimeoutException e) {
            String reason = "Element is not tapable within " + DEFAULT_WAIT_SECONDS + " seconds. It may be hidden, disabled, or covered by another element.";
            setErrorMessage(reason + " " + e.getMessage());
            logger.warn(reason + " " + e.getMessage());
            return Result.FAILED;
        } catch (NoSuchElementException e) {
            setErrorMessage("Element not found: " + e.getMessage());
            logger.warn(getErrorMessage());
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Element could not be verified as tapable: " + e.getMessage());
            logger.warn(getErrorMessage());
            return Result.FAILED;
        }
    }
}
