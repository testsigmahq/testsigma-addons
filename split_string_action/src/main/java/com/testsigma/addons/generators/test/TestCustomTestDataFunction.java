package com.testsigma.addons.generators.test;

import com.testsigma.sdk.TestDataParameter;
import com.testsigma.sdk.runners.TestDataFunctionRunner;
import com.testsigma.addons.generators.RandomNumber;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.testsigma.sdk.TestData;


public class TestCustomTestDataFunction {
    private TestDataFunctionRunner testDataFunctionRunner;

    @BeforeClass
    public void setup() throws Exception {
        testDataFunctionRunner = new TestDataFunctionRunner();
    }

    @Test
    public void generateRandomNumberInRange() throws Exception {
        RandomNumber testDataFunction = new RandomNumber();
        testDataFunction.setMin(new TestDataParameter(5));
        testDataFunction.setMax(new TestDataParameter(100));
        TestData testData = testDataFunctionRunner.run(testDataFunction);
        Assert.assertTrue((Integer)testData.getValue() > 5);
        Assert.assertTrue((Integer)testData.getValue() < 10);
    }

    @AfterClass
    public void teardown() {
    }
}
