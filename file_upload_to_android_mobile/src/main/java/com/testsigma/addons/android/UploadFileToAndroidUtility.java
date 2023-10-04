package com.testsigma.addons.android;

import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

@Data
public class UploadFileToAndroidUtility {
    private String successMessage;
    private String errorMessage;
    private String tempFilePath;
    public boolean performUpload(String url, String remotePath, AndroidDriver androidDriver){

        try {
            URL urlObject = new URL(url);
            File tempDirectory = Files.createTempDirectory("tempDir").toFile();
            String fileName = new File(urlObject.getPath()).getName();
            File tempFile = new File(tempDirectory, fileName);

            //Extracting file from url
            FileUtils.copyURLToFile(urlObject,tempFile);

            this.tempFilePath = tempFile.getAbsolutePath();
            remotePath = remotePath.concat(fileName);

            androidDriver.pushFile(remotePath, new File(this.tempFilePath));
            this.successMessage = "Successfully uploaded file to android device, filePath: "+remotePath;
            return true;

        } catch (Exception e) {
            this.errorMessage = "Unable to upload file to device:"+e.getMessage();
            return false;
        }
    }

}
