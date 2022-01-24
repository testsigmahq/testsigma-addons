package com.testsigma.addons.radio.with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Check the radio button with following text containing test-data",
        description = "Selects the radio button with following text that contains the provided value",
        applicationType = ApplicationType.WEB)
public class ClickOnRadioButtonWithFollowingTextContaining extends ClickOnRadioButtonBasedOnAttribute {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setOperator("contains");
        super.setAttribute("text");
        super.setTestData(testData);
        super.setIsLabelText(false);
        super.setIsFollowingText(true);
        return super.execute();
    }
}

