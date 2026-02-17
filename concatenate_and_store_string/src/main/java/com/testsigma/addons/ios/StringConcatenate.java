package com.testsigma.addons.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

@Data
@Action(actionText = "Concatenate Strings String1 & String2, store in runtime variable variable-name with special char testdata(Eg: none/space1/space2/@/$ etc)",
        description = "Concatenates Strings String1 & String2, store in runtime variable with special char (Eg: none/space1/space2/@/$ etc)" ,
        applicationType = ApplicationType.IOS)
public class StringConcatenate extends IOSAction {

    @TestData(reference = "String1")
    private com.testsigma.sdk.TestData testDataString1;
    @TestData(reference = "String2")
    private com.testsigma.sdk.TestData testDataString2;

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testDataString3;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;

        try {
            String string1 = testDataString1.getValue().toString();
            String string2 = testDataString2.getValue().toString();
            String strSpecChar = testDataString3.getValue().toString().toLowerCase().trim();
            String testdata3= "";
            String strSpace=" ";

            if (strSpecChar.contains("none")) {
                testdata3 = string1 + string2;
            } else if (strSpecChar.contains("space")) {
                Integer spaceCount = Integer.parseInt(strSpecChar.split("space")[1]);
                testdata3  = string1 + strSpace.repeat(spaceCount) + string2;
            } else {
                testdata3 = string1 + strSpecChar + string2;
            }
            runTimeData.setValue(testdata3);
            runTimeData.setKey(variableName.getValue().toString());

            logger.info("Successfully stored into RuntTime variable. " + variableName.getValue().toString() + " = " + testdata3);
            setSuccessMessage("Successfully stored into RuntTime variable. " + variableName.getValue().toString() + " = " + testdata3);
        } catch (Exception e) {
            logger.warn("Error while concatenating both strings :" + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Error while concatenating both strings :" + ExceptionUtils.getMessage(e));
            result= Result.FAILED;
        }

        return result;
    }
}