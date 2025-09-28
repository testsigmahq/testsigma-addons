package com.testsigma.addons.windows;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WindowsAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Data
@Action(actionText = "Extract data from QR Code file-path and store the data in a runtime variable variable-name",
        description = "Decodes a QR Code from a given file path and extracts the complete data",
        applicationType = ApplicationType.WINDOWS,
        useCustomScreenshot = false)
public class ExtractDataFromQRCode extends WindowsAction {

  @TestData(reference = "file-path")
  private com.testsigma.sdk.TestData filePath;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    logger.info("Initiating execution");
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;

    String qrCodeFilePath = filePath.getValue().toString();
    logger.info("File Path: " + qrCodeFilePath);
    File qrCodeFile = null;

    try {
      // Determine if the filePath is a URL or a local path
      if (qrCodeFilePath.startsWith("http://") || qrCodeFilePath.startsWith("https://")) {
        logger.info("URL detected, downloading file.");
        qrCodeFile = downloadFile(qrCodeFilePath);
      } else {
        logger.info("Local file path detected.");
        qrCodeFile = new File(qrCodeFilePath);
      }

      if (!qrCodeFile.exists()) {
        setErrorMessage("QR Code file not found: " + qrCodeFilePath);
        result = com.testsigma.sdk.Result.FAILED;
        return result;
      }

      String qrCodeData = decodeQRCode(qrCodeFile.getAbsolutePath());

      if (qrCodeData == null) {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("Failed to decode QR Code.");
      } else {
        runTimeData.setKey(variableName.getValue().toString());
        runTimeData.setValue(qrCodeData);
        logger.info("QR Code Data: " + qrCodeData);
        setSuccessMessage("Successfully decoded QR Code: " + qrCodeData);
      }
    } catch (Exception e) {
      logger.warn("Error occurred while decoding QR Code: " + e);
      setErrorMessage("Error occurred while decoding QR Code: " + e.getMessage());
    } finally {
      // Clean up temporary file if it was downloaded
      if (qrCodeFile != null && qrCodeFilePath.startsWith("http")) {
        qrCodeFile.delete();
      }

      return result;
    }
  }

  public String decodeQRCode(String qrCodeFilePath) {
    try {
      File qrCodeFile = new File(qrCodeFilePath);
      BufferedImage bufferedImage = ImageIO.read(qrCodeFile);
      LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      Result result = new MultiFormatReader().decode(bitmap);
      return result.getText();
    } catch (IOException | NotFoundException e) {
      logger.warn("Error decoding QR Code: " + e);
      return null;
    }
  }

  private File downloadFile(String fileUrl) throws IOException {
    URL url = new URL(fileUrl);
    logger.info("Downloading URL: " + url);
    String fileName = Paths.get(url.getPath()).getFileName().toString();
    logger.info("File name: " + fileName);
    File tempFile = File.createTempFile("downloaded-", fileName);
    logger.info("Temp file created: " + tempFile.getAbsolutePath());
    try (InputStream in = url.openStream();
         OutputStream out = new FileOutputStream(tempFile)) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
    }

    logger.info("Downloaded file: " + tempFile.getAbsolutePath());
    return tempFile;
  }
}