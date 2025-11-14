package com.testsigma.addons.web;


import com.testsigma.addons.web.utils.OutlookMailHelper;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;
import org.openqa.selenium.NoSuchElementException;

@Data
@lombok.EqualsAndHashCode(callSuper = false)
@Action(
        actionText = "Search the latest message-count emails from the outlook mailbox using client id client-id, tenant id " +
                "tenant-id, user name username, password password-input and using regex regex-pattern to filter" +
                "and store result into the variable latestEmailContent",
        description = "This action searches through the latest n emails from the specified mailbox and uses regex to filter the content.",
        applicationType = ApplicationType.WEB)
public class SearchLatestNEmailsWithRegex extends WebAction {

    @TestData(reference = "client-id")
    private com.testsigma.sdk.TestData clientId;
    @TestData(reference = "tenant-id")
    private com.testsigma.sdk.TestData tenantId;
    @TestData(reference = "username")
    private com.testsigma.sdk.TestData username;
    @TestData(reference = "password-input")
    private com.testsigma.sdk.TestData passwordInput;
    @TestData(reference = "regex-pattern")
    private com.testsigma.sdk.TestData regexPattern;
    @TestData(reference = "message-count")
    private com.testsigma.sdk.TestData messageCount;

    @TestData(reference = "content",allowedValues = {"Full-content","Subject","Body","From","To"})
    private com.testsigma.sdk.TestData content;

    @TestData(reference = "latestEmailContent",isRuntimeVariable = true)
    private com.testsigma.sdk.TestData latestEmailContent;

    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    protected Result execute() throws NoSuchElementException {
        Result result = Result.SUCCESS;

        try {
            logger.info("=== Starting to search latest n emails with regex ===");
            String password_value = passwordInput.getValue().toString();
            String client_id_value = clientId.getValue().toString();
            String tenant_id_value = tenantId.getValue().toString();
            String username_value = username.getValue().toString();
            String content_value = content.getValue().toString();
            String regex_pattern_value = regexPattern.getValue().toString();
            String message_count_value = messageCount.getValue().toString();
            
            // Parse message count
            int messageCountInt;
            try {
                messageCountInt = Integer.parseInt(message_count_value.trim());
                if (messageCountInt < 1) {
                    logger.warn("Message count must be at least 1, got: " + messageCountInt);
                    setErrorMessage("Message count must be at least 1.");
                    return Result.FAILED;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid message count format: " + message_count_value);
                setErrorMessage("Invalid message count format. Please provide a valid number.");
                return Result.FAILED;
            }
            
            logger.info(String.format("Client ID: %s, Tenant ID: %s, Username: %s, Content type: %s, Message count: %d",
                    client_id_value, tenant_id_value, username_value, content_value, messageCountInt));
            logger.info("Regex pattern: " + regex_pattern_value);
            logger.info("Runtime variable: " + latestEmailContent.getValue().toString());

            // Use OutlookMailHelper to search through latest n emails with regex
            OutlookMailHelper outlookMailHelper = new OutlookMailHelper(logger);
            String emailContent = outlookMailHelper.searchLatestNEmailsWithRegex(client_id_value, tenant_id_value,
                    username_value, password_value, content_value, regex_pattern_value, messageCountInt);

            if (emailContent == null || emailContent.isEmpty()) {
                logger.warn("Email content is null or empty");
                setErrorMessage("Failed to retrieve email content. The content may be empty or no match found.");
                return Result.FAILED;
            }

            // Store the email content in the runtime variable
            logger.info("=== Storing email content in runtime variable ===");
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(emailContent);
            runTimeData.setKey(latestEmailContent.getValue().toString());
            logger.info("Successfully stored email content in runtime variable: <b>" +
                    latestEmailContent.getValue().toString() + " = " + emailContent + "</b>");

            setSuccessMessage("Successfully searched through latest " + messageCountInt + " emails and stored " + content_value + 
                    " into the variable <b>" + latestEmailContent.getValue().toString() + " = " + emailContent + "</b>");
            logger.info("=== Successfully completed searching latest n emails with regex ===");
        } catch (Exception e) {
            result = Result.FAILED;
            logger.warn("Error occurred while searching latest n emails: " + e.getMessage());
            logger.warn("Error class: " + e.getClass().getName());
            setErrorMessage("An error occurred while searching latest n emails: " + e.getMessage());
        }
        return result;
    }
}

