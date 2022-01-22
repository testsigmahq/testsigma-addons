package com.testsigma.addons.string_utils.generators;

import com.mifmif.common.regex.Generex;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "generateRandomString(regex)",
        description = "Generate a string with random characters that match the provided regular expression")
public class GenerateRandomStringWithRegex extends TestDataFunction {

    @TestDataFunctionParameter(reference = "regex")
    private com.testsigma.sdk.TestDataParameter regex;

    public TestData generate() throws Exception {
        Generex generex = new Generex(regex.getValue().toString());
        String randomString = generex.random();
        System.out.println(randomString);
        return new TestData(randomString);
    }
}