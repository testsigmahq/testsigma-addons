package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;

/**
 * Utility for the visual-server "find image" calls.
 *
 * Centralises the find-image HTTP request so every action shares the same
 * connection handling (direct/proxy fallback, retries, "Connection: close",
 * response cleanup) provided by {@link HttpClientProvider}.
 */
public class FindImageUtils {

    private FindImageUtils() {
    }

    /**
     * Calls the find-image endpoint and returns the raw response body string.
     * Callers parse the body however they need (ResponseObjectForFindImage,
     * JSON tree, coordinates, etc.).
     *
     * @throws Exception if the call fails on every connection path, or the
     *                   server returns a non-2xx status / empty body.
     */
    public static String findImageResponseBody(File baseImageFile, File searchImageFile,
                                                String threshold, Logger logger) throws Exception {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("baseImageFile", baseImageFile.getName(),
                        RequestBody.create(baseImageFile, MediaType.parse("image/png")))
                .addFormDataPart("searchImageFile", searchImageFile.getName(),
                        RequestBody.create(searchImageFile, MediaType.parse("image/png")))
                .addFormDataPart("threshold", threshold)
                .addFormDataPart("scale", "40")
                .addFormDataPart("occurance", "1")
                .build();

        Request request = new Request.Builder()
                .url(Constants.VISUAL_SERVER_FIND_IMAGE_ENDPOINT)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + Constants.API_TOKEN)
                // Use a fresh connection per request instead of reusing a pooled
                // keep-alive connection that the server/proxy may have already
                // closed (a common cause of "unexpected end of stream").
                .addHeader("Connection", "close")
                .build();

        logger.info("Making find-image API call");
        return HttpClientProvider.executeWithFallback(request, logger);
    }
}
