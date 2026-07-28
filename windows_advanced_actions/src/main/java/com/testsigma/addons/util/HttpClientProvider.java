package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shared HTTP execution for the visual-server calls (OCR + find-image).
 *
 * The visual-server calls fail on some customer machines with:
 *   java.io.IOException: unexpected end of stream ... (RealConnection.createTunnel)
 * This happens when OkHttp is routed through the corporate system proxy
 * (the JVM is typically launched with -Djava.net.useSystemProxies=true) and that
 * proxy intermittently drops the HTTPS CONNECT tunnel. A direct curl on the same
 * box works because curl connects directly.
 *
 * We don't know in advance whether a given machine needs a proxy or a direct
 * connection, so instead of guessing we try BOTH paths and use whichever succeeds:
 *   1. direct (no proxy)        -- matches a working direct curl
 *   2. system/default proxy     -- for networks that mandate a proxy
 *
 * An environment can also force an explicit proxy (with optional basic auth) via
 * JVM system properties, in which case only that path is used:
 *   -Docr.proxyHost=proxy.host -Docr.proxyPort=8080
 *   -Docr.proxyUser=user -Docr.proxyPassword=pass   (optional)
 *
 * Callers should also send the "Connection: close" request header so a stale
 * pooled keep-alive connection is never reused.
 */
public class HttpClientProvider {

    private static final int ATTEMPTS_PER_PATH = 2;

    private static final List<OkHttpClient> HTTP_CLIENTS = buildHttpClients();

    private HttpClientProvider() {
    }

    private static OkHttpClient.Builder baseBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
    }

    private static List<OkHttpClient> buildHttpClients() {
        List<OkHttpClient> clients = new ArrayList<>();

        String proxyHost = System.getProperty("ocr.proxyHost");
        String proxyPort = System.getProperty("ocr.proxyPort");

        if (proxyHost != null && !proxyHost.trim().isEmpty()
                && proxyPort != null && !proxyPort.trim().isEmpty()) {
            // Explicit proxy requested via system properties: use only this path.
            OkHttpClient.Builder builder = baseBuilder()
                    .proxy(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(proxyHost.trim(), Integer.parseInt(proxyPort.trim()))));

            final String proxyUser = System.getProperty("ocr.proxyUser");
            final String proxyPassword = System.getProperty("ocr.proxyPassword");
            if (proxyUser != null && !proxyUser.isEmpty()) {
                builder.proxyAuthenticator((route, response) -> {
                    String credential = Credentials.basic(proxyUser,
                            proxyPassword == null ? "" : proxyPassword);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });
            }
            clients.add(builder.build());
        } else {
            // Try direct first (matches a working direct curl), then fall back to
            // the JVM/system default proxy for networks that require one.
            clients.add(baseBuilder().proxy(Proxy.NO_PROXY).build());
            clients.add(baseBuilder().build()); // uses default ProxySelector (system proxy)
        }

        return clients;
    }

    /**
     * Executes the request, trying each connection path (direct, then system
     * proxy) and retrying within a path to absorb transient blips. Returns the
     * response body string on the first 2xx response.
     *
     * @throws RuntimeException if the server returns a non-2xx status or empty body
     *                          (a real response, so paths are not retried)
     * @throws IOException      if every connection path/attempt fails at the
     *                          transport level
     */
    public static String executeWithFallback(Request request, Logger logger) throws IOException {
        IOException lastException = null;

        for (int pathIndex = 0; pathIndex < HTTP_CLIENTS.size(); pathIndex++) {
            OkHttpClient client = HTTP_CLIENTS.get(pathIndex);
            String pathLabel = "path " + (pathIndex + 1) + "/" + HTTP_CLIENTS.size();

            for (int attempt = 1; attempt <= ATTEMPTS_PER_PATH; attempt++) {
                logger.info("HTTP call (" + pathLabel + ", attempt " + attempt + "/" + ATTEMPTS_PER_PATH + ")");
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().string();
                    }
                    // A real HTTP response (both paths reach the same server), so
                    // don't fall back or retry -- surface it immediately.
                    throw new RuntimeException("HTTP call failed with status: " + response.code());
                } catch (IOException e) {
                    lastException = e;
                    logger.info("HTTP call failed (" + pathLabel + ", attempt " + attempt + "): " + e.getMessage());
                    if (attempt < ATTEMPTS_PER_PATH) {
                        try {
                            Thread.sleep(500L * attempt); // simple linear backoff within a path
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                    }
                }
            }
            logger.info("HTTP " + pathLabel + " exhausted; trying next connection path if available.");
        }

        logger.info("Exception during HTTP call: " + ExceptionUtils.getStackTrace(lastException));
        throw lastException;
    }
}
