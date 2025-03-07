package com.ios;



import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.IOSAction;
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
@Action(actionText = "Get value from gmail using EmailID and Password and trim the value after testdata upto nth digits and store the value in a runtime variable var1",
        description = "Verifying the value displayed in the csv file",
        applicationType = ApplicationType.IOS)

public class GetGmailPINBasedOnSubstring extends IOSAction {

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;
    @TestData(reference = "Password")
    private com.testsigma.sdk.TestData Password;

    @TestData(reference = "nth")
    private com.testsigma.sdk.TestData nth;
    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData data;
    @TestData(reference = "var1")
    private com.testsigma.sdk.TestData var1;


    @RunTimeData
    private com.testsigma.sdk.RunTimeData runtTimeData;

    @Override
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        String host = "imap.gmail.com";
        String port = "993";
        String username = EmailID.getValue().toString();
        String password = Password.getValue().toString();  //


        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", port);
        props.setProperty("mail.imaps.auth", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        try {

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            Message latestMessage = messages[messages.length - 1];

            Object content = latestMessage.getContent();
            System.out.println(content);
            BodyPart bodyPart = null;
            String FullMessage = null;
            if (content instanceof String) {
                System.out.println(content);
                FullMessage = (String) content;

            } else if (content instanceof Multipart) {
                // content is already a Multipart object, so just cast it and process the body part
                Multipart multipart = (Multipart) content;
                bodyPart = multipart.getBodyPart(0);
                FullMessage = (String) bodyPart.getContent();
                // process the body part as needed
            } else {
                System.out.println("No content");
                logger.info("NO CONTENT");
            }

//usertestsigma@cloudbyz.com and cwvbarlmeuvgvoxo



//            Object content = latestMessage.getContent();
//            Multipart multipart = (Multipart) content;
//            BodyPart bodyPart = multipart.getBodyPart(0);

            //  String FullMessage = (String) bodyPart.getContent();
            logger.info(FullMessage);
            String verificationCodePrefix = data.getValue().toString();
            int verificationCodeIndex = FullMessage.indexOf(verificationCodePrefix);

            if (verificationCodeIndex != -1) {
                int verificationCodeStartIndex = verificationCodeIndex + verificationCodePrefix.length();
                String verificationCode = FullMessage.substring(verificationCodeStartIndex, verificationCodeStartIndex + Integer.valueOf(nth.getValue().toString()));
                System.out.println(verificationCode);
                logger.info(verificationCode);
                runtTimeData.setKey(var1.getValue().toString());
                runtTimeData.setValue(verificationCode);
                setSuccessMessage("The verification code is "+verificationCode);

            } else {
                result = com.testsigma.sdk.Result.FAILED;
                setErrorMessage("Could not get the PIN.Check if the trimming data is correct");

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


