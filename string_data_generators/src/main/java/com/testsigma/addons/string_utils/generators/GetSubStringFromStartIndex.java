package com.testsigma.addons.string_utils.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "getSubstring(startIndex, string)",
        description = "Generates the starting index of the substring")
public class GetSubStringFromStartIndex extends TestDataFunction {

    @TestDataFunctionParameter(reference = "startIndex")
    private com.testsigma.sdk.TestDataParameter startIndex;
    @TestDataFunctionParameter(reference = "string")
    private com.testsigma.sdk.TestDataParameter string;

    public TestData generate() throws Exception {
        String actualString = string.getValue().toString();
        int fromIndex = Integer.valueOf(startIndex.getValue().toString());
        String substring = actualString.substring(fromIndex);
        return new TestData(substring);
    }
}