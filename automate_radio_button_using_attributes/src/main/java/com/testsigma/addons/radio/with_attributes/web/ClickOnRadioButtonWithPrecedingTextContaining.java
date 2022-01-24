package com.testsigma.addons.radio.with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Check the radio button with preceding text test-data",
        description = "Selects the radio button with the given preceding text",
        applicationType = ApplicationType.WEB)
public class ClickOnRadioButtonWithPrecedingTextContaining extends ClickOnRadioButtonBasedOnAttribute {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;

    public Result execute() throws NoSuchElementException {
        ClickOnRadioButtonBasedOnAttribute nlp = new ClickOnRadioButtonBasedOnAttribute();

        super.setAttribute("text");
        super.setTestData(testData);
        super.setOperator("contains");
        super.setIsLabelText(false);
        super.setIsPrecedingText(true);
        return super.execute();
    }

}
