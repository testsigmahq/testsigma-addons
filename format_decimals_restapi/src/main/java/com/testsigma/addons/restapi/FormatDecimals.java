package com.testsigma.addons.restapi;

import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.RunTimeData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.text.DecimalFormat;

@Data
@Action(actionText = "Format decimal testdata in testdata2 and store in runtimevariable",
        description = "Format decimal test data in test data and store in runtimevariable",
        applicationType = ApplicationType.REST_API,
        useCustomScreenshot = false)

public class FormatDecimals extends RestApiAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "testdata2")
    private com.testsigma.sdk.TestData testData2;
    @TestData(reference = "runtimevariable" , isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runTimeVar;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");
        logger.debug("test-data: " + this.runTimeVar.getValue());
        String formattedValue = "";
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            double num = Double.parseDouble(testData.getValue().toString());
            DecimalFormat df = new DecimalFormat(testData2.getValue().toString());
            formattedValue = df.format(num);
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(runTimeVar.getValue().toString());
            runTimeData.setValue(formattedValue);
            logger.info("Decimal formatted Successfully and stored in the runtime variable :" + runTimeVar.getValue() + " = " + formattedValue);
        } catch (Exception error) {
            result = com.testsigma.sdk.Result.FAILED;
            logger.warn("Please check the numeric value or decimal format passed for this step. Exception: " + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Formatting decimal value failed. Exception: " + ExceptionUtils.getMessage(error));
        }
        setSuccessMessage("Decimal formatted Successfully and stored in the runtime variable :" + runTimeVar.getValue() + " = " + formattedValue);
        return result;
    }
}
