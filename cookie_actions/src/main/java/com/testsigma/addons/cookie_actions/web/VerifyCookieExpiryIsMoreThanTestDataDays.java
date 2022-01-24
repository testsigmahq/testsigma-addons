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
@Action(actionText = "Verify cookie cookie-name expiry is more than number-of-days days",
        description = "Verifies if the cookie with a given name expires only after a given specific number of day(s)",
        applicationType = ApplicationType.WEB)
public class VerifyCookieExpiryIsMoreThanTestDataDays extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Cookie's expiry is more than number of days";
    private static final String NO_COOKIE_WITH_NAME_ERROR_MESSAGE = "Cookie with the name not found";
    private static final String COOKIE_EXPIRY_IS_NOT_MORE_THAN_EXPECTED_DAYS_ERROR_MESSAGE = "Cookie expiry is not more than the expected number of days";
    private static final String COOKIE_DOES_NOT_HAVE_EXPIRY = "The cookie does not have expiry";
    @TestData(reference = "cookie-name")
    private com.testsigma.sdk.TestData cookieName;
    @TestData(reference = "number-of-days")
    private com.testsigma.sdk.TestData numberOfDays;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            Cookie cookie = driver.manage().getCookieNamed(cookieName.getValue().toString());
            Date expiryDate = cookie.getExpiry();
            if(expiryDate == null){
                setErrorMessage(COOKIE_DOES_NOT_HAVE_EXPIRY);
                return Result.FAILED;
            }
            long expiryDays = TimeUnit.MILLISECONDS.toDays(expiryDate.getTime() - new Date().getTime());
            if (Long.valueOf(numberOfDays.getValue().toString()) < expiryDays) {
                setSuccessMessage(SUCCESS_MESSAGE);
                return Result.SUCCESS;
            } else {
                System.out.println("Expected Expiry days - More Than " + numberOfDays.getValue().toString() + ", Actual Expiry days - " + expiryDays);
                log("Expected Expiry days - More Than " + numberOfDays.getValue().toString() + ", Actual Expiry days - " + expiryDays);
                setErrorMessage(COOKIE_EXPIRY_IS_NOT_MORE_THAN_EXPECTED_DAYS_ERROR_MESSAGE);
                return Result.FAILED;
            }
        } catch (Exception exception) {
            setErrorMessage(NO_COOKIE_WITH_NAME_ERROR_MESSAGE);
            return Result.FAILED;
        }
    }
}

