package com.testsigma.addons.services;

import com.testsigma.sdk.runners.CICDCredentials;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;

public class UploadToStorageService {
  private final LoggerService logger = new LoggerService();

  public String uploadToStorage(CICDCredentials cicdCredentials) {
    String junitUploadUrl = cicdCredentials.getAddonOutputUploadUrl();
    String workingDirectory = GlobalStateService.getInstance().getWorkingDirectory();
    File zipFile = new File(workingDirectory + File.separator + "AddonOutputDownload.zip");

    logger.printLogs("Sending Request ...");
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      logger.printLogs("Creating HTTP PUT Request");
      HttpPut httpPut = new HttpPut(junitUploadUrl);

      FileEntity fileEntity = new FileEntity(zipFile);
      httpPut.setEntity(fileEntity);

      HttpResponse httpResponse = httpClient.execute(httpPut);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        logger.printLogs("File successfully uploaded to: " + junitUploadUrl);
      } else {
        logger.printLogs("File Upload failed with Status Code : " + statusCode);
      }
      return httpResponse.getStatusLine().toString();

    } catch (IOException e) {
      logger.printLogs("Error occurred while uploading to S3: " + e.getMessage());
    }
    return null;
  }

}
