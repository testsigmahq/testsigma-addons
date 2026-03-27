package utils;

import com.testsigma.sdk.Logger;

import javax.mail.*;
import javax.mail.search.*;
import java.util.Properties;

public class GmailUtils {

    private static final String HOST = "imap.gmail.com";
    private static final String PORT = "993";

    public static Store connectToGmail(String username, String password, Logger logger) throws MessagingException {
        logger.info("Connecting to Gmail IMAP as: " + username);

        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", HOST);
        props.setProperty("mail.imaps.port", PORT);
        props.setProperty("mail.imaps.auth", "true");
        props.setProperty("mail.imaps.starttls.enable", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Store store = session.getStore("imaps");
        store.connect(HOST, username, password);
        logger.info("Connected to Gmail IMAP successfully");
        return store;
    }

    public static String extractContent(Object content) throws Exception {
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            BodyPart bodyPart = multipart.getBodyPart(0);
            Object bodyContent = bodyPart.getContent();
            if (bodyContent instanceof String) {
                return (String) bodyContent;
            } else if (bodyContent instanceof Multipart) {
                Multipart bodyMultipart = (Multipart) bodyContent;
                return (String) bodyMultipart.getBodyPart(0).getContent();
            }
        }
        return null;
    }

    public static SearchTerm buildSearchTerm(String criteria, String value) {
        switch (criteria) {
            case "From":
                return new FromStringTerm(value);
            case "To":
                return new RecipientStringTerm(Message.RecipientType.TO, value);
            case "Subject":
                return new SubjectTerm(value);
            default:
                throw new IllegalArgumentException("Unsupported filter criteria: " + criteria +
                        ". Allowed values are: From, To, Subject");
        }
    }

    public static void closeQuietly(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) folder.close(false);
        } catch (Exception ignored) {}
        try {
            if (store != null && store.isConnected()) store.close();
        } catch (Exception ignored) {}
    }

    public static String sanitizeUsername(String raw) {
        return raw.trim();
    }

    public static String sanitizePassword(String raw) {
        return raw.trim().replaceAll("\\s+", "");
    }
}
