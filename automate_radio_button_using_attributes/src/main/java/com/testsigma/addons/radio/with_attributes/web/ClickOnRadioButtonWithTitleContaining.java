package com.testsigma.addons.radio.with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Check the radio button with title containing test-data",
        description = "Selects the radio button with title that contains the provided value",
        applicationType = ApplicationType.WEB)
public class ClickOnRadioButtonWithTitleContaining extends ClickOnRadioButtonBasedOnAttribute {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    @Override
    public Result execute() throws NoSuchElementException {
        super.setOperator("contains");
        super.setAttribute("title");
        super.setTestData(testData);
        super.setIsLabelText(false);
        return super.execute();
    }
}

