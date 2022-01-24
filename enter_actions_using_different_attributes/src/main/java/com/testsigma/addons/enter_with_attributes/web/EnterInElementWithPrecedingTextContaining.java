package com.testsigma.addons.enter_with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Enter test-data in the element with preceding text containing value",
        description = "Enters the test-data in the element with a preceding text that contains the provided text",
        applicationType = ApplicationType.WEB)
public class EnterInElementWithPrecedingTextContaining extends EnterInElementBasedOnAttributes {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    public Result execute() throws NoSuchElementException {
        super.setTestData(testData);
        super.setAttribute("text");
        super.setValue(value);
        super.setOperator("contains");
        super.setIsLabelText(false);
        return super.execute();
    }
}
