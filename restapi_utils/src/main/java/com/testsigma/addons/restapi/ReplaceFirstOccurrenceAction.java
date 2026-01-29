package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;


@Data
@Action(actionText = "Replace string1 with string2 in the given text string3 for the first occurrence and store into a runtime variable test data",
        description = "Replaces the first occurrence of string1 with string2 in string3 and stores the result in test data.",
        applicationType = ApplicationType.REST_API)

public class ReplaceFirstOccurrenceAction extends RestApiAction {

    @TestData(reference = "string1")
    private com.testsigma.sdk.TestData string1Data;  // The string to be replaced.

    @TestData(reference = "string2")
    private com.testsigma.sdk.TestData string2Data;  // The replacement string.

    @TestData(reference = "string3")
    private com.testsigma.sdk.TestData string3Data;// The base string where replacement will happen.

    @TestData(reference = "test data", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;

        try {
            String string1 = string1Data.getValue().toString();
            String string2 = string2Data.getValue().toString();
            String string3 = string3Data.getValue().toString();

            String replacedString = replaceFirst(string3, string1, string2);

            setSuccessMessage(String.format("Replaced first occurrence of '%s' with '%s' in '%s'. Result: '%s'",
                    string1, string2, string3, replacedString));
            runTimeData.setKey(var.getValue().toString());
            runTimeData.setValue(replacedString);

        } catch (Exception e) {
            logger.warn("Error during string replacement: " + e.getMessage() + e);
            setErrorMessage("An error occurred during string replacement: " + e.getMessage());
            result = Result.FAILED;
        }

        return result;
    }


    public String replaceFirst(String baseString, String stringToReplace, String replacementString) {
        int index = baseString.indexOf(stringToReplace);

        if (index == -1) {
            return baseString;
        }

        return baseString.substring(0, index) + replacementString + baseString.substring(index + stringToReplace.length());
    }


}