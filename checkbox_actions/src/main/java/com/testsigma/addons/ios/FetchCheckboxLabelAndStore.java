
package com.testsigma.addons.ios;

import com.testsigma.addons.utils.CheckBoxLabelFetcher;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import io.appium.java_client.ios.IOSDriver;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Fetch all checkboxes on the page and store the label in runtime variable runtime-variable in comma seperated format",
        description = "Fetch all checkboxes on the page and store the label in runtime variable runtime-variable in comma seperated format",
        applicationType = ApplicationType.IOS)
public class FetchCheckboxLabelAndStore extends IOSAction {

    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        try {
            String variableName = runtimeVariable.getValue().toString();
            logger.info("Starting to fetch checkbox labels on iOS platform and store in variable: " + variableName);

            IOSDriver iosDriver = (IOSDriver) this.driver;
            String labels = new CheckBoxLabelFetcher().getAllCheckboxLabels(iosDriver, logger);

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variableName);
            runTimeData.setValue(labels);

            logger.info("Successfully stored the checkbox labels = " + labels + " in runtime variable " + variableName + " on iOS platform");
            setSuccessMessage("Successfully stored the checkbox labels in runtime variable : " + variableName + " = " + labels);
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while fetching checkbox labels and storing in runtime variable: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in FetchCheckboxLabelAndStore iOS action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

