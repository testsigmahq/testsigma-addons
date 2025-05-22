package com.testsigma.addons.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipService {

  private final LoggerService loggerService = new LoggerService();

  public void createZipFile(Long runId) throws IOException {
    GlobalStateService stateService = GlobalStateService.getInstance();
    String workingDirectory = stateService.getWorkingDirectory();

    String folderName = "testrail-output-run-" + runId;
    Path junitPath = Paths.get(stateService.getJunitFilePath());
    Path logsPath = Paths.get(stateService.getLogFilePath());
    String zipFileName = workingDirectory + File.separator + "AddonOutputDownload.zip";

    // Create the zip output stream
    try (FileOutputStream fos = new FileOutputStream(zipFileName);
         ZipOutputStream zos = new ZipOutputStream(fos)) {

      // Add junit file to the zip under the folder
      addFileToZip(zos, junitPath, folderName + "/" + junitPath.getFileName());

      // Add logs file to the zip under the folder
      addFileToZip(zos, logsPath, folderName + "/" + logsPath.getFileName());
    }

    loggerService.printLogs("Zip file created at: " + zipFileName);
  }

  private void addFileToZip(ZipOutputStream zos, Path filePath, String zipEntryName) throws IOException {
    if (Files.exists(filePath)) {
      try (InputStream fis = Files.newInputStream(filePath)) {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zos.putNextEntry(zipEntry);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
          zos.write(buffer, 0, length);
        }

        zos.closeEntry();
      }
    } else {
      loggerService.printLogs("File not found: " + filePath);
    }
  }

}
