package com.testsigma.addons.while_on_text_elements.mobile_web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

@Data
@Action(actionText = "While the element does not display text",
        actionType = StepActionType.WHILE_LOOP,
        description = "Performs actions while element does not display the provided text",
        applicationType = ApplicationType.MOBILE_WEB)
public class WhileElementDoesNotDisplayText extends com.testsigma.addons.while_on_text_elements.web.WhileElementDoesNotDisplayText {
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;
}