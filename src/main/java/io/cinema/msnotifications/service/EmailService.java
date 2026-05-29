package io.cinema.msnotifications.service;

import jakarta.mail.MessagingException;

import java.io.IOException;

public interface EmailService {
    void sendEmail(
            String to,
            String subject,
            String body
    ) throws MessagingException, IOException;
}
