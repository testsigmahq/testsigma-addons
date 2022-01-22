package com.testsigma.addons.string_utils.generators.test;

import com.testsigma.addons.string_utils.generators.GenerateRandomStringWithRegex;
import com.testsigma.sdk.TestData;
import com.testsigma.sdk.TestDataParameter;
import com.testsigma.sdk.runners.TestDataFunctionRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class TestCustomTestDataFunction {
    private TestDataFunctionRunner testDataFunctionActionRunner;

    @BeforeClass
    public void setup() throws Exception {
        testDataFunctionActionRunner = new TestDataFunctionRunner();
    }

    @Test
    public void generateRandomNumberInRange() throws Exception {
        GenerateRandomStringWithRegex testDataFunction = new GenerateRandomStringWithRegex();
        testDataFunction.setRegex(new TestDataParameter("[0-3](([a-c]|[e-g]){2})"));
        TestData testData = testDataFunctionActionRunner.run(testDataFunction);
    }

    @AfterClass
    public void teardown() {
    }
}
