package com.testsigma.addons.passwordbox.enter_with_attributes.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Enter test-data in the password text box with preceding text value",
        description = "Enters data into the password field for given preceding text",
        applicationType = ApplicationType.WEB)
public class EnterInPasswordBoxWithPrecedingText extends EnterInPasswordBoxBasedOnAttribute {

    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData value;

    public Result execute() throws NoSuchElementException {
        super.setTestData(testData);
        super.setAttribute("text");
        super.setValue(value);
        super.setOperator("equals");
        super.setIsLabelText(false);
        super.setIsPrecedingText(true);
        return super.execute();
    }
}
