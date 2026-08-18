package com.testsigma.addons.web;

import com.testsigma.addons.web.util.Utilities;
import com.testsigma.addons.web.util.UtilitiesFactory;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
@Action(actionText = "Get latest downloaded file path and store in runtime variable variable-name",
        description = "Retrieves the local path of the most recently downloaded file from the browser" +
                " (Chrome/Edge) and stores it in a runtime variable.",
        applicationType = ApplicationType.WEB)
public class GetLatestDownloadedFilePath extends WebAction {
    @TestData(reference = "variable-name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData runtimeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;
        Utilities utilities = UtilitiesFactory.create(driver, logger);
        try {
            logger.info("Initiated execution");
            File filePath = utilities.copyFileFromDownloads();

            logger.info("Local path" + filePath.getAbsolutePath());
            runTimeData.setKey(runtimeVariable.getValue().toString());
            runTimeData.setValue(filePath.getAbsolutePath());
            setSuccessMessage("Successfully stored the latest downloaded file path in the runtime variable. "
                    + runtimeVariable.getValue().toString() + " = " + filePath.getAbsolutePath());
        } catch (Exception e) {
            logger.info("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Exception Occurred while extracting the latest downloaded file path. Exception: "
                    + ExceptionUtils.getMessage(e));
            result = Result.FAILED;
        }
        return result;
    }

}