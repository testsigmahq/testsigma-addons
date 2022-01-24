package com.testsigma.addons.verify.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the current page displays a button with title test-data",
        description = "Checks if the current page has a button which matches the title",
        applicationType = ApplicationType.WEB)
public class VerifyThatPageDisplaysButtonWithTitle extends VerifyThatPageDisplaysElementsWithAttributes {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setDriver(driver);
        super.setValue(testData);
        super.setAttribute("title");
        super.setElementType("button");
        super.setOperator("equals");
        return super.execute();
    }
}

