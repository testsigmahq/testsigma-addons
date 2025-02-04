package com.testsigma.addons.web;


import com.jcraft.jsch.*;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Properties;

@Data
@Action(actionText = "SFTP: Verify if file exists on SFTP server. SFTP server details: Host: Host-Name, Port: Port-No, User Name: User-Name, Password: User-Password, Remote File Path: Remote-File-Path",
        description = "Connects to an SFTP server and verifies if a file exists at the specified remote path.",
        applicationType = ApplicationType.WEB,
        useCustomScreenshot = false)
public class SFTPFileExistsVerification extends WebAction {

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
        logger.info("Initiating execution for SFTP file existence verification");

        String user = userName.getValue().toString();
        String host = hostName.getValue().toString();
        int portNumber = Integer.parseInt(port.getValue().toString());
        String password = userPassword.getValue().toString();
        String remoteFile = remoteFilePath.getValue().toString();

        JSch jsch = new JSch();
        ChannelSftp channelSftp = null;
        Session session = null;

        try {
            // Convert path to SFTP format
            remoteFile = convertToSFTPPath(remoteFile);
            logger.info("Remote-File-Path: " + remoteFile);

            session = jsch.getSession(user, host, portNumber);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");  // WARNING: Remove for production use
            session.setConfig(config);
            session.connect();
            logger.info("Session connected.");

            Channel channel = session.openChannel("sftp"); // Open channel before connecting
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            logger.info("SFTP channel connected.");

            // Verify if file exists
            if (verifyFileExists(channelSftp, remoteFile)) {
                setSuccessMessage("File exists on the server: " + remoteFile);
            } else {
                setErrorMessage("File does not exist on the server: " + remoteFile);
                result = Result.FAILED;
            }

        } catch (Exception e) {
            String errorStack = ExceptionUtils.getStackTrace(e);
            logger.warn("Error occurred during SFTP file existence verification: " + errorStack);
            result = Result.FAILED;
            setErrorMessage("Error occurred during SFTP file existence verification: " + e.getMessage());
        } finally {
            // Clean up and close connections and streams
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return result;
    }

    private boolean verifyFileExists(ChannelSftp channelSftp, String remoteFilePath) {
        try {
            channelSftp.stat(remoteFilePath); // Attempt to get file attributes.  If file doesn't exist, it throws an exception.
            logger.info("File exists on the server: " + remoteFilePath);
            return true; // File exists if stat() doesn't throw an exception
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                logger.warn("File does not exist on the remote server: " + remoteFilePath);
                setErrorMessage("File not found on the server.");
            } else {
                logger.warn("Error checking file existence: " + e.getMessage());
                setErrorMessage("Error during file verification: " + e.getMessage());
            }
            return false; // File doesn't exist or other error
        }
    }
}