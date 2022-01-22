package com.testsigma.addons.while_on_text_elements.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

@Data
@Action(actionText = "While the element contains text",
        actionType = StepActionType.WHILE_LOOP,
        description = "Performs actions while current page contains the provided text",
        applicationType = ApplicationType.MOBILE_WEB)
public class WhileElementContainsText extends com.testsigma.addons.while_on_text_elements.web.WhileElementContainsText {
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;
}