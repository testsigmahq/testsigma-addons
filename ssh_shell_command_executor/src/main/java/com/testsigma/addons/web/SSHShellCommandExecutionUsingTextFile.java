package com.testsigma.addons.web;

import com.jcraft.jsch.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

@Data
@Action(
        actionText = "SSH Shell: Execute commands from  text file File-Path(.txt) on Host Host-Name port Port-Number Username User-Name Password User-Password and store output in Variable-Name",
        description = "Executes SSH shell commands from a text file. Supports both file path and URL.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false
)
public class SSHShellCommandExecutionUsingTextFile extends WebAction {

    @TestData(reference = "Host-Name")
    private com.testsigma.sdk.TestData hostName;

    @TestData(reference = "Port-Number")
    private com.testsigma.sdk.TestData portNumber;

    @TestData(reference = "User-Name")
    private com.testsigma.sdk.TestData userName;

    @TestData(reference = "User-Password")
    private com.testsigma.sdk.TestData userPassword;

    @TestData(reference = "File-Path")
    private com.testsigma.sdk.TestData filePath;

    @TestData(reference = "Variable-Name")
    private com.testsigma.sdk.TestData storeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() throws NoSuchElementException {
        String user = userName.getValue().toString();
        String host = hostName.getValue().toString();
        String password = userPassword.getValue().toString();
        int port = Integer.parseInt(portNumber.getValue().toString());
        String path = filePath.getValue().toString();

        try {
            File commandFile = urlToFileConverter("commands.txt", path);
            List<String> commandList = Files.readAllLines(commandFile.toPath());
            return executeShellMode(user, host, password, port, commandList);

        } catch (Exception e) {
            logger.warn("Exception Occurred: " + ExceptionUtils.getStackTrace(e));
            setErrorMessage("Failed to load command file: " + ExceptionUtils.getMessage(e));
            return Result.FAILED;
        }
    }

    private Result executeShellMode(String user, String host, String password, int port, List<String> commands) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelShell channel = null;
        InputStream in = null;
        java.io.OutputStream out = null;
        StringBuilder formattedOutput = new StringBuilder();

        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);

            session.connect(30000);

            channel = (ChannelShell) session.openChannel("shell");
            in = channel.getInputStream();
            out = channel.getOutputStream();
            channel.connect();

            Thread.sleep(2000);
            // Clear initial connection output
            StringBuilder tempBuffer = new StringBuilder();
            readAvailableOutput(in, tempBuffer, true);

            // Start with variable name
            formattedOutput.append("----------------------------------------\n");

            int commandNumber = 1;
            int executedCount = 0;

            for (String cmd : commands) {
                if (cmd.trim().isEmpty()) continue;

                // Command header
                formattedOutput.append("Command").append(commandNumber).append(":-\n");
                formattedOutput.append(cmd).append("\n");
                formattedOutput.append("\n");
                formattedOutput.append("Output of command:-\n");

                // Execute command
                out.write((cmd + "\n").getBytes());
                out.flush();

                Thread.sleep(2000);
                
                // Read output for this command
                StringBuilder commandOutput = new StringBuilder();
                readAvailableOutput(in, commandOutput, true);
                
                String output = commandOutput.toString().trim();
                
                // Clean up the output - remove the command echo and prompt
                output = cleanCommandOutput(output, cmd);
                
                if (output.isEmpty()) {
                    formattedOutput.append("#NoOutput\n");
                } else {
                    formattedOutput.append(output).append("\n");
                }

                formattedOutput.append("----------------------------------------\n");
                
                commandNumber++;
                executedCount++;
            }

            out.write("exit\n".getBytes());
            out.flush();

            Thread.sleep(1500);
            // Clear any remaining output
            tempBuffer.setLength(0);
            readAvailableOutput(in, tempBuffer, true);

            // Add summary
            formattedOutput.append("\nTotal commands executed: ").append(executedCount);

            String fullOutput = formattedOutput.toString();
            runTimeData.setKey(storeVariable.getValue().toString());
            runTimeData.setValue(fullOutput);

            setSuccessMessage("SSH commands executed successfully. Output stored.");
            return Result.SUCCESS;

        } catch (Exception e) {
            setErrorMessage(e.getMessage());
            return Result.FAILED;

        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (channel != null) channel.disconnect(); } catch (Exception ignored) {}
            try { if (session != null) session.disconnect(); } catch (Exception ignored) {}
        }
    }

    private String cleanCommandOutput(String output, String command) {
        if (output == null || output.isEmpty()) {
            return "";
        }

        output = stripAnsiCodes(output);

        output = output.replaceAll("(?m)^.*" + java.util.regex.Pattern.quote(command) + ".*$", "");
        output = output.replaceAll("(?m)^\\[.*@.*\\].*\\$.*$", "");
        output = output.replaceAll("(?m)^.*#.*$", "");
        
        // Clean up extra whitespace
        output = output.replaceAll("(?m)^\\s+$", "");
        output = output.trim();
        
        return output;
    }

    private void readAvailableOutput(InputStream in, StringBuilder output, boolean clean) throws IOException {
        byte[] buffer = new byte[2048];
        while (in.available() > 0) {
            int len = in.read(buffer);
            if (len <= 0) break;

            String raw = new String(buffer, 0, len);
            output.append(clean ? stripAnsiCodes(raw) : raw);
        }
    }

    private String stripAnsiCodes(String text) {
        if (text == null) return null;
        return text.replaceAll("\u001B\\[[\\d;]*[A-Za-z]", "")
                .replaceAll("\r", "")
                .replaceAll("\n\\s*\n\\s*\n", "\n\n");
    }

    public File urlToFileConverter(String fileName, String url) throws IOException {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            URL urlObject = new URL(url);
            File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
            FileUtils.copyURLToFile(urlObject, tempFile);
            return tempFile;
        } else {
            return new File(url);
        }
    }
}
