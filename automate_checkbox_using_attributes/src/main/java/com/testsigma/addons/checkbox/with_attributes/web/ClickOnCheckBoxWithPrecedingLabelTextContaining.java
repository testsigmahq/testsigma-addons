package com.testsigma.addons.checkbox.with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Check the checkbox with preceding label containing test-data",
        description = "Selects the checkbox with a preceding label that contains the provided value",
        applicationType = ApplicationType.WEB)
public class ClickOnCheckBoxWithPrecedingLabelTextContaining extends ClickOnCheckBoxBasedOnAttributes {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;


    public Result execute() throws NoSuchElementException {
        super.setAttribute("text");
        super.setTestData(testData);
        super.setOperator("contains");
        super.setIsLabelText(true);
        super.setIsPrecedingText(true);
        return super.execute();
    }
}
