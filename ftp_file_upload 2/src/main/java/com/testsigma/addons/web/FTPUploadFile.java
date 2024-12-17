package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.openqa.selenium.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Data
@Action(actionText = "FTP: Connect to FTP server and upload a file. FTP server details: Host: Host-Name, Port: Port-No UserName: User-Name, Password: User-Password, Upload from Local-File-Path to Remote-Directory(ex: /Users/username/Downloads) with file name Remote-File-Name. Uploaded file path will be stored in runtime variable: variable-name",
        description = "Uploads a file from local system to remote server using FTP, and stores the uploaded file path in a runtime variable.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class FTPUploadFile extends WindowsAction {

  @TestData(reference = "Host-Name")
  private com.testsigma.sdk.TestData hostName;

  @TestData(reference = "Port-No")
  private com.testsigma.sdk.TestData portNo;

  @TestData(reference = "User-Name")
  private com.testsigma.sdk.TestData userName;

  @TestData(reference = "User-Password")
  private com.testsigma.sdk.TestData userPassword;

  @TestData(reference = "Local-File-Path")
  private com.testsigma.sdk.TestData localFilePath; // Local file to upload

  @TestData(reference = "Remote-Directory")
  private com.testsigma.sdk.TestData remoteDirectory; // Remote directory to upload to

  @TestData(reference = "Remote-File-Name")
  private com.testsigma.sdk.TestData remoteFileName; // Remote file name after upload

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData uploadedFilePath; // Runtime variable for storing uploaded file path

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution for FTP file upload");

    String user = userName.getValue().toString();
    String host = hostName.getValue().toString();
    String port = portNo.getValue().toString();
    String password = userPassword.getValue().toString();
    String localFile = localFilePath.getValue().toString();
    String remoteDir = remoteDirectory.getValue().toString();
    String remoteFile = remoteFileName.getValue().toString();

    FTPClient ftpClient = new FTPClient();
    try {
      // Verify the local file exists
      File firstLocalFile = new File(localFile);
      if (!firstLocalFile.exists()) {
        setErrorMessage("Local file not found: " + localFile);
        return Result.FAILED;
      }

      // Append extension if remote file name has no extension
      if (!remoteFile.contains(".")) {
        String localFileName = firstLocalFile.getName();
        int dotIndex = localFileName.lastIndexOf('.');
        if (dotIndex > 0) { // Local file has an extension
          String extension = localFileName.substring(dotIndex); // Extract the extension
          remoteFile += extension; // Append the extension to remote file name
          logger.info("Remote file name updated to include extension: " + remoteFile);
        } else {
          setErrorMessage("Local file does not have an extension: " + localFileName);
          return Result.FAILED;
        }
      }

      // Connect and login to the server
      ftpClient.connect(host, Integer.parseInt(port));
      boolean loginSuccess = ftpClient.login(user, password);

      if (!loginSuccess) {
        setErrorMessage("Failed to login to FTP server. Check credentials.");
        return Result.FAILED;
      }

      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      // Set the target directory on the FTP server
      boolean changedDir = ftpClient.changeWorkingDirectory(remoteDir);
      if (changedDir) {
        logger.info("Changed to directory: " + remoteDir);
      } else {
        setErrorMessage("Remote directory does not exist: " + remoteDir);
        return Result.FAILED;
      }

      // Upload the file
      try (InputStream inputStream = new FileInputStream(firstLocalFile)) {
        logger.info("Start uploading file to " + remoteDir + "/" + remoteFile);
        boolean done = ftpClient.storeFile(remoteFile, inputStream);

        if (done) {
          String outputPath = remoteDir + "/" + remoteFile;
          runTimeData.setValue(outputPath);
          runTimeData.setKey(uploadedFilePath.getValue().toString());
          setSuccessMessage("File uploaded successfully to: " + outputPath);
          return Result.SUCCESS;
        } else {
          setErrorMessage("File upload failed for: " + localFile);
          return Result.FAILED;
        }
      }

    } catch (IOException ex) {
      setErrorMessage("FTP Error: " + ex.getMessage());
      logger.warn("Exception occurred: " + ex);
      return Result.FAILED;
    } finally {
      try {
        if (ftpClient.isConnected()) {
          ftpClient.logout();
          ftpClient.disconnect();
        }
        logger.info("FTP connection closed.");
      } catch (IOException ex) {
        logger.warn("Error while closing FTP connection: " + ex);
      }
    }
  }
}
