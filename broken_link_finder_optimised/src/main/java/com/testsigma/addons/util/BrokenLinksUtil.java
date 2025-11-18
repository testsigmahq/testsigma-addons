package com.testsigma.addons.util;

import com.testsigma.sdk.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class BrokenLinksUtil {
    Logger logger;
    WebDriver driver;
    public BrokenLinksUtil(Logger logger, WebDriver driver) {
        this.logger = logger;
        this.driver = driver;
    }
    private static final int CONNECTION_TIMEOUT = 30000;

    /**
     * Finds all broken links in the given URL and its child pages up to the specified depth
     * @param baseUrl The starting URL to check
     * @param depth The maximum depth to traverse (0 = only base URL, 1 = base + immediate children, etc.)
     * @param connectionTimeout Connection timeout in milliseconds
     * @return Set of all broken links found
     */
    public Set<String> findAllBrokenLinksWithDepth(String baseUrl, int depth, int connectionTimeout) {
        Set<String> allBrokenLinks = new HashSet<>();
        Set<String> visitedUrls = new HashSet<>();
        if(connectionTimeout <= 0){
            connectionTimeout = CONNECTION_TIMEOUT;
        }
        
        logger.info("Starting to find broken links with depth " + depth + " from URL: " + baseUrl);
        
        try {
            // Normalize the base URL
            String normalizedBaseUrl = normalizeUrl(baseUrl);
            visitedUrls.add(normalizedBaseUrl);
            
            // Extract base domain for same-domain checking
            String baseDomain = extractBaseDomain(normalizedBaseUrl);
            
            // Recursively check pages
            checkPageRecursively(normalizedBaseUrl, depth, connectionTimeout, allBrokenLinks, visitedUrls, baseDomain);
        } catch (Exception e) {
            logger.warn("Error while finding broken links with depth: " + e.getMessage());
        }
        
        return allBrokenLinks;
    }

    /**
     * Extracts the base domain from a URL (protocol + host)
     */
    private String extractBaseDomain(String url) {
        try {
            URL urlObj = new URL(url);
            String baseDomain = urlObj.getProtocol() + "://" + urlObj.getHost();
            if (urlObj.getPort() != -1 && urlObj.getPort() != urlObj.getDefaultPort()) {
                baseDomain += ":" + urlObj.getPort();
            }
            return baseDomain;
        } catch (Exception e) {
            logger.warn("Error extracting base domain from URL: " + url + " - " + e.getMessage());
            return url;
        }
    }

    /**
     * Recursively checks a page and its child pages for broken links
     */
    private void checkPageRecursively(String url, int remainingDepth, int connectionTimeout, 
                                     Set<String> allBrokenLinks, Set<String> visitedUrls, String baseDomain) {
        try {
            logger.info("Checking page: " + url + " (remaining depth: " + remainingDepth + ")");
            
            // Navigate to the page
            driver.get(url);
            
            // Wait a bit for page to load
            Thread.sleep(1000);
            
            // Get current page URL for resolving relative URLs
            String currentPageUrl = driver.getCurrentUrl();
            
            // Find all links on the current page
            Set<WebElement> links = new HashSet<>(driver.findElements(By.tagName("a")));
            List<String> childUrls = new ArrayList<>();
            
            for (WebElement link : links) {
                String linkUrl = null;
                try {
                    linkUrl = link.getAttribute("href");
                } catch (StaleElementReferenceException e) {
                    logger.warn("Stale element encountered, skipping link: " + e.getMessage());
                    continue; // Skip this link if it's stale
                }
                
                if (linkUrl != null && !linkUrl.isEmpty()) {
                    // Convert relative URLs to absolute URLs
                    String absoluteLinkUrl = resolveUrl(linkUrl, currentPageUrl);
                    
                    // Normalize the URL
                    String normalizedLinkUrl = normalizeUrl(absoluteLinkUrl);
                    
                    // Skip if not a valid HTTP/HTTPS URL
                    if (!normalizedLinkUrl.startsWith("http://") && !normalizedLinkUrl.startsWith("https://")) {
                        continue;
                    }
                    
                    // Check if the link is broken
                    if (isBrokenLink(normalizedLinkUrl, connectionTimeout)) {
                        logger.info("Broken link found: " + normalizedLinkUrl);
                        allBrokenLinks.add(normalizedLinkUrl);
                    }
                    
                    // If we have depth remaining and this is a child page of the base domain, add it for recursive checking
                    if (remainingDepth > 0 && isSameDomain(normalizedLinkUrl, baseDomain) &&
                            !visitedUrls.contains(normalizedLinkUrl)) {
                        childUrls.add(normalizedLinkUrl);
                        visitedUrls.add(normalizedLinkUrl);
                    }
                }
            }
            
            // Recursively check child pages
            if (remainingDepth > 0) {
                for (String childUrl : childUrls) {
                    checkPageRecursively(childUrl, remainingDepth - 1, connectionTimeout, 
                                       allBrokenLinks, visitedUrls, baseDomain);
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error checking page " + url + ": " + e.getMessage());
        }
    }

    /**
     * Checks if a link is broken
     */
    private boolean isBrokenLink(String url, int connectionTimeout) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(connectionTimeout);
            connection.setInstanceFollowRedirects(false);
            int responseCode = connection.getResponseCode();
            
            // Follow redirects manually
            if (responseCode >= 300 && responseCode < 400) {
                String redirectUrl = connection.getHeaderField("Location");
                if (redirectUrl != null) {
                    return isBrokenLink(redirectUrl, connectionTimeout);
                }
            }
            
            return responseCode >= 400;
        } catch (Exception e) {
            logger.warn("Error checking link: " + url + " - " + e.getMessage());
            return true; // Consider it broken if we can't check it
        }
    }

    /**
     * Resolves a relative URL to an absolute URL
     */
    private String resolveUrl(String url, String baseUrl) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url; // Already absolute
            }
            URL base = new URL(baseUrl);
            URL resolved = new URL(base, url);
            return resolved.toString();
        } catch (Exception e) {
            logger.warn("Error resolving URL: " + url + " with base: " + baseUrl + " - " + e.getMessage());
            return url;
        }
    }

    /**
     * Normalizes a URL by removing fragments and trailing slashes
     */
    private String normalizeUrl(String url) {
        try {
            URL urlObj = new URL(url);
            String normalized = urlObj.getProtocol() + "://" + urlObj.getHost();
            if (urlObj.getPort() != -1 && urlObj.getPort() != urlObj.getDefaultPort()) {
                normalized += ":" + urlObj.getPort();
            }
            String path = urlObj.getPath();
            if (path != null && !path.isEmpty()) {
                normalized += path;
            }
            String query = urlObj.getQuery();
            if (query != null && !query.isEmpty()) {
                normalized += "?" + query;
            }
            // Remove trailing slash except for root
            if (normalized.endsWith("/") && normalized.length() > urlObj.getProtocol().length() + 3) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            return normalized;
        } catch (Exception e) {
            logger.warn("Error normalizing URL: " + url + " - " + e.getMessage());
            return url;
        }
    }

    /**
     * Checks if two URLs belong to the same domain
     */
    private boolean isSameDomain(String url1, String url2) {
        try {
            URL u1 = new URL(url1);
            URL u2 = new URL(url2);
            String host1 = u1.getHost().toLowerCase();
            String host2 = u2.getHost().toLowerCase();
            // Remove www. prefix for comparison
            if (host1.startsWith("www.")) {
                host1 = host1.substring(4);
            }
            if (host2.startsWith("www.")) {
                host2 = host2.substring(4);
            }
            return host1.equals(host2);
        } catch (Exception e) {
            logger.warn("Error comparing domains: " + url1 + " and " + url2 + " - " + e.getMessage());
            return false;
        }
    }

}
