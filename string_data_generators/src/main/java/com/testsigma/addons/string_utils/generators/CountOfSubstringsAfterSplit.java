package com.testsigma.addons.string_utils.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "countOfSubstrings(splitChar)",
        description = "Splits a string into substrings based on specified delimiting characters and counts the number of substrings obtained")
public class CountOfSubstringsAfterSplit extends TestDataFunction {

    @TestDataFunctionParameter(reference = "string")
    private com.testsigma.sdk.TestDataParameter string;
    @TestDataFunctionParameter(reference = "splitChar")
    private com.testsigma.sdk.TestDataParameter splitChar;

    public TestData generate() throws Exception {
        String actualString = string.getValue().toString();
        String[] substringArray = actualString.split(splitChar.getValue().toString());
        return new TestData(substringArray.length);
    }
}