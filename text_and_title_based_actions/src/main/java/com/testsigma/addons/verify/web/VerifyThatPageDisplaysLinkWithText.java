package com.testsigma.addons.verify.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the current page displays a link with text test-data",
        description = "Checks if the current page has a URL which matches the text",
        applicationType = ApplicationType.WEB)
public class VerifyThatPageDisplaysLinkWithText extends VerifyThatPageDisplaysElementsWithAttributes {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setDriver(driver);
        super.setValue(testData);
        super.setAttribute("text");
        super.setElementType("link");
        super.setOperator("equals");
        return super.execute();
    }
}

