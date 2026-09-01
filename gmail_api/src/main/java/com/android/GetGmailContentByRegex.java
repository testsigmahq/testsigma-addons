package com.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import utils.GmailUtils;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "Get email content from Gmail using EmailID and Password " +
        "filtered by filter-criteria with value filter-value " +
        "and extract matching content using regex-pattern and store it in a runtime variable var1",
        description = "Get email content from Gmail filtered by From address, To address, or Subject, " +
                "then extract the portion matching the given regex pattern and store it in a runtime variable",
        applicationType = ApplicationType.ANDROID)
public class GetGmailContentByRegex extends AndroidAction {

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;

    @TestData(reference = "Password")
    private com.testsigma.sdk.TestData Password;

    @TestData(reference = "filter-criteria",
            allowedValues = {"From", "To", "Subject"})
    private com.testsigma.sdk.TestData filterCriteria;

    @TestData(reference = "filter-value")
    private com.testsigma.sdk.TestData filterValue;

    @TestData(reference = "regex-pattern")
    private com.testsigma.sdk.TestData regexPattern;

    @TestData(reference = "var1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var1;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        String username = GmailUtils.sanitizeUsername(EmailID.getValue().toString());
        String password = GmailUtils.sanitizePassword(Password.getValue().toString());
        String criteria = filterCriteria.getValue().toString().trim();
        String value = filterValue.getValue().toString().trim();
        String regex = regexPattern.getValue().toString();

        logger.info("username: " + username);
        logger.info("Filter criteria: " + criteria + ", value: " + value);
        logger.info("Regex pattern: " + regex);

        Store store = null;
        Folder inbox = null;
        try {
            store = GmailUtils.connectToGmail(username, password, logger);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            SearchTerm searchTerm = GmailUtils.buildSearchTerm(criteria, value);
            Message[] messages = inbox.search(searchTerm);
            logger.info("Found " + messages.length + " messages matching filter: " + criteria + " = " + value);

            if (messages.length == 0) {
                setErrorMessage("No emails found in INBOX matching " + criteria + ": '" + value +
                        "'. Please verify the filter criteria and value are correct.");
                return Result.FAILED;
            }

            Message latestMessage = messages[messages.length - 1];
            logger.info("Subject: " + latestMessage.getSubject());

            String fullMessage = GmailUtils.extractContent(latestMessage.getContent());

            if (fullMessage == null || fullMessage.trim().isEmpty()) {
                setErrorMessage("Email matching " + criteria + ": '" + value +
                        "' was found (Subject: " + latestMessage.getSubject() +
                        ") but the email body is empty or could not be read.");
                return Result.FAILED;
            }

            logger.info("Extracted content length: " + fullMessage.length());

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(fullMessage);

            if (matcher.find()) {
                String matchedContent = matcher.group(0);
                logger.info("Regex matched: " + matchedContent);

                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(matchedContent);
                runTimeData.setKey(var1.getValue().toString());
                setSuccessMessage("Matched content stored in variable: " + var1.getValue().toString() +
                        ", value: " + matchedContent);
                return Result.SUCCESS;
            } else {
                setErrorMessage("Email found (Subject: " + latestMessage.getSubject() +
                        ") but no content matching regex '" + regex +
                        "' was found in the email body. Body length: " + fullMessage.length() + " chars.");
                return Result.FAILED;
            }

        } catch (java.util.regex.PatternSyntaxException e) {
            setErrorMessage("Invalid regex pattern '" + regex + "': " + e.getMessage());
            logger.warn("Invalid regex: " + e.getMessage());
            return Result.FAILED;
        } catch (AuthenticationFailedException e) {
            setErrorMessage("Gmail authentication failed for '" + username +
                    "'. Please verify: 1) App password is valid 2) 2-Step Verification is ON 3) IMAP is enabled in Gmail settings. Error: " + e.getMessage());
            logger.warn("Authentication failed: " + e.getMessage());
            return Result.FAILED;
        } catch (IllegalArgumentException e) {
            setErrorMessage(e.getMessage());
            logger.warn(e.getMessage());
            return Result.FAILED;
        } catch (Exception e) {
            setErrorMessage("Failed to retrieve email content. Error: " + e.getMessage());
            logger.warn(e.getMessage());
            return Result.FAILED;
        } finally {
            GmailUtils.closeQuietly(inbox, store);
        }
    }
}
