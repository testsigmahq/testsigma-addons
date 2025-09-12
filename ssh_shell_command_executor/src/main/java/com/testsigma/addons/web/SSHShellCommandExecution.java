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

import java.io.IOException;
import java.io.InputStream;

@Data
@Action(actionText = "SSH Shell: Connect to SSH server and execute commands using interactive shell mode, SSH server details: Host: Host-Name, Port: Port-Number, UserName: User-Name, Password: User-Password, Commands: Terminal-Commands , Command Separator: Command-Separator , Store Output Variable: Variable-Name",
        description = "SSH Shell Command Execution - Executes commands on SSH servers using interactive shell mode with automatic ANSI code cleaning. Perfect for network devices that require CLI interaction.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SSHShellCommandExecution extends WebAction {

    @TestData(reference = "Host-Name")
    private com.testsigma.sdk.TestData hostName;
    @TestData(reference = "Port-Number")
    private com.testsigma.sdk.TestData portNumber;
    @TestData(reference = "User-Name")
    private com.testsigma.sdk.TestData userName;
    @TestData(reference = "User-Password")
    private com.testsigma.sdk.TestData userPassword;
    @TestData(reference = "Terminal-Commands")
    private com.testsigma.sdk.TestData commands;
    @TestData(reference = "Command-Separator")
    private com.testsigma.sdk.TestData commandSeparator;
    @TestData(reference = "Variable-Name")
    private com.testsigma.sdk.TestData storeVariable;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() throws NoSuchElementException {
        logger.info("Initiating execution");

        String user = userName.getValue().toString();
        String host = hostName.getValue().toString();
        String password = userPassword.getValue().toString();
        int port = Integer.parseInt(portNumber.getValue().toString());
        String commandSeparatorStr = commandSeparator != null && !commandSeparator.getValue().toString().isEmpty()
                ? commandSeparator.getValue().toString()
                : ";";

        String allCommands = formatCommands(commands.getValue().toString(), commandSeparatorStr);

        logger.info("Using shell mode for interactive command execution");

        return executeShellMode(user, host, password, port, allCommands);
    }


    private com.testsigma.sdk.Result executeShellMode(String user, String host, String password, int port, String allCommands) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelShell channel = null;
        InputStream in = null;
        java.io.OutputStream out = null;
        StringBuilder output = new StringBuilder();

        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);

            // Set connection timeout
            session.setTimeout(30000); // 30 seconds
            session.connect();
            logger.info("Session connected to " + host + ":" + port + " as user " + user);

            // Open shell channel for interactive commands
            channel = (ChannelShell) session.openChannel("shell");
            in = channel.getInputStream();
            out = channel.getOutputStream();
            channel.connect();
            logger.info("Shell channel connected");

            // Wait for initial prompt
            Thread.sleep(2000);
            readAvailableOutput(in, output, true); // Always clean output

            // Send commands
            String[] commandArray = allCommands.split(";");
            for (String cmd : commandArray) {
                if (cmd.trim().isEmpty()) continue;

                logger.info("Executing command: " + cmd.trim());
                out.write((cmd.trim() + "\n").getBytes());
                out.flush();

                // Wait for command to complete
                Thread.sleep(3000);
                readAvailableOutput(in, output, true); // Always clean output
            }

            // Send exit command to close shell gracefully
            out.write("exit\n".getBytes());
            out.flush();
            Thread.sleep(2000);
            readAvailableOutput(in, output, true); // Always clean output

            String fullOutput = output.toString();
            runTimeData.setKey(storeVariable.getValue().toString());
            runTimeData.setValue(fullOutput);

            setSuccessMessage("Output is: " + fullOutput);
            return Result.SUCCESS;

        } catch (Exception e) {
            String errorStack = ExceptionUtils.getStackTrace(e);
            logger.info("Error occurred while executing the command: " + errorStack);
            setErrorMessage("Error occurred while executing the command: " + e.getMessage());
            return Result.FAILED;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (channel != null && channel.isConnected()) channel.disconnect();
                if (session != null && session.isConnected()) session.disconnect();
            } catch (IOException e) {
                logger.warn("Error closing resources: " + e.getMessage());
            }
        }
    }

    private void readAvailableOutput(InputStream in, StringBuilder output, boolean shouldClean) throws IOException {
        byte[] tmp = new byte[1024];
        while (in.available() > 0) {
            int i = in.read(tmp, 0, 1024);
            if (i < 0) break;
            String rawOutput = new String(tmp, 0, i);
            if (shouldClean) {
                String cleanOutput = stripAnsiCodes(rawOutput);
                output.append(cleanOutput);
            } else {
                output.append(rawOutput);
            }
        }
    }

    private String stripAnsiCodes(String input) {
        if (input == null) return null;

        String ansiPattern = "\u001B\\[[\\d;]*[A-Za-z]";
        String cleaned = input.replaceAll(ansiPattern, "");

        cleaned = cleaned.replaceAll("\u001B\\[\\d+[A-Za-z]", "");
        cleaned = cleaned.replaceAll("\u001B\\[\\d+;\\d+[A-Za-z]", "");
        cleaned = cleaned.replaceAll("\u001B\\[\\d+;\\d+;\\d+[A-Za-z]", "");


        cleaned = cleaned.replaceAll("\r\n", "\n");
        cleaned = cleaned.replaceAll("\r", "\n");

        cleaned = cleaned.replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
        return cleaned;
    }

    private String formatCommands(String commands, String separator) {
        if (commands == null || commands.trim().isEmpty()) {
            return "";
        }

        if (commands.contains(separator)) {
            return commands;
        }

        return commands.trim();
    }
}

