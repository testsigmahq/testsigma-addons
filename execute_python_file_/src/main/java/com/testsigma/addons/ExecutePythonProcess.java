package com.testsigma.addons;

import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


@Data
public class ExecutePythonProcess {

    public static String executePythonScript(String pythonExecutablePath, String scriptFilePath) {
        try {
            // Create the Python script file in temp directory
            File tempScript = createPythonScript(scriptFilePath);

            // Execute the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutablePath,
                    tempScript.getAbsolutePath());
            processBuilder.directory(tempScript.getParentFile());

            Process process = processBuilder.start();

            // Read output from Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder pythonErrorBuilder = new StringBuilder();
            // Read standard output
            while ((line = reader.readLine()) != null) {
                // logger.info("Python output: " + line);
                outputBuilder.append(line).append("\n");
            }

            // Read error output
            while ((line = errorReader.readLine()) != null) {
                // logger.info("Python error: " + line);
                pythonErrorBuilder.append(line).append("\n");
            }

            // Wait for process to complete
            boolean finished = process.waitFor(300, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                // logger.info("Python process timed out");
                throw new RuntimeException("Python process timed out waited for 5 minutes");
            }

            int exitCode = process.exitValue();
            // logger.info("Python process exit code: " + exitCode);

            // throw run time exception based on exit code
            if (exitCode != 0) {
                throw new RuntimeException("Python script failed with exit code: " + exitCode +
                        "\nError: " + pythonErrorBuilder.toString());
            }
            return outputBuilder.toString().trim() + "$delimiter$" + pythonErrorBuilder.toString().trim();
        } catch (IOException | InterruptedException e) {
            // logger.debug("Error executing Python script: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Error executing Python script: " + e.getMessage(), e);
        } catch (Exception e) {
            // logger.debug("error: " + ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("error: " + e.getMessage(), e);
        }
    }

    private static File createPythonScript(String scriptFilePath) throws IOException {
        File tempFile = File.createTempFile("motion_detection_", ".py");
        FileUtils.copyFile(new File(scriptFilePath), tempFile);
        tempFile.deleteOnExit();
        return tempFile;
    }
}

