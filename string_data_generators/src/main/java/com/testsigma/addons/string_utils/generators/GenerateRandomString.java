package com.testsigma.addons.string_utils.generators;

import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataFunction;
import com.testsigma.sdk.annotation.TestDataFunctionParameter;
import lombok.Data;

@Data
@com.testsigma.sdk.annotation.TestDataFunction(displayName = "generateRandomString(length)",
        description = "This action will generate a random string of given length")
public class GenerateRandomString extends TestDataFunction {

    @TestDataFunctionParameter(reference = "length")
    private com.testsigma.sdk.TestDataParameter length;

    public TestData generate() throws Exception {
        String list = "abcdefghijklmnopqrstuvwxyz0123456789";
        int stringLength = Integer.valueOf(length.getValue().toString());
        String randomString = "";
        for (int i = 0; i < stringLength; i++) {
            int rnum = (int) Math.floor(Math.random() * list.length());
            randomString += list.substring(rnum, rnum + 1);
        }
        return new TestData(randomString);
    }
}