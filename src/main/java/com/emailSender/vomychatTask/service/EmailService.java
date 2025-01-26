package com.emailSender.vomychatTask.service;

import com.emailSender.vomychatTask.exception.EmailException;
import com.emailSender.vomychatTask.model.EmailRequest;
import com.emailSender.vomychatTask.repository.EmailRepository;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class EmailService {
    @Autowired
    private EmailRepository emailRepository;

    @Value("${mail.username}")
    private String mailUsername;

    @Value("${mail.password}")
    private String mailPassword;

    /**
     * Sends an email with the provided details.
     *
     * @param to      the recipient's email address
     * @param subject the subject of the email
     * @param content the content of the email
     */
    @Async
    public void sendEmail(String to, String subject, String content) {
        log.info("Preparing to send email to: {}", to);

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            // Send the email
            Transport.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (MessagingException e) {
            // Log and throw custom exception on error
            log.error("Error sending email: {}", e.getMessage());
            throw new EmailException("Error sending email: " + e.getMessage(), e);
        }
    }

    /**
     * Processes an email request from the RabbitMQ queue and sends the email.
     *
     * @param emailRequest the email request containing the details to send the email
     */
    @Async
    @RabbitListener(queues = "emailQueue")
    public void processEmailMessage(EmailRequest emailRequest) {
        log.info("Received email request to send email with subject: {}", emailRequest.getSubject());

        // Call the sendEmail method to send the email
        sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getContent());
        log.info("Processed email with subject: {}", emailRequest.getSubject());
    }

    /**
     * Fetches emails from a specific folder on the Gmail account.
     *
     * @param folderName the folder to fetch emails from (e.g., "INBOX")
     */
    @Async
    public void fetchEmailsFromFolder(String folderName) {
        log.info("Fetching emails from folder: {}", folderName);

        try {
            // Setup properties for IMAP connection
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", "imap.gmail.com");
            props.put("mail.imaps.port", "993");
            props.put("mail.imaps.ssl.enable", "true");

            // Initialize session and connect to the mailbox
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(mailUsername, mailPassword);

            // Access the folder and fetch emails
            Folder folder = store.getFolder(folderName);
            if (!folder.exists()) {
                log.error("Folder {} does not exist", folderName);
                throw new EmailException("Folder " + folderName + " does not exist.");
            }

            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            for (Message message : messages) {
                log.info("Subject of email: {}", message.getSubject());
            }

            // Close the folder and store connections
            folder.close(false);
            store.close();
            log.info("Successfully fetched emails from folder: {}", folderName);
        } catch (MessagingException e) {
            // Log and throw custom exception on error
            log.error("Error fetching emails from folder {}: {}", folderName, e.getMessage());
            throw new EmailException("Error fetching emails from folder: " + e.getMessage(), e);
        }
    }
}
