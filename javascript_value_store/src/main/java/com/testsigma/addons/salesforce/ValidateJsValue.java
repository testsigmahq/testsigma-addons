package com.testsigma.addons.salesforce;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.SalesforceAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Execute JavaScript snippet testdata on an element1 and store the returned value in variable var1",
        description = "Executes JS and stores the value in runtime variable",
        applicationType = ApplicationType.Salesforce)
public class ValidateJsValue extends SalesforceAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData jssnippet;

    @Element(reference = "element1")
    private com.testsigma.sdk.Element element;

    @TestData(reference = "var1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        logger.info("JS SNIPPET IS " + jssnippet.getValue().toString());

        Result result = Result.SUCCESS;
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object obj = js.executeScript(jssnippet.getValue().toString(), element);
            String str = (obj == null) ? "" : obj.toString();

            runTimeData.setKey(variable.getValue().toString());
            runTimeData.setValue(str);
            setSuccessMessage("Successfully stored retured result " + str + " into a runtime variable " + runTimeData);
        } catch (Exception e) {
            result = Result.FAILED;
            logger.debug("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to perform operation " + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}