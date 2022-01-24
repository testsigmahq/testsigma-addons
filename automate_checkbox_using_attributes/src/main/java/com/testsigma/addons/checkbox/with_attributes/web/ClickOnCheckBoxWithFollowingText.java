package com.testsigma.addons.checkbox.with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Check the checkbox with following text test-data",
        description = "Selects the checkbox with the given following text",
        applicationType = ApplicationType.WEB)
public class ClickOnCheckBoxWithFollowingText extends ClickOnCheckBoxBasedOnAttributes {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setOperator("equals");
        super.setAttribute("text");
        super.setTestData(testData);
        super.setIsLabelText(false);
        super.setIsFollowingText(true);
        return super.execute();
    }
}

