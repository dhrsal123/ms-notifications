package io.cinema.msnotifications.service.impl;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import io.cinema.domain.enumerated.CinemaExceptionTypes;
import io.cinema.domain.exceptions.CinemaException;
import io.cinema.msnotifications.config.EmailProperties;
import io.cinema.msnotifications.service.EmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final Gmail gmail;
    private final EmailProperties emailProperties;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            var props = new Properties();
            var session = Session.getDefaultInstance(props, null);

            var msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(emailProperties.getSenderEmail()));
            msg.setSubject(subject, "UTF-8");
            msg.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            msg.setContent(body, "text/html; charset=utf-8");

            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                msg.writeTo(buffer);
                String encodedMessage = Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(buffer.toByteArray());

                var message = new Message();
                message.setRaw(encodedMessage);

                message = gmail.users().messages().send("me", message).execute();

                log.info("Message sent to {}", to);
                log.info("Message id: {}", message.getId());
            }
        } catch (Exception e) {
            throw new CinemaException("Something went wrong: " + e.getMessage(), CinemaExceptionTypes.TECHNICAL_ERROR);
        }
    }
}
