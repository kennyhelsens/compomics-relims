package com.compomics.relims.manager.usernotificationmanager;

import java.io.File;
import javax.mail.*;
import javax.mail.internet.*;

import java.security.Security;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

public class MailEngine {

    private static final String SENDERADRESS = "CompomicsReSpin@gmail.com";
    private static final String SENDERPASSWORD = "respin,13*";

    public static void main(String[] args) {
        File testFile = new File("C:\\Users\\Kenneth\\Desktop\\searchGUI_input.txt");
        try {
            String[] recipients = new String[]{};
            sendMail(recipients, "This is a testing message", "test", testFile);
        } catch (Exception ex) {
            Logger.getLogger(MailEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Session setup() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.debug", "false");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDERADRESS,
                        SENDERPASSWORD);
            }
        });

        session.setDebug(true);
        return session;
    }

    public static void sendMail(String[] recipients, String subject, String problem, File attachment) throws Exception {

        Session session = setup();
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        Transport transport = session.getTransport();
        InternetAddress addressFrom = new InternetAddress(SENDERADRESS);

        MimeMessage message = new MimeMessage(session);
        message.setSender(addressFrom);
        message.setSubject(subject);
        //TODO REPLACE THIS WITH ADDITION TO RECIPIENTS IN PRE-METHOD
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                SENDERADRESS));

        if (recipients.length != 0) {
            for (String aRecipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                        aRecipient));
            }
        }

        MimeBodyPart messageBodyPart =
                new MimeBodyPart();
        messageBodyPart.setText(problem);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        if (attachment != null) {
            if (attachment.exists()) {
                messageBodyPart = new MimeBodyPart();
                DataSource source =
                        new FileDataSource(attachment);
                messageBodyPart.setDataHandler(
                        new DataHandler(source));
                messageBodyPart.setFileName(attachment.getName());
                multipart.addBodyPart(messageBodyPart);
            }
        }
        // Put parts in message
        message.setContent(multipart);

        transport.connect();
        transport.send(message);
        transport.close();
    }
}
