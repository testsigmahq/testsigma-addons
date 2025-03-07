package com.android;

import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import javax.mail.*;
import java.util.Properties;

@Data
@Action(actionText = "Get complete email content from Gmail using EmailID and Password and store it in a runtime variable var1",
        description = "Get complete email content from Gmail using EmailID and Password and store it in a runtime variable var1",
        applicationType = ApplicationType.ANDROID)

public class GetGmailContent extends AndroidAction {

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;
    @TestData(reference = "Password")
    private com.testsigma.sdk.TestData Password;
    @TestData(reference = "var1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");
        String host = "imap.gmail.com";
        String port = "993";
        String username = EmailID.getValue().toString();
        String password = Password.getValue().toString();
        logger.info("username: " + username);
        logger.info("password: " + password);
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", port);
        props.setProperty("mail.imaps.auth", "true");
        props.setProperty("mail.imaps.starttls.enable", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        logger.info("properties fixed");
        try {

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            logger.info("");

            Store store = session.getStore("imaps");
            store.connect(host, username, password);
            logger.info("store ");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();
            logger.info("messages: " + messages.length);
            Message latestMessage = messages[messages.length - 1];
            logger.info("latestMessage" + latestMessage.getContent());
            Object content = latestMessage.getContent();
            BodyPart bodyPart = null;
            String FullMessage = null;
            if (content instanceof String) {
                System.out.println(content);
                FullMessage = (String) content;

            } else if (content instanceof Multipart) {
                // content is already a Multipart object, so just cast it and process the body part
                Multipart multipart = (Multipart) content;
                bodyPart = multipart.getBodyPart(0);
                Object bodycontent = bodyPart.getContent();
                if (bodycontent instanceof String) {
                    FullMessage = (String) bodycontent;

                } else if (bodycontent instanceof Multipart) {
                    Multipart bodymultipart = (Multipart) bodycontent;
                    logger.info("Body content" + bodymultipart.getBodyPart(0).getContent());
                    logger.info("Body content type:" + bodymultipart.getContentType());
                    FullMessage = (String) bodymultipart.getBodyPart(0).getContent();
                }
            } else {
                System.out.println("No content");
                logger.info("NO CONTENT");
            }

            logger.info(FullMessage);
            runTimeData = new com.testsigma.sdk.RunTimeData();
            runTimeData.setValue(FullMessage);
            runTimeData.setKey(var1.getValue().toString());
            setSuccessMessage("Email content stored in runtime variable: " + var1.getValue().toString() + "and value: " + FullMessage);

            inbox.close(false);
            store.close();

        } catch (Exception e) {

            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Could not retrieve the email content. The error is " + e.getMessage());
            logger.warn(e.getMessage());
        }
        return result;
    }
}