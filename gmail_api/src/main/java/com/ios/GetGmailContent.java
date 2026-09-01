package com.ios;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import utils.GmailUtils;

import javax.mail.*;

@Data
@Action(actionText = "Get complete email content from Gmail using EmailID and Password and store it in a runtime variable var1",
        description = "Get complete email content from Gmail using EmailID and Password and store it in a runtime variable var1",
        applicationType = ApplicationType.IOS)
public class GetGmailContent extends IOSAction {

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;
    @TestData(reference = "Password")
    private com.testsigma.sdk.TestData Password;
    @TestData(reference = "var1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        logger.info("Initiating execution");
        String username = GmailUtils.sanitizeUsername(EmailID.getValue().toString());
        String password = GmailUtils.sanitizePassword(Password.getValue().toString());
        logger.info("username: " + username);

        Store store = null;
        Folder inbox = null;
        try {
            store = GmailUtils.connectToGmail(username, password, logger);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();
            logger.info("messages: " + messages.length);

            if (messages.length == 0) {
                setErrorMessage("No emails found in the INBOX for '" + username + "'. The mailbox is empty.");
                return Result.FAILED;
            }

            Message latestMessage = messages[messages.length - 1];
            logger.info("latestMessage subject: " + latestMessage.getSubject());

            String fullMessage = GmailUtils.extractContent(latestMessage.getContent());

            if (fullMessage == null || fullMessage.trim().isEmpty()) {
                setErrorMessage("Latest email (Subject: " + latestMessage.getSubject() +
                        ") was found but the email body is empty or could not be read.");
                return Result.FAILED;
            }

            logger.info("Extracted content length: " + fullMessage.length());
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(fullMessage);
            runTimeData.setKey(var1.getValue().toString());
            setSuccessMessage("Email content stored in runtime variable: " + var1.getValue().toString() +
                    " and value: " + fullMessage);
            return Result.SUCCESS;

        } catch (AuthenticationFailedException e) {
            setErrorMessage("Gmail authentication failed for '" + username +
                    "'. Please verify: 1) App password is valid 2) 2-Step Verification is ON 3) IMAP is enabled in Gmail settings. Error: " + e.getMessage());
            logger.warn("Authentication failed: " + e.getMessage());
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
