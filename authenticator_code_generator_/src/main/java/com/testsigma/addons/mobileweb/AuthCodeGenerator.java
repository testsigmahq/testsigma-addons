package com.testsigma.addons.mobileweb;

import com.testsigma.addons.web.GoogleAuthGenerator;
import com.testsigma.sdk.ApplicationType;
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
        applicationType = ApplicationType.MOBILE_WEB)
public class AuthCodeGenerator extends GoogleAuthGenerator {

    @TestData(reference = "secretkey")
    private com.testsigma.sdk.TestData secret;
    @TestData(reference = "testdata", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData testData;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");


        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            Totp otpgenerator = new Totp(secret.getValue().toString());

            logger.debug(otpgenerator.toString());
            String newotp = otpgenerator.now();
            runTimeData.setKey(testData.getValue().toString());
            runTimeData.setValue(newotp.toString());
            logger.info(runTimeData.getKey().toString() + " " + runTimeData.getValue().toString());
            setSuccessMessage("The TOTP is " + newotp + " " + "and has been assigned to runtime variable "
                    + testData.getValue().toString());

        } catch (Exception error) {

            logger.info("stack trace : " + ExceptionUtils.getStackTrace(error));
            logger.debug(error.getMessage() + error.getCause());
            setErrorMessage("Operation Failed.Please check the logs for more infor");


        }
        return result;
    }
}
