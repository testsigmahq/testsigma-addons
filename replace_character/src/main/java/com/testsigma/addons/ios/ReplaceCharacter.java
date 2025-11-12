package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;


@Data
@Action(actionText = "Replace an existingcharacter with newcharacter in testdata and store in latestword",
        description = "It replaces an existing character with new character in provided testdata",
        applicationType = ApplicationType.IOS,
        useCustomScreenshot = false)
public class ReplaceCharacter extends IOSAction {

    @TestData(reference = "existingcharacter")
    private com.testsigma.sdk.TestData character1;
    @TestData(reference = "newcharacter")
    private com.testsigma.sdk.TestData character2;
    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData provideddata;
    @TestData(reference = "latestword", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData updatedword;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {

        logger.info("Initiating execution");
        String existingcharacter = character1.getValue().toString();
        String newcharacter = character2.getValue().toString();
        String testdata = provideddata.getValue().toString();

        Result result = Result.SUCCESS;

        try {
            logger.info("existing character: " + existingcharacter);
            logger.info("new character: " + newcharacter);
            logger.info("provided testdata: " + testdata);
            if (!testdata.contains(existingcharacter)) {
                setErrorMessage("Existing character '" + existingcharacter + "' not found in the provided testdata.");
                logger.info("Existing character '" + existingcharacter + "' not found in the provided testdata.");
                return Result.FAILED;
            }

            if (newcharacter.equals("\\")) {
                testdata = testdata.replace(existingcharacter, "\\");
            } else {
                testdata = testdata.replace(existingcharacter, newcharacter);
                logger.info("Replacement of character: " + existingcharacter + " with " + newcharacter);
            }

            setSuccessMessage("Updated word: " + testdata);
            logger.info("Final word: " + testdata);
            String latestword = testdata.toString();
            runTimeData.setValue(latestword);
            runTimeData.setKey(updatedword.getValue().toString());
            logger.info("Successfully stored in a variable '" + runTimeData.getKey() + "'");

        } catch (Exception e) {
            logger.info("An error occurred : " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("An error occurred: " + e.getMessage());
            result = Result.FAILED;
        }
        return result;
    }
}