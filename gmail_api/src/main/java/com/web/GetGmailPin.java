package com.web;


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
@Action(actionText = "Get value from gmail using EmailID and Password and using regex testdata to store the value in a runtime variable var1",
        description = "Get value from gmail using EmailID and Password and using regex testdata to store the value in a runtime variable var1",
        applicationType = ApplicationType.WEB)

public class GetGmailPin extends WebAction {

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
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
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
                // content is axxlready a Multipart object, so just cast it and process the body part
                Multipart multipart = (Multipart) content;
                bodyPart = multipart.getBodyPart(0);
                Object bodycontent = bodyPart.getContent();
                if (bodycontent instanceof String) {
                    FullMessage = (String) bodycontent;

                }else if (bodycontent instanceof Multipart) {
                	 Multipart bodymultipart = (Multipart) bodycontent;
                	 logger.info("Body content"+ bodymultipart.getBodyPart(0).getContent());
                     logger.info("Body content type:"+ bodymultipart.getContentType());
                	 FullMessage = (String) bodymultipart.getBodyPart(0).getContent();
                	
                }
				/*
				 * logger.info("Body content"+ bodyPart.getContent());
				 * logger.info("Body content type:"+ bodyPart.getContentType()); FullMessage =
				 * (String) bodyPart.getContent(); logger.info("Full message"+ FullMessage);
				 */
                // process the body part as needed
            } else {
                System.out.println("No content");
                logger.info("NO CONTENT");
            }


//            Object content = latestMessage.getContent();
//            Multipart multipart = (Multipart) content;
//            BodyPart bodyPart = multipart.getBodyPart(0);

          //  String FullMessage = (String) bodyPart.getContent();
            logger.info(FullMessage);
            //System.out.println(FullMessage);
            Pattern pattern = Pattern.compile(regex.getValue().toString()); //(?s)PIN\s*(\d+)
            Matcher matcher = pattern.matcher(FullMessage);

            if (matcher.find()) {
                String otp = matcher.group(0);
                setSuccessMessage("PIN is "+otp+"; And stored in runtime variable: " + var1.getValue().toString());
                logger.info("OTP is "+otp);
                runTimeData = new com.testsigma.sdk.RunTimeData();
                runTimeData.setValue(otp);
                runTimeData.setKey(var1.getValue().toString());
            } else {
                result = Result.FAILED;
                setErrorMessage("NOT an OTP. Check if Regex is correct");
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {

            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Could not get the PIN . The error is "+e.getMessage());
            logger.warn(e.getMessage());
        }
        return result;
    }
}

