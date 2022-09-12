package io.project.smartcontactmanager.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {


    public boolean sendEmail(String subject, String message, String to){

        boolean f = false;
        String from = "astrospamcoder@gmail.com";

        String host = "smtp.mailtrap.io";

//        System Prop
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES " + properties);

//        host set
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "2525");
        properties.put("mail.smtp.ssl.enable", "false");
        properties.put("mail.smtp.auth", "true");

//        Setting session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("8b0d929fb1a016", "a773aae77507ef");
            }
        });

        session.setDebug(true);

        MimeMessage m = new MimeMessage(session);
        try{

            m.setFrom(from);

            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            m.setSubject(subject);

//            m.setText(message);
            m.setContent(message, "text/html");

            Transport.send(m);
            System.out.println("Email Sent...");
            f = true;


        }
        catch(Exception e){
            e.printStackTrace();
        }
        return f;
    }
}
