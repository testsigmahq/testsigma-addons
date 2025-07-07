package com.testsigma.addons.ios;

import com.testsigma.addons.ExecutePythonProcess;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;


@Data
@Action(actionText = "execute python script from file file-path using python executable path executable-path " +
        "and store the output in runtime variable variable_name",
        description = "This action executes a Python script from a file and stores the output in a runtime variable." +
                "(Note: make sure the Python script is executable and the Python interpreter is correctly set up.)",
        applicationType = ApplicationType.IOS)
public class ExecutePythonFile extends IOSAction {

    @TestData(reference = "file-path")
    private com.testsigma.sdk.TestData filePath;
    @TestData(reference = "executable-path")
    private com.testsigma.sdk.TestData executablePath;
    @TestData(reference = "variable_name", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        try {
            // Assuming the Python script is executed here
            String pythonOutput = ExecutePythonProcess.executePythonScript(
                    executablePath.getValue().toString(),
                    filePath.getValue().toString()
            );
            String scriptOutput = pythonOutput.split("\\$delimiter\\$")[0].trim();
            String pythonError = pythonOutput.split("\\$delimiter\\$").length > 1 ?
                    pythonOutput.split("\\$delimiter\\$")[1].trim() : "";
            logger.debug("Python script output: " + scriptOutput);
            logger.debug("Python script error: " + pythonError);
            if (!pythonError.isEmpty()) {
                logger.info("Python script executed with errors: " + pythonError);
                setErrorMessage("Python script executed with errors: " + pythonError);
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(pythonError);
                result = com.testsigma.sdk.Result.FAILED;
            } else {
                logger.info("Python script executed successfully. Output: " + scriptOutput);
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(scriptOutput);
                setSuccessMessage("Successfully executed the python script and stored the output in runtime variable" +
                        runTimeData.getKey());
            }
        } catch (Exception e) {
            logger.debug("Error executing Python script: " + e.getMessage());
            result = com.testsigma.sdk.Result.FAILED;
        }
        return result;
    }
}
