package com.testsigma.addons.while_on_text_elements.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Element;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

@Data
@Action(actionText = "While the element does not display text",
        actionType = StepActionType.WHILE_LOOP,
        description = "Performs actions while element does not display the provided text",
        applicationType = ApplicationType.WEB)
public class WhileElementDoesNotDisplayText extends WebAction {

    private static final String ERROR_MESSAGE = "The Element displays the Expected text";
    private static final String SUCCESS_MESSAGE = "The Element does not display the the Text \"%s\"";
    private static final String DISPLAY_NONE_SUCCESS_MESSAGE = "The Element is not displayed";
    private static final String VISIBILITY_NONE_SUCCESS_MESSAGE = "The Element is not visible";
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;
    private List<WebElement> elements;

    @Override
    public Result execute() throws NoSuchElementException {
        try{
            String elementText = element.getElement().getText();
            WebElement webElement = element.getElement();
            if (elementText != null && elementText.equals(testData.getValue().toString())) {
                String displayValue = webElement.getCssValue("display");
                if (!displayValue.equals("none")) {
                    try {
                        String visibility = webElement.getCssValue("visibility");
                        if (!(visibility.equals("collapse") || visibility.equals("hidden"))) {
                            setSuccessMessage(ERROR_MESSAGE);
                            return Result.FAILED;
                        } else {
                            setErrorMessage(VISIBILITY_NONE_SUCCESS_MESSAGE);
                            return Result.SUCCESS;
                        }
                    } catch (Exception e) {
                        logger.info("Ignore this error," + e);
                        setSuccessMessage(ERROR_MESSAGE);
                        return Result.FAILED;
                    }
                } else {
                    setErrorMessage(DISPLAY_NONE_SUCCESS_MESSAGE);
                    return Result.FAILED;
                }
            } else {
                setErrorMessage(String.format(SUCCESS_MESSAGE, testData.getValue().toString()));
                return Result.SUCCESS;
            }
        }catch (Exception exception){
            setErrorMessage(String.format(SUCCESS_MESSAGE, testData.getValue().toString()));
            return Result.SUCCESS;
        }

    }
}

