package com.testsigma.addons.mobile_gestures.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

@Data
@Action(actionText = "Do Pinch Zoom Out action on the element with percent percent(% between 0.00 to 1.00)", applicationType = ApplicationType.ANDROID)
public class PinchClosegesture extends AndroidAction {


    @TestData(reference = "percent")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;


    @Override
    protected com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here


        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        logger.info("Initiating execution");
        logger.debug("Element: " + this.element.getValue() + " by:" + this.element.getBy());
        AndroidDriver androidDriver = (AndroidDriver) this.driver;
        WebElement webElement = element.getElement();
        if (webElement.isDisplayed()) {
            logger.info("Element is displayed, entering data");
            setSuccessMessage(String.format("Element Found Proceeding next actions"));
        } else {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Element with locator %s is not visible ::" + element.getBy() + "::" + element.getValue());
            setErrorMessage(String.format("Element with locator %s is not visible", element.getBy()));
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("percent", Float.parseFloat(testData.getValue().toString()));
        params.put("left", webElement.getLocation().getX() + 50);
        params.put("top", webElement.getLocation().getY() - 50);
        params.put("width", webElement.getRect().getHeight() - 50);
        params.put("height", webElement.getRect().getWidth() - 50);

        JavascriptExecutor js = androidDriver;
        js.executeScript("mobile: pinchCloseGesture", params);

        setSuccessMessage("Successfully PinchClose gesture performed on element");

        return result;

    }
}

