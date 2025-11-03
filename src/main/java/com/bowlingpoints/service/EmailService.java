package com.bowlingpoints.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("maq.htas.gr1pm@gmail.com"); // debe ser el configurado en application.properties
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // 👈 true = interpreta como HTML

        mailSender.send(message);
    }
}
