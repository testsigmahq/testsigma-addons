package com.testsigma.addons.web;

import com.testsigma.sdk.ApplicationType;
import com.testsigma.sdk.Result;
import com.testsigma.sdk.WebAction;
import com.testsigma.sdk.annotation.Action;
import com.testsigma.sdk.annotation.TestData;
import lombok.Data;

import javax.mail.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Action(actionText = "Get value from gmail using EmailID and Password and trim the value using regex testdata to store the value in a runtime variable var1",
        description = "Verifying the value displayed in the csv file",
        applicationType = ApplicationType.WEB)

public class GmailGetOTP extends WebAction {

    @TestData(reference = "EmailID")
    private com.testsigma.sdk.TestData EmailID;
    @TestData(reference = "Password")
    private com.testsigma.sdk.TestData Password;
    @TestData(reference = "testdata")
    private com.testsigma.sdk.TestData regex;
    @TestData(reference = "var1")
    private com.testsigma.sdk.TestData var1;

    @Override
    public com.testsigma.sdk.Result execute() {

        com.testsigma.sdk.Result result = com.testsigma.sdk.Result.SUCCESS;
        logger.info("Initiating execution");

        String host = "imap.gmail.com";
        String port = "993";
        String username = EmailID.getValue().toString();  //kcsdqa@gmail.com / HouseAcrossTheRiverNoida
        String password = Password.getValue().toString();  //


        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", port);
        props.setProperty("mail.imaps.auth", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Store store = null;
        Folder inbox = null;

        try {

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            store = session.getStore("imaps");
            store.connect(host, username, password);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            Message latestMessage = messages[messages.length - 1];

            Object content = latestMessage.getContent();
            Multipart multipart = (Multipart) content;
            BodyPart bodyPart = multipart.getBodyPart(0);

            String FullMessage = (String) bodyPart.getContent();
            logger.info(FullMessage);
            //System.out.println(FullMessage);
            Pattern pattern = Pattern.compile(regex.getValue().toString()); //(?s)PIN\s*(\d+)
            Matcher matcher = pattern.matcher(FullMessage);

            if (matcher.find()) {
                String otp = matcher.group(1);
                setSuccessMessage("PIN is "+otp);
                System.out.println("OTP is "+otp);
            } else {
                result = Result.FAILED;
                setErrorMessage("NOT an OTP. Check if Regex is correct");

            }

        } catch (Exception e) {

            result = com.testsigma.sdk.Result.FAILED;
            setErrorMessage("Could not get the PIN . The error is "+e.getMessage());
            logger.warn(e.getMessage());
        } finally {
            closeQuietly(inbox);
            closeQuietly(store);
        }
        return result;
    }

    private void closeQuietly(Folder folder) {
        if (folder != null && folder.isOpen()) {
            try {
                folder.close(false);
            } catch (Exception ignored) {
                // no-op
            }
        }
    }

    private void closeQuietly(Store store) {
        if (store != null) {
            try {
                store.close();
            } catch (Exception ignored) {
                // no-op
            }
        }
    }
}

