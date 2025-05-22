package com.testsigma.addons.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.URI;
import java.util.Locale;

public class CLIDownloaderService {

  public final LoggerService loggerService = new LoggerService();

  public String downloadExe() throws Exception {
    String baseURL = "https://eks-common-bucket.s3.us-east-1.amazonaws.com/trcli-executable";

    // Get the operating system name and architecture
    String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

    loggerService.printLogs("OS " + os);
    loggerService.printLogs("arch " + arch);
    loggerService.printLogs("SystemUtils.IS_OS_WINDOWS " + SystemUtils.IS_OS_MAC_OSX);

    String osFolder = null;
    String fileExtension = "";

    if (SystemUtils.IS_OS_WINDOWS) {
      if (arch.contains("arm64") || arch.contains("aarch64")) {
        osFolder = "windows-arm64";
      } else {
        osFolder = "windows-amd64";
      }
      fileExtension = ".exe";
    } else if (SystemUtils.IS_OS_LINUX) {
      if (arch.contains("arm64") || arch.contains("aarch64")) {
        osFolder = "linux-arm64";
      } else {
        osFolder = "linux-amd64";
      }
    } else if (SystemUtils.IS_OS_MAC) {
      if (arch.contains("amd64") || arch.contains("x86_64")) {
        osFolder = "macos-amd64";
      } else {
        osFolder = "macos-arm64";
      }
    } else {
      loggerService.printLogs("Unsupported OS " + os + arch);
    }
    String s3DownloadUrl = baseURL + "/" + osFolder + "/trcli-exe" + fileExtension;

    // Download the executable
    String destinationFile = GlobalStateService.getInstance().getWorkingDirectory() + "/trcli-exe" + fileExtension;
    loggerService.printLogs("Downloading " + s3DownloadUrl + " to " + destinationFile);
    FileUtils.copyURLToFile(new URI(s3DownloadUrl).toURL(), new File(destinationFile));
    loggerService.printLogs("Downloaded " + s3DownloadUrl + " to " + destinationFile);

    // Add permission
    changeToExecutable(new File(destinationFile));

    return destinationFile;
  }

  private void changeToExecutable(File source) {
    boolean result = source.setExecutable(true);
    if (!result) {
      loggerService.printLogs("Problem while changing the permissions to executable::" + source);
      return;
    }
    loggerService.printLogs("Changed the permission to executable::" + source);
  }

}
