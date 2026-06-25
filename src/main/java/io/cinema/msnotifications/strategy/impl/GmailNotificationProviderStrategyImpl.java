package io.cinema.msnotifications.strategy.impl;

import io.cinema.domain.enumerated.CinemaExceptionTypes;
import io.cinema.domain.exceptions.CinemaException;
import io.cinema.msnotifications.domain.dto.NotificationDTO;
import io.cinema.msnotifications.domain.enumerated.NotificationProvider;
import io.cinema.msnotifications.service.EmailService;
import io.cinema.msnotifications.strategy.NotificationProviderStrategy;
import io.cinema.msnotifications.util.TemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailNotificationProviderStrategyImpl implements NotificationProviderStrategy {
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @Override
    public NotificationProvider getNotificationProvider() {
        return NotificationProvider.GOOGLE;
    }

    @Override
    public void send(NotificationDTO notification) {
        log.info("Sending notification via GOOGLE to: {}", notification.recipient());

        String template = notification.notificationType() + ".mustache";
        String message = templateEngine.buildTemplate(template, notification.templateModel());

        try {
            emailService.sendEmail(notification.recipient(), notification.subject(), message);
        } catch (Exception e) {
            log.error("Something went wrong while sending the email: {}", e.getMessage());

            throw new CinemaException(
                    "Something went wrong while sending the email: " + e.getMessage(),
                    CinemaExceptionTypes.TECHNICAL_ERROR
            );
        }
    }
}
