package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Action(actionText = "Convert ZPL file-path into PNG file with name (Ex:output.png) file-name and output file path output-file-path and store " +
        "filepath in runtime variable variable-name",
        description = "Converts ZPL code into a PNG file and stores the file path in runtime variable",
        applicationType = ApplicationType.WEB)
public class ZplToPngConvertor extends WebAction {

  @TestData(reference = "file-path")
  private com.testsigma.sdk.TestData filePath_;

  @TestData(reference = "file-name")
  private com.testsigma.sdk.TestData fileName_;

  @TestData(reference = "output-file-path")
  private com.testsigma.sdk.TestData outputFilePath_;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variable_;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;

  @Override
  public Result execute() throws NoSuchElementException {
    String filePath = filePath_.getValue().toString();
    String fileName = fileName_.getValue().toString();
    String outputFilePath = outputFilePath_.getValue().toString();
    String variableName = variable_.getValue().toString();

    try {
      // Validate input file path
      Path inputFilePath = Paths.get(filePath);
      if (!Files.exists(inputFilePath)) {
        setErrorMessage("File Path does not exist: " + filePath);
        return Result.FAILED;
      }

      // Check and update file name to ensure it ends with .png
      if (!fileName.endsWith(".png")) {
        if (fileName.contains(".")) {
          setErrorMessage("Invalid file extension. Only .png is allowed: " + fileName);
          return Result.FAILED;
        }
        fileName = fileName + ".png";  // Append .png if no extension is given
      }

      // Read the ZPL file content
      String zpl = Files.readString(inputFilePath, StandardCharsets.UTF_8);

      // Prepare the HTTP request
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create("https://api.labelary.com/v1/printers/8dpmm/labels/4x6/0/"))
              .header("Accept", "image/png")
              .POST(HttpRequest.BodyPublishers.ofString(zpl))
              .build();

      // Send the HTTP request and process the response
      HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

      if (response.statusCode() == 200) {
        // Save the response as a PNG file
        Path outputFilePathWithName = Paths.get(outputFilePath, fileName);
        Files.write(outputFilePathWithName, response.body());

        // Store the file path in the runtime variable
        runTimeData.setKey(variableName);
        runTimeData.setValue(outputFilePathWithName.toString());
        setSuccessMessage("Successfully converted Zpl to Png and saved to: " + outputFilePathWithName);
        return Result.SUCCESS;
      } else {
        setErrorMessage("Failed to convert ZPL. HTTP Status: " + response.statusCode() +
                ". Response body: " + new String(response.body(), StandardCharsets.UTF_8));
        return Result.FAILED;
      }
    } catch (IOException e) {
      setErrorMessage("I/O Error: " + e.getMessage());
      return Result.FAILED;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      setErrorMessage("Request interrupted: " + e.getMessage());
      return Result.FAILED;
    } catch (Exception e) {
      setErrorMessage("Unexpected error: " + e.getMessage());
      return Result.FAILED;
    }
  }
}
