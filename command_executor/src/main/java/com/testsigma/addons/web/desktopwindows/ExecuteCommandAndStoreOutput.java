package com.testsigma.addons.web.desktopwindows;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Action(actionText = "Execute command and store output in a variable. Command to execute: Executable-Command , variable name: Output-Variable",
        description = "Execute the command and store the output in a variable.",
        applicationType = ApplicationType.WINDOWS)
public class ExecuteCommandAndStoreOutput extends WindowsAction {

    @TestData(reference = "Executable-Command")
    private com.testsigma.sdk.TestData commandToExecute;

    @TestData(reference = "Output-Variable")
    private com.testsigma.sdk.TestData variableName;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    private static final long EXECUTION_TIMEOUT_SECONDS = 10; // Define a timeout value (e.g., 10 seconds)

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        // Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        String output = null; // Initialize output to null
        try {
            String command = commandToExecute.getValue().toString();
            output = executeCommand(command); // Assign output from executeCommand
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(output);  // Store potentially null output

            logger.info("Output: " + output);

            // Construct the success message based on the actual output
            String message = "Command executed successfully and output is stored in variable: " + variableName.getValue().toString();
            if (output != null && !output.isEmpty()) {
                message += "='" + output.trim() + "'"; //Add ' ' to the value
            } else {
                message += " ";
            }
            setSuccessMessage(message);


        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Unable to execute command, please check Additional logs for more info: " + e.getMessage());
            logger.warn(errorMessage); // Use logger.error for full stack traces
        } finally {
            if (runTimeData == null) {
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue("");
            }

        }
        return result;
    }

    private String executeCommand(String command) throws Exception {
        StringBuilder output = new StringBuilder();
        String os = System.getProperty("os.name").toLowerCase();
        logger.info("OS Name: " + os);

        List<String> commandList = new ArrayList<>();
        if (os.contains("win")) {
            commandList.addAll(Arrays.asList("cmd", "/c"));
            commandList.add(command);
        } else {
            commandList.addAll(Arrays.asList("/bin/bash", "-c"));
            commandList.add(command);
        }

        Process process = null;
        try {
            logger.info("Executing command: " + commandList);
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);

            // Redirect error stream to the input stream - helpful for debugging.
            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();

            // Use a BufferedReader to read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Start a thread to read the output from the process
            Thread readerThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator()); // Use platform-specific line separator
                    }
                } catch (IOException e) {
                    logger.warn("Error reading process output: " + e.getMessage());
                }
            });
            readerThread.start();

            // Wait for the process to complete or timeout
            boolean completed = process.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            readerThread.join(100); // Try to join the reader thread for a short duration.
            if (!completed) {
                logger.warn("Command execution timed out after " + EXECUTION_TIMEOUT_SECONDS + " seconds. Killing the process.");
                process.destroy();
                return ""; // Return empty string instead of null
            }

            int exitCode = process.exitValue();
            logger.info("Command exited with code: " + exitCode);

            if (exitCode != 0) {
                logger.warn("Command execution may have failed. Check output for errors.");
            }

            // If the process completed, wait for the reader thread to finish processing all output
            readerThread.join(); // Ensures all output is read if the process completes within the timeout


        } catch (IOException | InterruptedException e) {
            logger.warn("Error executing command: " + e.getMessage() + e); // Log the exception with stack trace
            if (process != null)
                process.destroy();
            throw e;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        if (output.length() == 0) {
            return ""; // Return empty string instead of null
        }
        return output.toString();
    }
}