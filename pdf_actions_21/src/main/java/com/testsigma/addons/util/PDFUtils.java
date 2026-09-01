package com.testsigma.addons.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PDFUtils {

    public static PDDocument getPDDocument(String urlString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .build();

        // Send request and get response as InputStream
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        // Create a temporary file to store the PDF
        Path tempFilePath = Files.createTempFile("tempfile-", ".pdf");
        tempFilePath.toFile().deleteOnExit(); // Delete temp file on JVM exit

        // Copy the response InputStream to the temporary file
        try (InputStream is = response.body()) {
            Files.copy(is, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Temporary file created: " + tempFilePath);
        } catch (Exception e) {
            throw new Exception("Error while copying the PDF from URL to temporary file", e);
        }
        // Load PDF from the temporary file path
        try {
            return Loader.loadPDF(tempFilePath.toFile());
        } catch (Exception e) {
            throw new Exception("Error while loading the PDF document", e);
        }

    }
}
