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

    private static final String email = "CompomicsReSpin@gmail.com";
    private static final String password = "respin,13*";
    
    public static void main(String[]args){
        File testFile = new File("C:\\Users\\Kenneth\\Desktop\\searchGUI_input.txt");
        try {
            sendMail("This is a testing message","test",testFile);
        } catch (Exception ex) {
            Logger.getLogger(MailEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void sendMail(String subject, String problem, File attachment) throws Exception {

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

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
                return new PasswordAuthentication(email,
                        password);
            }
        });

        session.setDebug(true);


        Transport transport = session.getTransport();
        InternetAddress addressFrom = new InternetAddress("CompomicsReSpin@gmail.com");

        MimeMessage message = new MimeMessage(session);
        message.setSender(addressFrom);
        message.setSubject(subject);
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                "CompomicsReSpin@gmail.com"));

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
