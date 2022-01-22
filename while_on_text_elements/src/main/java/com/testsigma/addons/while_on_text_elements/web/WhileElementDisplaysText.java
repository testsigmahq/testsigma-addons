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
@Action(actionText = "While the element displays text",
        actionType = StepActionType.WHILE_LOOP,
        description = "Performs actions while the element displays the provided text",
        applicationType = ApplicationType.WEB)
public class WhileElementDisplaysText extends WebAction {

    private static final String SUCCESS_MESSAGE = "The Element displays the Expected text";
    private static final String ERROR_MESSAGE = "Expected text is not found";
    private static final String DISPLAY_NONE_ERROR_MESSAGE = "The Element is not displayed";
    private static final String VISIBILITY_NONE_ERROR_MESSAGE = "The Element is not visible";
    @TestData(reference = "text")
    private com.testsigma.sdk.TestData testData;
    @Element(reference = "element")
    private com.testsigma.sdk.Element element;
    private List<WebElement> elements;

    @Override
    public Result execute() throws NoSuchElementException {
        try {
            String elementText = element.getElement().getText();
            WebElement webElement = element.getElement();
            if (elementText != null && elementText.equals(testData.getValue().toString())) {
                String displayValue = webElement.getCssValue("display");
                if (!displayValue.equals("none")) {
                    try {
                        String visibility = webElement.getCssValue("visibility");
                        if (!(visibility.equals("collapse") || visibility.equals("hidden"))) {
                            setSuccessMessage(SUCCESS_MESSAGE);
                            return Result.SUCCESS;
                        } else {
                            setErrorMessage(VISIBILITY_NONE_ERROR_MESSAGE);
                            return Result.FAILED;
                        }
                    } catch (Exception e) {
                        logger.info("Ignore this error," + e);
                        setSuccessMessage(SUCCESS_MESSAGE);
                        return Result.SUCCESS;
                    }
                } else {
                    setErrorMessage(DISPLAY_NONE_ERROR_MESSAGE);
                    return Result.FAILED;
                }
            } else {
                setErrorMessage(ERROR_MESSAGE);
                return Result.FAILED;
            }
        }catch (Exception exception){
            exception.printStackTrace();
            setErrorMessage(ERROR_MESSAGE);
            return Result.FAILED;
        }

    }

}

