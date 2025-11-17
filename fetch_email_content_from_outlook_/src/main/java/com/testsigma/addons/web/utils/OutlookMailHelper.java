package com.testsigma.addons.web.utils;

import com.microsoft.aad.msal4j.*;
import com.testsigma.sdk.Logger;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.URI;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutlookMailHelper {
    Logger logger;
    private static final String DEFAULT_EWS_URL = "https://outlook.office365.com/EWS/Exchange.asmx";
    private static final String EWS_SCOPE = "https://outlook.office365.com/.default";

    public OutlookMailHelper(Logger logger) {
        this.logger = logger;
    }

    /**
     * Acquires an EWS access token using username/password authentication (ROPC flow)
     */
    public String acquireEwsToken(String clientId, String tenantId, String username, String password) throws Exception {
        try {
            PublicClientApplication app = PublicClientApplication.builder(clientId)
                    .authority("https://login.microsoftonline.com/" + tenantId)
                    .build();

            IAuthenticationResult result = app.acquireToken(UserNamePasswordParameters.builder(
                    Collections.singleton(EWS_SCOPE), username, password.toCharArray()).build()).get();
            
            if (result == null || result.accessToken() == null || result.accessToken().isEmpty()) {
                throw new Exception("Failed to acquire access token");
            }
            return result.accessToken();
        } catch (Exception e) {
            logger.warn("Exception in acquireEwsToken(): " + ExceptionUtils.getMessage(e));
            throw e;
        }
    }

    /**
     * Creates an ExchangeService instance with OAuth bearer token authentication
     */
    private ExchangeService createExchangeService(String accessToken, String ewsUrl) throws Exception {
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        service.getHttpHeaders().put("Authorization", "Bearer " + accessToken);
        service.setUrl(new URI(ewsUrl));
        return service;
    }

    /**
     * Creates and returns an ExchangeService instance with authentication
     */
    private ExchangeService getExchangeService(String clientId, String tenantId, String username, String password) throws Exception {
        return createExchangeService(acquireEwsToken(clientId, tenantId, username, password), DEFAULT_EWS_URL);
    }

    /**
     * Fetches emails from inbox
     */
    private FindItemsResults<Item> fetchEmails(ExchangeService service, int count) throws Exception {
        FindItemsResults<Item> results = service.findItems(WellKnownFolderName.Inbox, new ItemView(count));
        if (results == null || results.getItems() == null || results.getItems().isEmpty()) {
            throw new Exception("No emails found in the inbox");
        }
        return results;
    }

    /**
     * Applies regex pattern to content and returns matched result
     */
    private String applyRegex(String content, String regexPattern) throws Exception {
        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            throw new Exception("Regex pattern is required");
        }
        Matcher matcher = Pattern.compile(regexPattern).matcher(content);
        if (!matcher.find()) {
            throw new Exception("Regex pattern did not match any content. Pattern: " + regexPattern);
        }
        return matcher.groupCount() > 0 ? matcher.group(1) : matcher.group(0);
    }

    /**
     * Gets the latest email content from Outlook mailbox
     * @param clientId Azure AD client ID
     * @param tenantId Azure AD tenant ID
     * @param username Email username
     * @param password Email password
     * @param contentType Content type to retrieve: "Full-content", "Subject", "Body", "From", "To"
     * @return The requested email content
     */
    public String getLatestEmailContent(String clientId, String tenantId, String username, String password, String contentType) throws Exception {
        ExchangeService service = getExchangeService(clientId, tenantId, username, password);
        FindItemsResults<Item> results = fetchEmails(service, 1);
        EmailMessage email = EmailMessage.bind(service, results.getItems().get(0).getId());
        return extractEmailContent(email, contentType);
    }

    /**
     * Gets the latest email content from Outlook mailbox and applies regex pattern to extract matching content
     * @param clientId Azure AD client ID
     * @param tenantId Azure AD tenant ID
     * @param username Email username
     * @param password Email password
     * @param contentType Content type to retrieve: "Full-content", "Subject", "Body", "From", "To"
     * @param regexPattern Regex pattern to apply on the extracted content
     * @return The regex-matched email content
     */
    public String getLatestEmailContentWithRegex(String clientId, String tenantId, String username, String password, String contentType, String regexPattern) throws Exception {
        return searchLatestNEmailsWithRegex(clientId, tenantId, username, password, contentType, regexPattern, 1);
    }

    /**
     * Searches through the latest n emails and applies regex pattern to extract matching content
     * @param clientId Azure AD client ID
     * @param tenantId Azure AD tenant ID
     * @param username Email username
     * @param password Email password
     * @param contentType Content type to retrieve: "Full-content", "Subject", "Body", "From", "To"
     * @param regexPattern Regex pattern to apply on the extracted content
     * @param messageCount Number of latest messages to search through
     * @return The regex-matched email content from the first matching email
     */
    public String searchLatestNEmailsWithRegex(String clientId, String tenantId, String username, String password, String contentType, String regexPattern, int messageCount) throws Exception {
        try {
            ExchangeService service = getExchangeService(clientId, tenantId, username, password);
            FindItemsResults<Item> results = fetchEmails(service, messageCount);
            
            for (Item item : results.getItems()) {
                try {
                    EmailMessage email = EmailMessage.bind(service, item.getId());
                    String emailContent = extractEmailContent(email, contentType);
                    if (emailContent != null && !emailContent.isEmpty()) {
                        return applyRegex(emailContent, regexPattern);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process email: " + ExceptionUtils.getMessage(e));
                }
            }
            throw new Exception("Regex pattern did not match any content in the latest " + messageCount + " emails. Pattern: " + regexPattern);
        } catch (Exception e) {
            logger.warn("Exception in searchLatestNEmailsWithRegex(): " + ExceptionUtils.getMessage(e));
            throw e;
        }
    }

    /**
     * Formats email addresses from a collection
     */
    private String formatEmailAddresses(EmailAddressCollection addresses) {
        if (addresses == null || addresses.getCount() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (EmailAddress address : addresses) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(address.getAddress());
        }
        return sb.toString();
    }

    /**
     * Extracts the requested content type from an email message
     */
    private String extractEmailContent(EmailMessage email, String contentType) throws Exception {
        switch (contentType) {
            case "Subject":
                return email.getSubject() != null ? email.getSubject() : "";
            
            case "Body":
                return email.getBody() != null ? email.getBody().toString() : "";
            
            case "From":
                return email.getFrom() != null && email.getFrom().getAddress() != null 
                    ? email.getFrom().getAddress() : "";
            
            case "To":
                return formatEmailAddresses(email.getToRecipients());
            
            case "Full-content":
                String toAddresses = formatEmailAddresses(email.getToRecipients());
                return String.format("Subject: %s\nFrom: %s\n%sBody: %s\n",
                    email.getSubject() != null ? email.getSubject() : "",
                    email.getFrom() != null && email.getFrom().getAddress() != null ? email.getFrom().getAddress() : "",
                    toAddresses.isEmpty() ? "" : "To: " + toAddresses + "\n",
                    email.getBody() != null ? email.getBody().toString() : "");
            
            default:
                logger.warn("Unknown content type: " + contentType + ", returning empty string");
                return "";
        }
    }
}
