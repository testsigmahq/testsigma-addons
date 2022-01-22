package com.testsigma.addons.string_utils.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Store test-data-1 in runtime variable test-data-2",
        description = "Stores the testData in runtime variable",
        applicationType = ApplicationType.MOBILE_WEB)
public class StoreTestDataParameterIntoRuntimeVariable extends com.testsigma.addons.string_utils.web.StoreTestDataParameterIntoRuntimeVariable {
    @TestData(reference = "test-data-1")
    private com.testsigma.sdk.TestData testData1;
    @TestData(reference = "test-data-2")
    private com.testsigma.sdk.TestData testData2;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    @Override
    public Result execute() throws NoSuchElementException {
        return super.execute();
    }
}

