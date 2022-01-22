package com.testsigma.addons.string_utils.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "getSubstring(startIndex,  endIndex, string)",
        description = "Generates the substring from a string between the start and end indexes")
public class GetSubStringFromStartIndexToEndIndex extends TestDataFunction {

    @TestDataFunctionParameter(reference = "startIndex")
    private com.testsigma.sdk.TestDataParameter startIndex;
    @TestDataFunctionParameter(reference = "endIndex")
    private com.testsigma.sdk.TestDataParameter endIndex;
    @TestDataFunctionParameter(reference = "string")
    private com.testsigma.sdk.TestDataParameter string;

    public TestData generate() throws Exception {
        String actualString = string.getValue().toString();
        int fromIndex = Integer.valueOf(startIndex.getValue().toString());
        int tillIndex = Integer.valueOf(startIndex.getValue().toString());
        String substring = actualString.substring(fromIndex, tillIndex);
        return new TestData(substring);
    }
}