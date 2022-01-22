package com.testsigma.addons.string_utils.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "insertStringAfter(index, string, insertedString)",
        description = "This action will insert a string in a given index")
//This addon provides string utilities that can be use to manipulate strings in various ways.
public class InsertStringAfter extends TestDataFunction {

    @TestDataFunctionParameter(reference = "index")
    private com.testsigma.sdk.TestDataParameter index;
    @TestDataFunctionParameter(reference = "string")
    private com.testsigma.sdk.TestDataParameter string;
    @TestDataFunctionParameter(reference = "insertedString")
    private com.testsigma.sdk.TestDataParameter insertedString;

    public TestData generate() throws Exception {
        String actualString = string.getValue().toString();
        String stringToBeInserted = insertedString.getValue().toString();
        int index = Integer.valueOf(getIndex().getValue().toString());
        StringBuffer alteredString = new StringBuffer(actualString);
        alteredString.insert(index + 1, stringToBeInserted);
        TestData testData = new TestData(alteredString);
        return testData;
    }
}