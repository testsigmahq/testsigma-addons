package com.testsigma.addons.textbox.enter_with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Enter test-data in the text box with label text containing value",
        description = "Enters data into the text box containing part of the given label",
        applicationType = ApplicationType.WEB)
public class EnterInTextBoxWithLabelTextContaining extends EnterInTextBoxBasedOnAttributes {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    public Result execute() throws NoSuchElementException {
        super.setTestData(testData);
        super.setAttribute("text");
        super.setValue(value);
        super.setOperator("contains");
        super.setIsLabelText(true);
        super.setIsContainerLabel(true);
        return super.execute();
    }
}
