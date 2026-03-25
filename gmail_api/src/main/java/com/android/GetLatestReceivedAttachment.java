package com.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.NoSuchElementException;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = false)
@Action(actionText = "Get attachment from Gmail username username-value passcode passcode-value subject-filter subject-filter-value subject-match-type subject-match-type-value at index attachment-index (Starting from 1) and store path in runtime variable variable-name", 
        description = "Gets the attachment at the given 1-based index from the single newest inbox message only (does not search older mail). Subject filter, if set, must match that message using contains, startsWith, endsWith, or regex. Stores the file path in a runtime variable.", 
        applicationType = ApplicationType.ANDROID,
        useCustomScreenshot = false
)
public class GetLatestReceivedAttachment extends AndroidAction {
  @TestData(reference = "username-value")
  private com.testsigma.sdk.TestData usernameVal;

  @TestData(reference = "passcode-value")
  private com.testsigma.sdk.TestData passcodeVal;

  @TestData(reference = "subject-filter-value")
  private com.testsigma.sdk.TestData subjectFilter;

  @TestData(reference = "subject-match-type-value", allowedValues = { "contains", "startsWith", "endsWith", "regex" })
  private com.testsigma.sdk.TestData subjectMatchType;
  
  @TestData(reference = "attachment-index")
  private com.testsigma.sdk.TestData attachmentIndex;

  @TestData(reference = "variable-name", isRuntimeVariable = true)
  private com.testsigma.sdk.TestData variableName;

  @RunTimeData
  private com.testsigma.sdk.RunTimeData runTimeData;
  
  @Override
  public com.testsigma.sdk.Result execute() throws NoSuchElementException {
    com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
    logger.info("Initiating execution");

    String host = "imap.gmail.com";
    String username = usernameVal.getValue().toString();
    String password = passcodeVal.getValue().toString();

    // Subject filter (blank = match all)
    String filterRaw = "";
    try {
      filterRaw = subjectFilter.getValue().toString().trim();
    } catch (Exception e) {
      logger.warn("Could not read subject-filter, matching all emails: " + e.getMessage());
    }
    logger.info("Subject filter: " + (filterRaw.isEmpty() ? "(none)" : filterRaw));

    String matchType = "contains";
    Pattern subjectPattern = null;
    String filterLower = filterRaw.toLowerCase();
    try {
      if (subjectMatchType != null && subjectMatchType.getValue() != null) {
        String inputMatchType = subjectMatchType.getValue().toString().trim();
        if (!inputMatchType.isEmpty()) {
          matchType = inputMatchType;
        }
      }
    } catch (Exception e) {
      logger.warn("Could not read subject-match-type, defaulting to contains: " + e.getMessage());
    }

    if ("regex".equalsIgnoreCase(matchType) && !filterRaw.isEmpty()) {
      subjectPattern = Pattern.compile(filterRaw, Pattern.CASE_INSENSITIVE);
    }

    // Parse 1-based attachment index (default 1)
    int requestedIndex = 1;
    try {
      String idxStr = attachmentIndex.getValue().toString().trim();
      if (!idxStr.isEmpty()) {
        requestedIndex = Integer.parseInt(idxStr);
      }
    } catch (Exception e) {
      logger.warn("Could not parse attachment-index, defaulting to 1: " + e.getMessage());
    }
    if (requestedIndex < 1) {
      logger.warn("attachment-index must be >= 1, resetting to 1.");
      requestedIndex = 1;
    }
    logger.info("Requested attachment index (1-based): " + requestedIndex);

    // Use system temp dir — always exists on any OS / cloud server
    String downloadDir = System.getProperty("java.io.tmpdir") + "/testsigma_attachments/";
    new File(downloadDir).mkdirs();

    Folder inbox = null;
    Store store = null;
    String filePath = "";
    try {
      Properties props = new Properties();
      props.put("mail.store.protocol", "imaps");
      props.put("mail.imaps.host", "imap.gmail.com");
      props.put("mail.imaps.port", "993");
      props.put("mail.imaps.ssl.enable", "true");

      Session session = Session.getInstance(props);

      store = session.getStore("imaps");
      store.connect(host, username, password);

      inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_ONLY);

      int totalMessages = inbox.getMessageCount();
      logger.info("Inbox message count: " + totalMessages);

      if (totalMessages < 1) {
        String reason = "Inbox is empty; no latest message to check.";
        logger.info(reason);
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage(reason);
        return result;
      }

      // Only the newest message (highest sequence number) — do not scan older mail
      Message message = inbox.getMessage(totalMessages);
      String subject = message.getSubject();
      if (subject == null)
        subject = "";

      logger.info("Checking latest message only. Subject: \"" + subject + "\"");

      if (!filterRaw.isEmpty()) {
        String subjectLower = subject.toLowerCase();
        boolean subjectMatches;
        if ("startswith".equalsIgnoreCase(matchType)) {
          subjectMatches = subjectLower.startsWith(filterLower);
        } else if ("endswith".equalsIgnoreCase(matchType)) {
          subjectMatches = subjectLower.endsWith(filterLower);
        } else if ("regex".equalsIgnoreCase(matchType)) {
          subjectMatches = subjectPattern.matcher(subject).find();
        } else {
          subjectMatches = subjectLower.contains(filterLower);
        }
        if (!subjectMatches) {
          String reason = "Latest email subject does not match filter \"" + filterRaw + "\".";
          logger.info(reason);
          result = com.testsigma.sdk.Result.FAILED;
          setErrorMessage(reason);
          return result;
        }
      }

      logger.info("Subject matched: \"" + subject + "\"");

      if (!message.isMimeType("multipart/*")) {
        String reason = "Latest email is not multipart (no attachments in this message).";
        logger.warn(reason);
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage(reason);
        return result;
      }

      Multipart multipart = (Multipart) message.getContent();

      List<MimeBodyPart> attachments = new ArrayList<>();
      for (int i = 0; i < multipart.getCount(); i++) {
        BodyPart bodyPart = multipart.getBodyPart(i);
        if (bodyPart.getFileName() != null) {
          attachments.add((MimeBodyPart) bodyPart);
        }
      }

      if (attachments.isEmpty()) {
        String reason = "Latest email has no attachments.";
        logger.info(reason);
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage(reason);
        return result;
      }

      logger.info("Email has " + attachments.size() + " attachment(s). "
          + "Valid indices: 1 to " + attachments.size());

      if (requestedIndex > attachments.size()) {
        result = com.testsigma.sdk.Result.FAILED;
        setErrorMessage("Attachment index " + requestedIndex + " is out of range. "
            + "This email has " + attachments.size() + " attachment(s) "
            + "(valid indices: 1 to " + attachments.size() + ").");
        logger.warn(getErrorMessage());
        return result;
      }

      MimeBodyPart target = attachments.get(requestedIndex - 1);
      filePath = downloadDir + target.getFileName();
      target.saveFile(new File(filePath));
      logger.info("Attachment [" + requestedIndex + "] downloaded at: " + filePath);

      runTimeData.setKey(variableName.getValue().toString());
      runTimeData.setValue(filePath);
      setSuccessMessage("Attachment [" + requestedIndex + "] downloaded. File path: " + filePath);

    } catch (Exception e) {
      logger.warn("Exception occurred while executing test step: " + ExceptionUtils.getStackTrace(e));
      result = com.testsigma.sdk.Result.FAILED;
      setErrorMessage("Exception: " + ExceptionUtils.getMessage(e));
    } finally {
      try {
        if (inbox != null && inbox.isOpen())
          inbox.close(false);
      } catch (MessagingException e) {
        logger.warn("Failed to close inbox: " + e.getMessage());
      }
      try {
        if (store != null && store.isConnected())
          store.close();
      } catch (MessagingException e) {
        logger.warn("Failed to close store: " + e.getMessage());
      }
    }
    return result;
  }
}