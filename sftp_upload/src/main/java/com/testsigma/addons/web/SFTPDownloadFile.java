package com.testsigma.addons.web;

import com.jcraft.jsch.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.file.Paths;
import java.util.Properties;

@Data
@Action(actionText = "SFTP: Download file from SFTP server to a local folder. SFTP server details: Host: Host-Name, Port: Port-No, User Name: User-Name, Password: User-Password, Remote File Path: Remote-File-Path, Local Destination Folder: Local-Destination-Folder",
        description = "Connects to an SFTP server and downloads a file to a specified local folder, keeping the original filename.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SFTPDownloadFile extends WebAction {

  @TestData(reference = "Host-Name")
  private com.testsigma.sdk.TestData hostName;

  @TestData(reference = "Port-No")
  private com.testsigma.sdk.TestData port;

  @TestData(reference = "User-Name")
  private com.testsigma.sdk.TestData userName;

  @TestData(reference = "User-Password")
  private com.testsigma.sdk.TestData userPassword;

  @TestData(reference = "Remote-File-Path")
  private com.testsigma.sdk.TestData remoteFilePath;

  @TestData(reference = "Local-Destination-Folder")
  private com.testsigma.sdk.TestData localDestinationFolder;

  // Method to convert Windows path to SFTP compatible format
  private String convertToSFTPPath(String path) {
    String os = System.getProperty("os.name").toLowerCase();

    if (os.contains("win")) {
      path = path.replace("\\", "/");
      if (path.matches("^[a-zA-Z]:.*")) {
        path = "/" + path.substring(0, 1).toUpperCase() + path.substring(2);
      }
    }
    return path;
  }

  @Override
  public Result execute() {
    Result result = Result.SUCCESS;
    logger.info("Initiating execution for SFTP file download");

    String user = userName.getValue().toString();
    String host = hostName.getValue().toString();
    int portNumber = Integer.parseInt(port.getValue().toString());
    String password = userPassword.getValue().toString();
    String remoteFile = remoteFilePath.getValue().toString();
    String localFolder = localDestinationFolder.getValue().toString();

    JSch jsch = new JSch();
    ChannelSftp channelSftp = null;
    Session session = null;
    String finalLocalPath = "";

    try {
      // Convert path to SFTP format
      remoteFile = convertToSFTPPath(remoteFile);
      logger.info("Remote Source File Path: " + remoteFile);

      // Extract filename from the remote path
      String remoteFileName = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);
      if(remoteFileName.isEmpty()){
        throw new Exception("Remote file path cannot end with a slash. It must point to a specific file.");
      }

      // Construct the full local path using Java's Path API for cross-platform compatibility
      finalLocalPath = Paths.get(localFolder, remoteFileName).toString();
      logger.info("Local Destination Folder: " + localFolder);
      logger.info("Extracted Filename: " + remoteFileName);
      logger.info("Final Local Destination Path: " + finalLocalPath);

      session = jsch.getSession(user, host, portNumber);
      session.setPassword(password);

      Properties config = new Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);
      session.connect();
      logger.info("Session connected.");

      Channel channel = session.openChannel("sftp");
      channel.connect();
      channelSftp = (ChannelSftp) channel;
      logger.info("SFTP channel connected.");

      // Download the file from remote server to the constructed local path
      logger.info(String.format("Attempting to download file from '%s' to '%s'", remoteFile, finalLocalPath));
      channelSftp.get(remoteFile, finalLocalPath);

      setSuccessMessage(String.format("Successfully downloaded file from '%s' to '%s'", remoteFile, finalLocalPath));
      logger.info("File download completed successfully.");

    } catch (SftpException e) {
      String errorMessage;
      if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
        errorMessage = "File not found on the SFTP server at path: " + remoteFile;
      } else if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
        errorMessage = "Permission denied for file operation at path: " + remoteFile;
      } else {
        errorMessage = "An SFTP error occurred during file download: " + e.getMessage();
      }
      String errorStack = ExceptionUtils.getStackTrace(e);
      logger.warn(errorMessage + "\n" + errorStack);
      result = Result.FAILED;
      setErrorMessage(errorMessage);
    } catch (Exception e) {
      String errorStack = ExceptionUtils.getStackTrace(e);
      logger.warn("An unexpected error occurred during SFTP file download: " + errorStack);
      result = Result.FAILED;
      setErrorMessage("An unexpected error occurred during SFTP file download: " + e.getMessage());
    } finally {
      // Clean up and close connections
      if (channelSftp != null && channelSftp.isConnected()) {
        channelSftp.disconnect();
        logger.info("SFTP channel disconnected.");
      }
      if (session != null && session.isConnected()) {
        session.disconnect();
        logger.info("Session disconnected.");
      }
    }

    return result;
  }
}