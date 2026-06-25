package io.cinema.msnotifications.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import io.cinema.domain.exceptions.CinemaException;
import io.cinema.msnotifications.config.EmailProperties;
import io.cinema.msnotifications.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Gmail gmail;

    @Mock
    private EmailProperties emailProperties;

    @Captor
    private ArgumentCaptor<Message> captor;

    @InjectMocks
    private EmailServiceImpl emailService;

    @SneakyThrows
    @Test
    void shouldSendEmail() {
        // arrange
        var to = "test@gmail.com";
        var subject = "Test Subject";
        var content = "Test Content";
        var sender = "support@cinema.io";

        when(emailProperties.getSenderEmail()).thenReturn(sender);

        var dummyResponse = new Message().setId("msg-12345");
        when(gmail.users().messages().send(eq("me"), captor.capture()).execute())
                .thenReturn(dummyResponse);

        // act
        emailService.sendEmail(to, subject, content);

        // assert
        var capturedMessage = captor.getValue();
        assertThat(capturedMessage.getRaw()).isNotNull();

        byte[] emailBytes = Base64.getUrlDecoder().decode(capturedMessage.getRaw());

        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

        assertThat(mimeMessage.getSubject()).isEqualTo(subject);
        assertThat(mimeMessage.getFrom()[0]).hasToString(sender);
        assertThat(mimeMessage.getRecipients(jakarta.mail.Message.RecipientType.TO)[0]).hasToString(to);

        Object messageContent = mimeMessage.getContent();
        String actualBody;

        if (messageContent instanceof java.io.InputStream inputStream) {
            actualBody = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } else {
            actualBody = messageContent.toString();
        }

        assertThat(actualBody).isEqualTo(content);
    }

    @SneakyThrows
    @Test
    void shouldThrowCinemaExceptionWhenEmailFails() {
        // arrange
        var to = "test@gmail.com";
        var subject = "Test Subject";
        var content = "Test Content";
        var sender = "support@cinema.io";

        when(emailProperties.getSenderEmail()).thenReturn(sender);

        when(gmail.users().messages().send(eq("me"), any(Message.class)).execute())
                .thenThrow(new IOException("Test exception."));

        // act & assert
        assertThatThrownBy(() -> emailService.sendEmail(to, subject, content))
                .isInstanceOf(CinemaException.class)
                .hasMessage("Something went wrong: Test exception.");

    }

}