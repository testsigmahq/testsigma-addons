package com.android;


import com.testsigma.sdk.AndroidAction;
import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.RunTimeData;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import javax.mail.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "Get URL from gmail using EmailID and Password and trim the value using regex testdata to store the value in a runtime variable var1",
        description = "Verifying the value displayed in the csv file",
        applicationType = ApplicationType.ANDROID)

public class GetURLFromMail extends AndroidAction {

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;
    @TestData(reference = "Password")
    private com.testsigma.sdk.TestData Password;
    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData regex;
    @TestData(reference = "var1", isRuntimeVariable = true)
    private com.testsigma.sdk.TestData var1;
    @RunTimeData
    private com.testsigma.sdk.RunTimeData runTimeData;

    @Override
    public Result execute() {

        Result result;
        logger.info("Initiating execution");
        String host = "imap.gmail.com";
        String port = "993";
        String username = EmailID.getValue().toString();
        String password = Password.getValue().toString();  //
        logger.info("username: "+ username);
        logger.info("password: "+ password);
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", port);
        props.setProperty("mail.imaps.auth", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        String url = "";

        try {

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

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
            String fullMessage = null;
            if (content instanceof String) {
                fullMessage = (String) content;

            } else if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                bodyPart = multipart.getBodyPart(0);
                fullMessage = (String) bodyPart.getContent();
                logger.info("Full message"+ fullMessage);
            } else {
                logger.info("NO CONTENT");
            }

            Pattern pattern = Pattern.compile(regex.getValue().toString());
            Matcher matcher = pattern.matcher(fullMessage);

            if (matcher.find()) {
                url = matcher.group(0);
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(url);
                runTimeData.setKey(var1.getValue().toString());
                logger.info("URL is "+ url);
            } else {
                logger.info(matcher.group(0));
            }
            inbox.close(false);
            store.close();

        } catch (Exception e) {
            setErrorMessage("Could not get the URL . The error is "+e.getMessage());
            logger.warn(e.getMessage());
            return Result.FAILED;
        }
        if (url.isEmpty()){
            setErrorMessage("NO URL found. Check if Regex is correct");
            return Result.FAILED;
        }
        setSuccessMessage("URL fetched successfully: "+ url+ "; And stored in runtime variable: " + var1.getValue().toString());
        logger.info("url: "+ url);
        return Result.SUCCESS;
    }
}

