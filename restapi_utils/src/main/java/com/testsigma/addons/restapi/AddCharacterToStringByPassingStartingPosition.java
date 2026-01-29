package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

@Data
@Action(actionText = "Add character test-data1 to the String test-data2 from start position ( Start ) and store into testdata", 
        description = "Adds a specified character at a specified position in a given string and stores the result in runtime data", 
        applicationType = ApplicationType.REST_API, useCustomScreenshot = false)
public class AddCharacterToStringByPassingStartingPosition extends RestApiAction {

    @TestData(reference = "test-data2")
    private com.testsigma.sdk.TestData inputString;
    
    @TestData(reference = "test-data1")
    private com.testsigma.sdk.TestData charToAdd;
    
    @TestData(reference = "Start")
    private com.testsigma.sdk.TestData position;
    
    @TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData outputString;
    
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        logger.info("Initiating execution");
        logger.debug("Input String: " + this.inputString.getValue() 
                   + ", Character to Add: " + this.charToAdd.getValue() 
                   + ", Position: " + this.position.getValue());

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

        try {
            String input = inputString.getValue().toString();
            String character = charToAdd.getValue().toString();
            int pos = Integer.parseInt(position.getValue().toString());

            if (pos < 0 || pos > input.length()) {
                throw new IndexOutOfBoundsException("Position is out of bounds.");
            }

            StringBuilder modifiedString = new StringBuilder(input);
            modifiedString.insert(pos, character);

            String resultString = modifiedString.toString();
            runTimeData.setValue(resultString);
            runTimeData.setKey(outputString.getValue().toString());
            runTimeData.setKey(outputString.getValue().toString());
            setSuccessMessage("Added character '" + character + "' at position " + pos 
                            + " in string and stored the result: " + resultString);

        } catch (IndexOutOfBoundsException e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("Position out of bounds: " + position.getValue() + " " + e);
            setErrorMessage("Position out of bosunds: " + position.getValue());
        } catch (Exception e) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.info("An error occurred while adding character to string" + " " + e);
            setErrorMessage("An error occurred while adding character to string: " + e.getMessage());
        }

        return result;
    }
}
