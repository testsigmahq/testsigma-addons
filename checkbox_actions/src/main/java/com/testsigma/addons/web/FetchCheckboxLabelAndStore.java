
package com.testsigma.addons.web;

import com.testsigma.addons.utils.CheckBoxLabelFetcher;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Data
@Action(actionText = "Fetch all checkboxes on the page and store the label in runtime variable runtime-variable in comma seperated format",
        description = "Fetch all checkboxes on the page and store the label in runtime variable runtime-variable in comma seperated format",
        applicationType = ApplicationType.WEB)
public class FetchCheckboxLabelAndStore extends WebAction {

    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        try {
            String variableName = runtimeVariable.getValue().toString();
            logger.info("Starting to fetch checkbox labels on Web platform and store in variable: " + variableName);
            
            String labels = new CheckBoxLabelFetcher().getAllCheckboxLabels(driver, logger);

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variableName);
            runTimeData.setValue(labels);

            logger.info("Successfully stored the checkbox labels = " + labels + " in runtime variable " + variableName + " on Web platform");
            setSuccessMessage("Successfully stored the checkbox labels in runtime variable : " + variableName + " = " + labels);
            return Result.SUCCESS;
        } catch (Exception e) {
            String errorMessage = "An error occurred while fetching checkbox labels and storing in runtime variable: " + e.getMessage();
            setErrorMessage(errorMessage);
            logger.info("Error in FetchCheckboxLabelAndStore Web action: " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}

