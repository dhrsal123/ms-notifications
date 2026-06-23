package io.cinema.msnotifications.service.impl;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import io.cinema.domain.enumerated.CinemaExceptionTypes;
import io.cinema.domain.exceptions.CinemaException;
import io.cinema.msnotifications.config.EmailProperties;
import io.cinema.msnotifications.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final String APPLICATION_NAME = "cinema-notifications";
    private final EmailProperties emailProperties;

    @Override
    public void sendEmail(
            String to,
            String subject,
            String body
    ) throws MessagingException, IOException {

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(GmailScopes.GMAIL_SEND)
                .createDelegated(emailProperties.getSenderEmail());

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        Gmail service = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
        )
                .setApplicationName(APPLICATION_NAME)
                .build();

        var props = new Properties();
        var session = Session.getDefaultInstance(props, null);

        var msg = new MimeMessage(session);
        msg.setSubject(subject);
        msg.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        msg.setContent(body, "text/html");

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            msg.writeTo(buffer);
            byte[] messageBytes = buffer.toByteArray();
            var encodedMessage = Base64
                    .getEncoder()
                    .encodeToString(messageBytes);

            var message = new Message();
            message.setRaw(encodedMessage);

            message = service
                    .users()
                    .messages()
                    .send("me", message)
                    .execute();

            log.info("Message sent to {}", to);
            log.info("Message id: {}", message.getId());
        } catch (Exception e) {
            throw new CinemaException("Something went wrong: " + e.getMessage(), CinemaExceptionTypes.TECHNICAL_ERROR);
        }


    }
}
