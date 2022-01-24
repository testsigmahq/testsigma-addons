package com.testsigma.addons.string_utils.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "trim(string)",
        description = "Returns new string after removing any leading and trailing whitespace")
public class Trim extends TestDataFunction {

    @TestDataFunctionParameter(reference = "string")
    private com.testsigma.sdk.TestDataParameter string;

    public TestData generate() throws Exception {
        return new TestData(string.getValue().toString().trim());
    }
}