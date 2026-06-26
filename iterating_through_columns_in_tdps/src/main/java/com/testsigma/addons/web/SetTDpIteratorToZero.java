package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Objects;

@Action(actionText = "Set TDP iterator TDP_ITERATOR_KEY_NAME value to 0",
 description = "Set TDP iterator TDP_ITERATOR_KEY_NAME to 0",
 applicationType = ApplicationType.WEB,
 useCustomScreenshot = false)
public class SetTDpIteratorToZero extends WebAction {

    @TestData(reference = "TDP_ITERATOR_KEY_NAME", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        logger.debug("Runtime variable:" + testData1.getValue().toString());

        if(!Objects.equals(testData1.getValue().toString(), "TDP_ITERATOR_KEY_NAME")){
            logger.warn("Don't change the value of TDP_ITERATOR_KEY_NAME");
            setErrorMessage("Don't change the TDP_ITERATOR_KEY_NAME variable name");
            return com.testsigma.sdk.Result.FAILED;
        }
        try {
            runTimeData.setValue("0");
            runTimeData.setKey("TDP_ITERATOR_KEY_NAME");
            logger.info("Set runtime variable to 0");
            setSuccessMessage("Set TDP iterator TDP_ITERATOR_KEY_NAME to 0");
            return com.testsigma.sdk.Result.SUCCESS;
        }
        catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            logger.info("Error occurred while setting runtime variable to 0: " + ExceptionUtils.getMessage(e));
            setErrorMessage("Error occurred while setting runtime variable to 0: " + ExceptionUtils.getMessage(e));
            return com.testsigma.sdk.Result.FAILED;
        }
    }
    
}
