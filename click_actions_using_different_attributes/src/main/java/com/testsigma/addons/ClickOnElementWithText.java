package com.testsigma.addons;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Click on the element with text test-data",
        description = "Performs click action on an element with the given text",
        applicationType = ApplicationType.WEB)
public class ClickOnElementWithText extends ClickOnElementBasedOnAttributeValues {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setOperator("equals");
        super.setAttribute("text");
        super.setElementType("element");
        super.setValue(testData);
        return super.execute();
    }
}

