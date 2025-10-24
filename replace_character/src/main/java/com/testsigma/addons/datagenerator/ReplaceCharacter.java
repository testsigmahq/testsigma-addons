package com.testsigma.addons.datagenerator;

import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.*;
import lombok.Data;
import com.testsigma.sdk.TestData;
import org.apache.commons.lang3.exception.ExceptionUtils;


@Data
@TestDataFunction(displayName = "Replace an existingcharacter with newcharacter in testdata",
        description = "It replaces an existing character with new character in provided testdata")
public class ReplaceCharacter extends com.testsigma.sdk.TestDataFunction {

    @TestDataFunctionParameter(reference = "existingcharacter")
    private com.testsigma.sdk.TestData character1;
    @TestDataFunctionParameter(reference = "newcharacter")
    private com.testsigma.sdk.TestData character2;
    @TestDataFunctionParameter(reference = "testdata")
    private com.testsigma.sdk.TestData providedData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public TestData generate() throws Exception {

        logger.info("Initiating execution");
        String existingcharacter = character1.getValue().toString();
        String newcharacter = character2.getValue().toString();
        String testdata = providedData.getValue().toString();
        
        try {
            logger.info("existing character: " + existingcharacter);
            logger.info("new character: " + newcharacter);
            logger.info("provided testdata: " + testdata);
            if (!testdata.contains(existingcharacter)) {
                setErrorMessage("Existing character '" + existingcharacter + "' not found in the provided testdata.");
                logger.info("Existing character '" + existingcharacter + "' not found in the provided testdata.");
                throw new Exception("Existing character not found in the provided testdata.");
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
            TestData testData = new TestData(latestword);
            logger.info("Successfully stored in a variable '" + runTimeData.getKey() + "'");
            return testData;


        } catch (Exception e) {
            setErrorMessage("An error occurred: " + ExceptionUtils.getStackTrace(e));
        throw new Exception("An error occurred during character replacement." + ExceptionUtils.getStackTrace(e));
        }
    }
}