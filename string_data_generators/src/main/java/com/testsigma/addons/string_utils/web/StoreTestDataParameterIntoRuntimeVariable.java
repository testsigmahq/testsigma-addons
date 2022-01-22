package com.testsigma.addons.string_utils.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Store test-data-1 in runtime variable test-data-2",
        description = "Stores the testData in runtime variable",
        applicationType = ApplicationType.WEB)
public class StoreTestDataParameterIntoRuntimeVariable extends WebAction {

    private static final String SUCCESS_MESSAGE = "the testData parameter is stored Successfully in runtime variable";
    private static final String ERROR_MESSAGE = "Expected textarea Not Found";
    @TestData(reference = "test-data-1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "test-data-2")
    private com.testsigma.sdk.TestData testData2;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        runTimeData.setKey(testData2.getValue().toString());
        runTimeData.setValue(testData1.getValue());
        setSuccessMessage(SUCCESS_MESSAGE);
        return Result.SUCCESS;
    }
}

