package com.testsigma.addons.restapi;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.StepActionType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Get the testdata value and character to be replaced, remove the special character and store it in testdata response",
        description = "This addon is to remove the special characters from the string and store it in a variable",
        applicationType = ApplicationType.REST_API,
        actionType = StepActionType.NONE)

public class RemoveSpecialCharacters extends RestApiAction {
    @TestData(reference = "value")
    private com.testsigma.sdk.TestData input1;
    @TestData(reference = "character")
    private com.testsigma.sdk.TestData input2;
    @TestData(reference = "response", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData response;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;
    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");
        String inputString = input1.getValue().toString();
        // The variable "inputString" will have the string obtained from the testdata( variable) passed
        inputString= inputString.replace(input2.getValue().toString(), "");
        runTimeData = new com.testsigma.sdk.RunTimeData();
        runTimeData.setValue(inputString);
        runTimeData.setKey(response.getValue().toString());

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        setSuccessMessage("The extracted string is  " + inputString);

        return result;

    }

}
