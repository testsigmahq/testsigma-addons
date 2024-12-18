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
@Action(actionText = "SSH: Connect to SSH server and execute commands, SSH server details: Host: Host-Name, Port: Port-Number, UserName: User-Name, Password: User-Password, Commands: Terminal-Commands , Command Separator: Command-Separator , Store Output Variable: Variable-Name",
        description = "Executes commands on an SSH server in a single session",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SDFSSHCommandExecution extends WebAction {

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
            : "&&";
    String allCommands = commands.getValue().toString().replace(commandSeparatorStr, "&&");

    JSch jsch = new JSch();
    Session session = null;
    ChannelExec channel = null;
    InputStream in = null;
    StringBuilder output = new StringBuilder();

    try {
      session = jsch.getSession(user, host, port);
      session.setPassword(password);

      // Avoid host key check for simplicity. Implement properly in production.
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);

      session.connect();
      logger.info("Session connected.");

      // Execute all commands in a single channel
      channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand(allCommands);
      channel.setInputStream(null);
      channel.setErrStream(System.err);

      in = channel.getInputStream();
      channel.connect();
      logger.info("Channel connected for all commands: " + allCommands);

      byte[] tmp = new byte[1024];
      while (true) {
        while (in.available() > 0) {
          int i = in.read(tmp, 0, 1024);
          if (i < 0) break;
          output.append(new String(tmp, 0, i));
        }
        if (channel.isClosed()) {
          break;
        }
        try {
          Thread.sleep(1000);
        } catch (Exception ee) {
          logger.info("Error while sleeping thread: " + ee.getMessage());
        }
      }


      runTimeData.setKey(storeVariable.getValue().toString());
      runTimeData.setValue(output.toString());
      setSuccessMessage("Output is: " + output.toString());
      return Result.SUCCESS;
    } catch (Exception e) {
      String errorStack = ExceptionUtils.getStackTrace(e);
      logger.info("Error occurred while executing the command: " + errorStack);
      setErrorMessage("Error occurred while executing the command: " + e.getMessage());
      return Result.FAILED;
    } finally {
      try {
        if (in != null) in.close();
        if (channel != null && channel.isConnected()) channel.disconnect();
        if (session != null && session.isConnected()) session.disconnect();
      } catch (IOException e) {
        setErrorMessage("Error closing resources: " + e.getMessage());
        return Result.FAILED;
      }
    }
  }
}
