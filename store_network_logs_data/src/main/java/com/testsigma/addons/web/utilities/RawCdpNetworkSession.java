package com.testsigma.addons.web.utilities;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.CdpEndpointFinder;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.Connection;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.SeleniumCdpConnection;
import org.openqa.selenium.devtools.idealized.target.model.SessionID;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Talks Network.* CDP directly over the raw JSON-RPC connection Selenium already establishes
 * (local ChromeDriver or grid-proxied — SeleniumCdpConnection handles both), bypassing
 * DevTools.createSession()/CdpVersionFinder entirely. That version-matching layer is what fails
 * with "no-op implementation of the CDP" whenever the shared Selenium install's bundled
 * devtools-vNNN jars don't cover the live browser's major version — a gap outside this addon's
 * control, since that install lives in the host agent, not this addon's own classpath. The
 * Target.attachToTarget + Network.* wire format below is plain CDP JSON and has been stable
 * across Chrome versions for years, so it isn't tied to any bundled binding at all.
 */
public class RawCdpNetworkSession implements AutoCloseable {

    private final Connection connection;
    private final SessionID sessionId;
    private final Logger logger;

    private RawCdpNetworkSession(Connection connection, SessionID sessionId, Logger logger) {
        this.connection = connection;
        this.sessionId = sessionId;
        this.logger = logger;
    }

    @SuppressWarnings("unchecked")
    public static RawCdpNetworkSession attach(WebDriver driver, Logger logger, Duration timeout) throws Exception {
        org.openqa.selenium.Capabilities capabilities = null;
        if (driver instanceof org.openqa.selenium.HasCapabilities) {
            capabilities = ((org.openqa.selenium.HasCapabilities) driver).getCapabilities();
        } else {
            logger.info("[raw-cdp] driver does not implement HasCapabilities: " + driver.getClass().getName());
        }

        if (isSauceLabs(capabilities)) {
            logger.warn("[raw-cdp] Detected an unsupported test lab environment for CDP-based network capture.");
            throw new UnsupportedOperationException(
                    "Network capture is not currently supported in this test lab environment.");
        }

        try {
            if (capabilities != null) {
                Optional<java.net.URI> reportedUri = CdpEndpointFinder.getReportedUri(capabilities);
                logger.info("[raw-cdp] driver capabilities reported CDP endpoint: " + reportedUri);
            }
        } catch (Exception e) {
            logger.info("[raw-cdp] could not read reported CDP endpoint from capabilities: " + e.getMessage());
        }

        logger.info("[raw-cdp] calling SeleniumCdpConnection.create(driver)...");
        Optional<Connection> connectionOpt = SeleniumCdpConnection.create(driver);
        logger.info("[raw-cdp] SeleniumCdpConnection.create returned present=" + connectionOpt.isPresent());
        Connection connection = connectionOpt.orElseThrow(() -> new IllegalStateException(
                "Could not establish a raw CDP connection (no CDP endpoint reported for this driver)."));

        String targetId;
        try {
            targetId = driver.getWindowHandle();
        } catch (Exception e) {
            logger.warn("[raw-cdp] driver.getWindowHandle() failed: " + e.getMessage());
            throw e;
        }
        logger.info("[raw-cdp] driver.getWindowHandle() (used as CDP targetId): " + targetId);

        logger.info("[raw-cdp] sending Target.attachToTarget with targetId=" + targetId + ", flatten=true, timeout=" + timeout);
        Map<String, Object> attachResult;
        try {
            attachResult = connection.sendAndWait(
                    new SessionID(""),
                    new Command<>("Target.attachToTarget", Map.of("targetId", targetId, "flatten", true), Map.class),
                    timeout);
        } catch (Exception e) {
            logger.warn("[raw-cdp] Target.attachToTarget threw: " + e);
            throw e;
        }
        logger.info("[raw-cdp] Target.attachToTarget raw result: " + attachResult);

        Object rawSessionId = attachResult == null ? null : attachResult.get("sessionId");
        if (rawSessionId == null) {
            throw new IllegalStateException("Target.attachToTarget did not return a sessionId: " + attachResult);
        }
        logger.info("[raw-cdp] attached, sessionId: " + rawSessionId);

        RawCdpNetworkSession session = new RawCdpNetworkSession(connection, new SessionID(rawSessionId.toString()), logger);
        session.enableNetwork(timeout);
        return session;
    }

    /**
     * Sauce Labs relays a session's CDP endpoint through its own tunnel/proxy, which does not
     * expose a usable raw CDP connection for SeleniumCdpConnection to attach to (see
     * sauce-cdp-check's standalone reproduction) - so fail fast with a clear message instead of
     * letting the attach hang or fail later with an opaque error.
     *
     * Sauce Labs sessions don't reliably echo back a top-level "sauce:options"-style capability
     * key, so a shallow key scan misses them. The one marker observed consistently in practice is
     * buried in a nested value instead - e.g. chrome.userDataDir being under
     * "C:\Users\sauce\AppData\..." - so this scans the whole capabilities structure (keys and
     * values, recursing into nested maps/lists) for a "sauce" substring rather than just top-level keys.
     */
    private static boolean isSauceLabs(org.openqa.selenium.Capabilities capabilities) {
        if (capabilities == null) {
            return false;
        }
        return containsSauceMarker(capabilities.asMap());
    }

    private static boolean containsSauceMarker(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (containsSauceMarker(entry.getKey()) || containsSauceMarker(entry.getValue())) {
                    return true;
                }
            }
            return false;
        }
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                if (containsSauceMarker(item)) {
                    return true;
                }
            }
            return false;
        }
        return value.toString().toLowerCase().contains("sauce");
    }

    @SuppressWarnings("unchecked")
    private void enableNetwork(Duration timeout) {
        logger.info("[raw-cdp] sending Network.enable for sessionId=" + sessionId);
        try {
            Map<String, Object> result = connection.sendAndWait(sessionId, new Command<>("Network.enable", Map.of(), Map.class), timeout);
            logger.info("[raw-cdp] Network.enable raw result: " + result);
        } catch (Exception e) {
            logger.warn("[raw-cdp] Network.enable threw: " + e);
            throw e;
        }
    }

    /** requestWillBeSent params: requestId, request:{url, method, headers, postData?}, timestamp */
    @SuppressWarnings("unchecked")
    public void onRequestWillBeSent(BiConsumer<Long, Map<String, Object>> handler) {
        logger.info("[raw-cdp] registering listener for Network.requestWillBeSent");
        connection.addListener(new Event<>("Network.requestWillBeSent", input -> (Map<String, Object>) input.read(Map.class)), (seq, params) -> {
            try {
                Map<String, Object> request = (Map<String, Object>) params.get("request");
                logger.info("[raw-cdp] Network.requestWillBeSent seq=" + seq + " requestId=" + params.get("requestId")
                        + " url=" + (request == null ? null : request.get("url"))
                        + " method=" + (request == null ? null : request.get("method")));
            } catch (Exception e) {
                logger.warn("[raw-cdp] failed to log requestWillBeSent event: " + e);
            }
            handler.accept(seq, params);
        });
    }

    /** responseReceived params: requestId, response:{url, status, headers}, timestamp */
    @SuppressWarnings("unchecked")
    public void onResponseReceived(BiConsumer<Long, Map<String, Object>> handler) {
        logger.info("[raw-cdp] registering listener for Network.responseReceived");
        connection.addListener(new Event<>("Network.responseReceived", input -> (Map<String, Object>) input.read(Map.class)), (seq, params) -> {
            try {
                Map<String, Object> response = (Map<String, Object>) params.get("response");
                logger.info("[raw-cdp] Network.responseReceived seq=" + seq + " requestId=" + params.get("requestId")
                        + " url=" + (response == null ? null : response.get("url"))
                        + " status=" + (response == null ? null : response.get("status")));
            } catch (Exception e) {
                logger.warn("[raw-cdp] failed to log responseReceived event: " + e);
            }
            handler.accept(seq, params);
        });
    }

    @SuppressWarnings("unchecked")
    public Optional<String> getResponseBody(String requestId, Duration timeout) {
        logger.info("[raw-cdp] sending Network.getResponseBody for requestId=" + requestId);
        try {
            Map<String, Object> result = connection.sendAndWait(
                    sessionId,
                    new Command<>("Network.getResponseBody", Map.of("requestId", requestId), Map.class),
                    timeout);
            logger.info("[raw-cdp] Network.getResponseBody raw result keys: " + (result == null ? null : result.keySet()));
            Object body = result == null ? null : result.get("body");
            return Optional.ofNullable(body == null ? null : body.toString());
        } catch (Exception e) {
            logger.warn("[raw-cdp] Network.getResponseBody threw for requestId=" + requestId + ": " + e);
            return Optional.empty();
        }
    }

    @Override
    public void close() {
        logger.info("[raw-cdp] closing connection");
        try {
            connection.close();
        } catch (Exception e) {
            logger.warn("[raw-cdp] error closing connection: " + e);
        }
    }
}
