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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private static final long COMMAND_TIMEOUT_SECONDS = 120; // Set timeout to 120 seconds


    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        // Your Awesome code starts here
        logger.info("Initiating execution");
        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        String output = null; // Initialize output to null
        try {
            String command = commandToExecute.getValue().toString();
            CommandResult commandResult = executeCommandWithTimeout(command, COMMAND_TIMEOUT_SECONDS); // Execute with timeout
            output = commandResult.getOutput(); // Get combined output (std + err)

            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setKey(variableName.getValue().toString());
            runTimeData.setValue(output);  // Store output in runtime variable

            logger.info("Output: " + output);

            // Construct the success message based on the actual output
            String message = "Command executed successfully and output is stored in variable: " + variableName.getValue().toString();
            if (output != null && !output.isEmpty()) {
                message += "='" + output.trim() + "'"; // Add ' ' to the value
            } else {
                message += " ";
            }
            setSuccessMessage(message); //Always Set Success Message even the output is empty


        } catch (TimeoutException e) {
            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Command execution timed out after " + COMMAND_TIMEOUT_SECONDS + " seconds.");
            logger.warn("Command execution timed out: " + ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            String errorMessage = ExceptionUtils.getStackTrace(e);
            result = com.testsigma.sdk.Result.FAILED; // Keep failure as fallback
            setErrorMessage("Unable to execute command, please check Additional logs for more info: " + e.getMessage());
            logger.warn(errorMessage); // Use logger.error for full stack traces
        } finally {
            if (runTimeData == null) {
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setKey(variableName.getValue().toString());
                runTimeData.setValue(""); // If an exception occurs and the runTimeData object is null, initialize the value of the runtime variable with a blank string
            }

        }
        return result;
    }


    private CommandResult executeCommandWithTimeout(String command, long timeoutSeconds) throws Exception, TimeoutException {
        CompletableFuture<CommandResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                return executeCommand(command); // Your original executeCommand method
            } catch (Exception e) {
                // Wrap the exception so it can be handled in the main thread.
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException e) {
            // Unwrap the exception thrown by executeCommand
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause; // Re-throw the original exception
            } else {
                throw new Exception("Error during command execution", cause);
            }

        } catch (TimeoutException e) {
            future.cancel(true); // Interrupt the process if it's still running
            throw e;
        }
    }


    private CommandResult executeCommand(String command) throws Exception {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        int exitCode = -1; // Initialize with a default value
        String os = System.getProperty("os.name").toLowerCase();
        logger.info("OS Name: " + os);

        // Adjust the command for different operating systems
        String shell = "/bin/bash"; // Default shell
        String shellArg = "-c"; // Default shell argument

        if (os.contains("win")) {
            shell = "cmd";
            shellArg = "/c";
        }


        Process process = null;
        try {
            // Log current working directory
            logger.info("Current working directory: " + new java.io.File(".").getAbsolutePath());

            // Use ProcessBuilder to execute the command with shell
            List<String> fullCommand = new ArrayList<>();
            fullCommand.add(shell);
            fullCommand.add(shellArg);
            fullCommand.add(command);  // Pass the entire original command string.
            logger.info("Executing command: " + fullCommand);
            ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);

            process = processBuilder.start();

            // Capture the output using BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Capture the error stream
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
            }

            // Wait for the process to complete and log the exit code
            exitCode = process.waitFor();
            logger.info("Process exit code: " + exitCode);

            logger.warn("Error stream: " + errorOutput.toString());

        } catch (IOException | InterruptedException e) {
            logger.info("Error executing command: " + e.getMessage());
            output.append("\nException during command execution: ").append(e.getMessage());
            errorOutput.append("\nException during command execution: ").append(e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        // Combine standard output and error output
        String combinedOutput = output.toString() + "\n" + errorOutput.toString();

        return new CommandResult(combinedOutput, exitCode);
    }

    // Helper class to store both the output and the exit code
    @Data
    private static class CommandResult {
        private final String output;
        private final int exitCode;
    }
}