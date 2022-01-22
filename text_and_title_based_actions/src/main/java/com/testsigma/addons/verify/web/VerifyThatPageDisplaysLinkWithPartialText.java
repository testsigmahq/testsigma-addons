package com.testsigma.addons.verify.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Verify that the current page displays a link with partial text test-data",
        description = "Checks if the current page has a URL which matches the partial text",
        applicationType = ApplicationType.WEB)
public class VerifyThatPageDisplaysLinkWithPartialText extends VerifyThatPageDisplaysElementsWithAttributes {
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setDriver(driver);
        super.setValue(testData);
        super.setAttribute("text");
        super.setElementType("link");
        super.setOperator("contains");
        return super.execute();
    }

}

