package com.testsigma.addons.web;

import com.jcraft.jsch.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(actionText = "SFTP: Connect to SFTP server and upload a file. SFTP server details: Host: Host-Name, UserName: User-Name, Password: User-Password, Upload from Local-File-Path to Remote-Directory(ex: /C:/Users/username/Downloads) with file name Remote-File-Name. Uploaded file path will be stored in runtime variable: variable-name",
        description = "Uploads a file from local system or URL to remote server using SFTP, and stores the uploaded file path in a runtime variable, ensuring matching extensions.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SFTPUploadFile extends WindowsAction {

  @TestData(reference = "Host-Name")
  private com.testsigma.sdk.TestData hostName;

  @TestData(reference = "User-Name")
  private com.testsigma.sdk.TestData userName;

  @TestData(reference = "User-Password")
  private com.testsigma.sdk.TestData userPassword;

  @TestData(reference = "Local-File-Path")
  private com.testsigma.sdk.TestData localFilePath; // Local file path or URL

  @TestData(reference = "Remote-Directory")
  private com.testsigma.sdk.TestData remoteDirectory; // Remote directory to upload to

  @TestData(reference = "Remote-File-Name")
  private com.testsigma.sdk.TestData remoteFileName; // Remote file name after upload

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData uploadedFilePath; // Runtime variable for storing uploaded file path

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  // Method to convert Windows path to SFTP compatible format
  private String convertToSFTPPath(String path) {
    String os = System.getProperty("os.name").toLowerCase();

    if (os.contains("win")) {
      // For Windows, convert the path to SFTP style
      // Example: "C:\\Users\\sam.isaac\\Downloads" -> "/C:/Users/sam.isaac/Downloads"
      path = path.replace("\\", "/");
      if (path.matches("^[a-zA-Z]:.*")) {
        path = "/" + path.substring(0, 1).toUpperCase() + path.substring(2);
      }
    }
    return path;
  }

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution for SFTP file upload");

    String user = userName.getValue().toString();
    String host = hostName.getValue().toString();
    String password = userPassword.getValue().toString();
    String localFile = localFilePath.getValue().toString();
    String remoteDir = remoteDirectory.getValue().toString();
    String remoteFile = remoteFileName.getValue().toString();
    File localFileToUpload = null;

    logger.info("remoteDir: " + remoteDir);

    // Convert paths to SFTP format (handles both local and remote paths)
    remoteDir = convertToSFTPPath(remoteDir);
    logger.info("Remote-Directory: " + remoteDir);

    JSch jsch = new JSch();
    ChannelSftp channelSftp = null;
    Session session = null;

    try {
      // Determine if the localFile is a URL or a local path
      if (localFile.startsWith("http://") || localFile.startsWith("https://") || localFile.startsWith("file://")) {
        localFileToUpload = downloadFile(localFile);
      } else {
        localFile = convertToSFTPPath(localFile);
        localFileToUpload = new File(localFile);
      }

      logger.info("Local-File-Path: " + localFileToUpload.getAbsolutePath());


      if (!localFileToUpload.exists()) {
        setErrorMessage("Local file not found: " + localFile);
        return Result.FAILED;
      }

      String localFileName = localFileToUpload.getName();
      String fileExtension = "";
      int dotIndex = localFileName.lastIndexOf('.');
      if (dotIndex > 0) {
        fileExtension = localFileName.substring(dotIndex);
      }

      // Append extension if remote file name has no extension
      if (!remoteFile.contains(".")) {
        remoteFile += fileExtension;
        logger.info("Remote file name updated to include extension: " + remoteFile);
      }else{
        //check if extensions match
        int remoteDotIndex = remoteFile.lastIndexOf('.');
        String remoteFileExtension = "";
        if(remoteDotIndex >0 ){
          remoteFileExtension = remoteFile.substring(remoteDotIndex);
          if(!remoteFileExtension.equalsIgnoreCase(fileExtension)){
            setErrorMessage("Input and output file extensions do not match: " + fileExtension + " and "+ remoteFileExtension);
            return Result.FAILED;
          }

        }
      }


      // Setting up the session for SFTP connection
      session = jsch.getSession(user, host);
      session.setPassword(password);

      // Avoid host key checking (for simplicity)
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);
      session.connect();
      logger.info("Session connected.");

      // Setting up the SFTP channel
      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect();
      logger.info("SFTP channel connected.");

      // Check if the remote directory exists
      try {
        channelSftp.cd(remoteDir);
        logger.info("Remote directory exists: " + remoteDir);
      } catch (SftpException e) {
        // If the directory does not exist, create it
        logger.warn("Remote directory does not exist, creating: " + remoteDir);
        channelSftp.mkdir(remoteDir);
        channelSftp.cd(remoteDir);
      }

      // Upload the file from the local machine to the remote server
      String FilePath = null;
      // Correct concatenation of remote directory and file name
      FilePath = remoteDir.endsWith("/") ? remoteDir + remoteFile : remoteDir + "/" + remoteFile;
      channelSftp.put(localFileToUpload.getAbsolutePath(), FilePath);
      logger.info("File uploaded successfully to: " + FilePath);

      // Set success message and runtime data
      setSuccessMessage("File uploaded successfully to: " + FilePath);
      runTimeData.setKey(uploadedFilePath.getValue().toString());
      runTimeData.setValue(FilePath);

    } catch (Exception e) {
      String errorStack = ExceptionUtils.getStackTrace(e);
      logger.warn("Error occurred while uploading file: " + errorStack);
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("Error occurred during SFTP file upload: " + e.getMessage());
    } finally {
      // Clean up and close connections
      if (channelSftp != null && channelSftp.isConnected()) {
        channelSftp.disconnect();
      }
      if (session != null && session.isConnected()) {
        session.disconnect();
      }
      if (localFileToUpload != null && localFile.startsWith("http")) {
        localFileToUpload.delete(); // delete temp file if it was a download
      }
    }

    return result;
  }


  private File downloadFile(String fileUrl) throws IOException {
    URL url = new URL(fileUrl);
    String fileName = Paths.get(url.getPath()).getFileName().toString();
    File tempFile = File.createTempFile("downloaded-", fileName);
    try (InputStream in = url.openStream();
         OutputStream out = new FileOutputStream(tempFile)) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
    }
    return tempFile;
  }
}