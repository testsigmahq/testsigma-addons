package com.testsigma.addons.web;

import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

import java.util.Random;

@Data
@Action(actionText = "Generate Random 4 digit ping where first 2 digits are consecutive and store the generated value in runtime var1",
        description = "Executes JS and stores the value in runtime variable",
        applicationType = ApplicationType.WEB)
public class RandomDataGeneration extends WebAction {



    @TestData(reference = "var1")
    private com.testsigma.sdk.TestData variable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");



        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            Random random = new Random();

            // Generate random first two digits ensuring they are consecutive
            int firstDigit = random.nextInt(9); // First digit can be from 0 to 9
            int secondDigit = firstDigit + 1; // Ensure second digit is consecutive

            // Generate random third and fourth digits
            int thirdDigit = random.nextInt(10);
            int fourthDigit = random.nextInt(10);

            // Combine digits to form the PIN
            String pin = String.format("%d%d%d%d", firstDigit, secondDigit, thirdDigit, fourthDigit);

            runTimeData.setKey(variable.getValue().toString());
            runTimeData.setValue(pin);
            setSuccessMessage("Successfully stored the pin :: "+pin+" into a runtime variable "+runTimeData);
        }

        catch(Exception e) {

            logger.debug(e.getMessage());
            setErrorMessage("Failed to perform operation "+e.getMessage()+e.getCause());
        }

        return result;
    }
}
