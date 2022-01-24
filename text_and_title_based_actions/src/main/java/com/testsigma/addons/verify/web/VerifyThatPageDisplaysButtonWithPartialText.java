package com.testsigma.addons.verify.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the current page displays a button with partial text test-data",
        description = "Checks if the current page has a button which matches the partial text",
        applicationType = ApplicationType.WEB)
public class VerifyThatPageDisplaysButtonWithPartialText extends VerifyThatPageDisplaysElementsWithAttributes {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setDriver(driver);
        super.setValue(testData);
        super.setAttribute("text");
        super.setElementType("button");
        super.setOperator("contains");
        return super.execute();
    }
}

