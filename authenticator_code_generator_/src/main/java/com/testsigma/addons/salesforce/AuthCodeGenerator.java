package com.testsigma.addons.salesforce;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.SalesforceAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.openqa.selenium.NoSuchElementException;


@Data
@Action(actionText = "Generate TOTP using secretkey and store it into a runtime variable testdata",
        description = "Generates time based OTP using google authenticator",
        applicationType = ApplicationType.Salesforce)
public class AuthCodeGenerator extends SalesforceAction {
    @TestData(reference = "secretkey")
    private com.testsigma.sdk.TestData secret;
    @TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        //Your Awesome code starts here
        logger.info("Initiating execution");


        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            Totp otpGenerator = new Totp(secret.getValue().toString());

            logger.debug(otpGenerator.toString());
            String newOtp = otpGenerator.now();
            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(newOtp);
            logger.info(runTimeData.getKey().toString() + " " + runTimeData.getValue().toString());
            setSuccessMessage("The TOTP is " + newOtp + " " + "and has been assigned to runtime variable "
                    + testData.getValue().toString());

        } catch (Exception error) {
            logger.debug(error.getMessage() + error.getCause());
            logger.info("stack trace : " + ExceptionUtils.getStackTrace(error));
            setErrorMessage("Operation Failed.Please check the logs for more info");

        }
        return result;
    }

}
