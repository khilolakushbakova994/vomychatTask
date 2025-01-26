package com.emailSender.vomychatTask.controller;

import com.emailSender.vomychatTask.model.EmailRequest;
import com.emailSender.vomychatTask.service.EmailService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends an email request by adding it to the RabbitMQ queue for processing.
     *
     * @param emailRequest the email request containing the details of the email to be sent
     * @return a confirmation message indicating that the email was added to the queue
     */
    @PostMapping("/send")
    public String sendEmail(@RequestBody EmailRequest emailRequest) {
        rabbitTemplate.convertAndSend("emailQueue", emailRequest);
        return "Email has been added to the queue for sending to " + emailRequest.getTo();
    }

    /**
     * Fetches emails from a specified folder.
     *
     * @param folderName the name of the folder from which emails should be fetched
     * @return a message indicating that emails from the folder are being read
     */
    @GetMapping("/fetch")
    public String fetchEmails(@RequestParam String folderName) {
        emailService.fetchEmailsFromFolder(folderName);
        return "Reading emails from folder: " + folderName;
    }
}
