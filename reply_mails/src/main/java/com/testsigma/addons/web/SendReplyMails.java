package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.annotation.Mailbox;
import com.testsigma.sdk.MailboxMessage;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.*;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Action(actionText = "Reply to the mail with body that contains the subject in mail-name mail-box ",
        description = "Reply to the mail that matches the subject in the mail box",
        applicationType = ApplicationType.WEB)
public class SendReplyMails extends WebAction {

    private static final String SUCCESS_MESSAGE = "Successfully replied to the mail.";
    private static final String MAIL_ERROR_MESSAGE = "Mail contains the given subject not found";

    @TestData(reference = "body")
    private com.testsigma.sdk.TestData body;
    @TestData(reference = "subject")
    private com.testsigma.sdk.TestData subject;
    @TestData(reference = "mail-name")
    private com.testsigma.sdk.TestData mail;

    @Mailbox
    private com.testsigma.sdk.Mailbox mailbox;

    @Email
    private com.testsigma.sdk.Email email;

    @Override
    protected Result execute() throws NoSuchElementException {
        mailbox.setEmail(mail.toString());
        try {
            List<MailboxMessage> messageList = mailbox.getMessages();
            MailboxMessage requiredMessage = null;
            for (MailboxMessage message : messageList) {
                if (message.getSubject().contains(subject.toString())) {
                        requiredMessage = message;
                        break;
                }
            }
            if(requiredMessage == null){
                setErrorMessage(MAIL_ERROR_MESSAGE);
                return Result.FAILED;
            }
            String messageId = email.getMessageId(requiredMessage.getHeaders());
            Map<String, String> headersList = new HashMap<>();
            headersList.put("In-Reply-To", messageId);
            headersList.put("References", messageId);

            List<String> to = new ArrayList<>();
            to.add(requiredMessage.getReceivedFrom());
            email.setTo(to);
            email.setSubject("Re:" + subject.toString());
            email.setBody(body.toString());
            email.addHeader(headersList);
            email.send();
            setSuccessMessage(SUCCESS_MESSAGE);
            return Result.SUCCESS;
        } catch (Exception e) {
            setErrorMessage(e.toString());
            return Result.FAILED;
        }
    }
}
