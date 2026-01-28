package com.testsigma.addons.restapi;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.RestApiAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Data
@Action(actionText = "Xml: Extract and store the content in the runtime-variable for XML filepath",
        description = "Extracts the content of xml and stores it in the runtime variable",
        applicationType = ApplicationType.REST_API)
public class ExtractTheContentOfXML extends RestApiAction {

    @TestData(reference = "filepath")
    private com.testsigma.sdk.TestData filepath;
    @TestData(reference = "runtime-variable", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData targetVariable;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        final String SUCCESS_MESSAGE = "Successfully extracted content and stored it in runtime variable";
        final String FAILURE_MESSAGE = "Failed to extract content";
        logger.info("Extracting the content of XML filepath and storing it in the runtime-variable");
        logger.info("Test Data (Filepath): " + filepath.getValue().toString());
        Result result = Result.SUCCESS;

        try {
            String filePathStr = filepath.getValue().toString();
            String xmlContent;

            // Check if the path is an HTTP/HTTPS URL
            if (filePathStr.startsWith("http://") || filePathStr.startsWith("https://")) {
                // Use HttpClient for HTTP/HTTPS URLs
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(filePathStr))
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                xmlContent = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            } else {
                // Use Files API for local file paths and UNC paths
                xmlContent = Files.readString(Paths.get(filePathStr), StandardCharsets.UTF_8);
            }

            logger.info("Extracted XML Content: " + xmlContent);
            runTimeData.setValue(xmlContent);
            runTimeData.setKey(targetVariable.getValue().toString());
            setSuccessMessage(SUCCESS_MESSAGE);

        } catch (Exception e) {
            logger.info("Error occurred while extracting XML content: " + ExceptionUtils.getMessage(e));
            setErrorMessage(FAILURE_MESSAGE);
            result = Result.FAILED;
        }

        return result;
    }

}
