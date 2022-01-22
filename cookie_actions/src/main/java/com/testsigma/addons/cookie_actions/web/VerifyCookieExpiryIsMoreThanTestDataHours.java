package com.testsigma.addons.cookie_actions.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Data
@Action(actionText = "Verify cookie cookie-name expiry is more than number-of-hours hours",
        description = "Verifies if the cookie with a given name expires only after a given hour(s)",
        applicationType = ApplicationType.WEB)
public class VerifyCookieExpiryIsMoreThanTestDataHours extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Cookie's expiry is more than number of days";
    private static final String NO_COOKIE_WITH_NAME_ERROR_MESSAGE = "Cookie with the name not found";
    private static final String COOKIE_EXPIRY_IS_NOT_MORE_THAN_EXPECTED_HOURS_ERROR_MESSAGE = "Cookie expiry is not more than the expected number of hours";
    private static final String COOKIE_DOES_NOT_HAVE_EXPIRY = "The cookie does not have expiry";
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "number-of-hours")
    private com.testsigma.sdk.TestData numberOfHours;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            Cookie cookie = driver.manage().getCookieNamed(cookieName.getValue().toString());
            Date expiryDate = cookie.getExpiry();
            if(expiryDate == null){
                setErrorMessage(COOKIE_DOES_NOT_HAVE_EXPIRY);
                return Result.FAILED;
            }
            long expiryHours = TimeUnit.MILLISECONDS.toHours(expiryDate.getTime() - new Date().getTime());
            if (Long.valueOf(numberOfHours.getValue().toString()) < expiryHours) {
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                System.out.println("Expected Expiry hours - More Than " + numberOfHours.getValue().toString() + ", Actual Expiry hours - " + expiryHours);
                log("Expected Expiry hours - More Than " + numberOfHours.getValue().toString() + ", Actual Expiry hours - " + expiryHours);
                setErrorMessage(COOKIE_EXPIRY_IS_NOT_MORE_THAN_EXPECTED_HOURS_ERROR_MESSAGE);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            setErrorMessage(NO_COOKIE_WITH_NAME_ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

