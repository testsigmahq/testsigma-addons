package com.testsigma.addons.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;

import java.util.Objects;

@Action(actionText = "set TDP iterator TDP_ITERATOR_KEY_NAME value to 0",
        description = "Set TDP iterator TDP_ITERATOR_KEY_NAME to 0",
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false)
public class SetTDpIteratorToZero extends AndroidAction {

    @TestData(reference = "TDP_ITERATOR_KEY_NAME", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        if (!Objects.equals(testData1.getValue().toString(), "TDP_ITERATOR_KEY_NAME")) {
            setErrorMessage("Don't change the TDP_ITERATOR_KEY_NAME variable name");
            return com.testsigma.sdk.Result.FAILED;
        }
        try {
            runTimeData.setValue("0");
            runTimeData.setKey("TDP_ITERATOR_KEY_NAME");
            setSuccessMessage("Set TDP iterator TDP_ITERATOR_KEY_NAME to 0");
            return com.testsigma.sdk.Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage("Error occurred while setting runtime variable to 0: " + e.getMessage());
            return com.testsigma.sdk.Result.FAILED;
        }
    }
}
