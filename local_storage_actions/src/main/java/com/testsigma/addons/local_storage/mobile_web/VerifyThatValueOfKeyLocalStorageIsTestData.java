package com.testsigma.addons.local_storage.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the value of key in local storage is test-data",
        description = "Verifies that the key/value pair stored in the local browser storage matches a given key/value pair and returns the status",
        applicationType = ApplicationType.MOBILE_WEB)
public class VerifyThatValueOfKeyLocalStorageIsTestData extends com.testsigma.addons.local_storage.web.VerifyThatValueOfKeyLocalStorageIsTestData {
    @TestData(reference = "key")
    private com.testsigma.sdk.TestData key;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData expectedValue;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setKey(key);
        super.setExpectedValue(expectedValue);
        return super.execute();
    }
}