package com.testsigma.addons.mobileweb;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Data
@Action(
        actionText = "Encode testdata into URL encoded format and store into runtime variable variable-name",
        description = "Encodes the given input into a URL-safe (UTF-8) encoded format and stores it in a runtime variable",
        applicationType = ApplicationType.MOBILE_WEB
)
public class UrlEncodeAddon extends WebAction {

    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData input;

    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() {
        Result result = Result.SUCCESS;
        logger.info("Inititating execution.....");
        try {
            String value = input.getValue().toString();
            logger.info("Test data is: " + value);
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

            logger.info("Encoded URL: " + encoded);

            runTimeData.setValue(encoded);
            runTimeData.setKey(variableName.getValue().toString());
            setSuccessMessage("Successfully encoded value: " + encoded);

        } catch (Exception e) {
            result = Result.FAILED;
            logger.warn("Failed to encode value due to exception: "
                    + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to encode value due to exception: "
                    + ExceptionUtils.getMessage(e));
        }

        return result;
    }
}
