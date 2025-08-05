package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.MailboxMessage;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.Mailbox;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Action(actionText = "Get email content matching the regex regex-condition from mail box using EmailID " +
        "with filter-type filter contains text test-data and store it in a runtime variable var1",
        description = "Get complete email content from Gmail using EmailID and Password and" +
                " store it in a runtime variable var1",
        applicationType = ApplicationType.WEB)
public class GetGmailContent extends WebAction {

    @Mailbox
    private com.testsigma.sdk.Mailbox mailbox;

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;
    @TestData(reference = "test-data")
    private com.testsigma.sdk.TestData testData;
    @TestData(reference = "regex-condition")
    private com.testsigma.sdk.TestData regexCondition;
    @TestData(reference = "filter-type", allowedValues = {"subject","sentTo","receivedFrom"})
    private com.testsigma.sdk.TestData filterType;
    @TestData(reference = "var1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {
        Result result = Result.SUCCESS;
        try {
            // Set the email ID for the mailbox
            logger.info("email id " + EmailID.getValue().toString());
            mailbox.setEmail(EmailID.getValue().toString());
            
            // Get all messages from the mailbox
            logger.info("getting messages from mailbox");
            List<MailboxMessage> messages = mailbox.getMessages();
            logger.info("messages " + messages.size());

            // print the message content
            try {
                for( MailboxMessage m : messages ) {
                    logger.info("message : " + m.getSubject());
                    logger.info("content : " + m.getTextMessage());
                    logger.info("from : " + m.getReceivedFrom());
                }
            } catch (Exception e) {
                //
                logger.info("ignoring messages");
            }
            // Filter messages based on the filterType and testData
            List<MailboxMessage> filteredMessages = messages.stream()
                .filter(message -> {
                    String filterValue = testData.getValue().toString();
                    String currentFilterType = filterType.getValue().toString();
                    
                    switch (currentFilterType.toLowerCase()) {
                        case "subject":
                            return message.getSubject() != null && 
                                   message.getSubject().contains(filterValue);
                        case "sentto":
                            return message.getSentTo() != null && 
                                   message.getSentTo().contains(filterValue);
                        case "receivedfrom":
                            return message.getReceivedFrom() != null && 
                                   message.getReceivedFrom().contains(filterValue);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
            logger.info("filtered messages " + filteredMessages.size());
            // Extract content based on regex condition
            String output = "";
            if (!filteredMessages.isEmpty()) {
                MailboxMessage matchedMessage = filteredMessages.get(0);
                String messageContent = matchedMessage.getTextMessage();
                logger.info("message content " + messageContent);
                if (messageContent != null && !regexCondition.getValue().toString().isEmpty()) {
                    // Apply regex pattern to extract desired content
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        regexCondition.getValue().toString(), 
                        java.util.regex.Pattern.DOTALL
                    );
                    java.util.regex.Matcher matcher = pattern.matcher(messageContent);
                    
                    if (matcher.find()) {
                        output = matcher.group();
                    } else {
                        // If no regex match found, return the full content
                        output = messageContent;
                    }
                } else {
                    // If no regex condition or content is null, return the full content
                    output = messageContent != null ? messageContent : "";
                }
            }
            logger.info("output " + output);
            
            // Store the result in runtime variable
            if(output != null && !output.isEmpty()){
                logger.info("setting runtime variable " + output);
                runTimeData.setValue(output);
                runTimeData.setKey(var1.getValue().toString());
            } else {
                logger.info("No content found in the email");
                setErrorMessage("No content found in the email");
                return Result.FAILED;
            }
            logger.info("stored email content successfully");
            setSuccessMessage("Email content fetched successfully");
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.info("error : " + ExceptionUtils.getStackTrace(e));
            return Result.FAILED;
        }
    }
}