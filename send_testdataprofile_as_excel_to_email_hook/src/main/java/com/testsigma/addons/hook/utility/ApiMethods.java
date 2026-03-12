package com.testsigma.addons.hook.utility;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiMethods {

    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    public String makeGetRequest(String endPoint, String id, String apiKey) throws IOException, InterruptedException, ApiException {

        HttpResponse<String> response =null;
        String body = null;
        URI uri = URI.create(endPoint + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 ){
            throw new ApiException("Status code of the api request is not 200" +response);
        } else if ( response.body().isEmpty()) {
            throw new ApiException("Response of the api request is empty");
        } else {
            body = response.body();
        }
        return body;
    }

    public static class ApiException extends Exception {

        public ApiException(String errorMessage) {
            super(errorMessage);
        }
    }

}
