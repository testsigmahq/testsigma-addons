package com.testsigma.addons.web;

import com.testsigma.addons.util.ImageComparisonUtils;
import com.testsigma.addons.util.StringToImageConverter;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import com.testsigma.sdk.annotation.TestStepResult;
import lombok.Data;

import java.io.File;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

@Data
@Action(actionText = "SSHJ: Connect to SSH server and execute commands, SSH server details: Host: Host-Name," +
        " Port: Port-Number, UserName: User-Name, Password: User-Password, Commands: Terminal-Commands ," +
        " Command Separator: Command-Separator , Store Output Variable: Variable-Name",
        description = "Executes commands on an SSH server using the SSHJ library (alternative implementation)",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = true)
public class SSHCommandExecutionSSHJ extends WebAction {

  @TestStepResult
  private com.testsigma.sdk.TestStepResult testStepResult;

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
    logger.info("Initiating execution (SSHJ)");

    String user = userName.getValue().toString();
    String host = hostName.getValue().toString();
    String password = userPassword.getValue().toString();
    int port = Integer.parseInt(portNumber.getValue().toString());
    String commandSeparatorStr = commandSeparator != null && !commandSeparator.getValue().toString().isEmpty()
            ? commandSeparator.getValue().toString()
            : "&&";
    String allCommands = commands.getValue().toString().replace(commandSeparatorStr, "&&");

    // Run in a login shell so PATH and env from /etc/profile, ~/.profile are loaded.
    // Otherwise exec() uses a minimal shell (e.g. sh) and commands like "smsd" are not found.
    String wrappedCommand = wrapInLoginShell(allCommands);

    SSHClient ssh = null;
    Session session = null;

    try {
      ssh = new SSHClient();
      ssh.addHostKeyVerifier(new PromiscuousVerifier());
      ssh.connect(host, port);
      ssh.authPassword(user, password);
      logger.info("SSH session connected (SSHJ).");

      session = ssh.startSession();
      Command cmd = session.exec(wrappedCommand);
      StringBuilder output = new StringBuilder();
      try (Reader reader = new InputStreamReader(cmd.getInputStream())) {
        char[] buf = new char[1024];
        int n;
        while ((n = reader.read(buf)) != -1) {
          output.append(buf, 0, n);
        }
      }
      cmd.join(30, TimeUnit.SECONDS);
      if (cmd.getExitStatus() != null && cmd.getExitStatus() != 0) {
        try (Reader errReader = new InputStreamReader(cmd.getErrorStream())) {
          char[] buf = new char[1024];
          int n;
          while ((n = errReader.read(buf)) != -1) {
            output.append(buf, 0, n);
          }
        }
      }

      runTimeData.setKey(storeVariable.getValue().toString());
      runTimeData.setValue(output.toString());

      // Display output as custom screenshot in step result (same as DisplayTestDataAsStepResultScreenShot)
      File screenshotFile = null;
      try {
        screenshotFile = StringToImageConverter.convertToFile(output);
        String s3Url = testStepResult != null ? testStepResult.getScreenshotUrl() : null;
        if (s3Url != null && !s3Url.isEmpty()) {
          ImageComparisonUtils imageComparisonUtils = new ImageComparisonUtils(driver, logger);
          boolean uploadResult = imageComparisonUtils.uploadFile(s3Url, screenshotFile.getAbsolutePath());
          if (!uploadResult) {
            logger.debug("Error uploading custom screenshot to S3; step result may not show the output image.");
          } else {
            logger.debug("Custom screenshot (command output) uploaded successfully.");
          }
        }
      } finally {
        if (screenshotFile != null && screenshotFile.exists()) {
          screenshotFile.deleteOnExit();
        }
      }

      setSuccessMessage("Output is: " + output.toString());
      return Result.SUCCESS;
    } catch (Exception e) {
      String errorStack = ExceptionUtils.getStackTrace(e);
      logger.info("Error occurred while executing the command (SSHJ): " + errorStack);
      setErrorMessage("Error occurred while executing the command: " + e.getMessage());
      return Result.FAILED;
    } finally {
      try {
        if (session != null) session.close();
        if (ssh != null) ssh.disconnect();
      } catch (IOException e) {
        logger.info("Error closing SSHJ resources: " + e.getMessage());
      }
    }
  }

  /**
   * Wraps the command in a login shell (bash -l -c '...') so that profile scripts
   * are sourced and PATH includes /usr/local/bin, custom paths, etc. Without this,
   * session.exec() runs in a minimal non-login shell where commands like "smsd" may not be found.
   */
  private static String wrapInLoginShell(String command) {
    String escaped = command.replace("'", "'\\''");
    return "bash -l -c '" + escaped + "'";
  }
}
