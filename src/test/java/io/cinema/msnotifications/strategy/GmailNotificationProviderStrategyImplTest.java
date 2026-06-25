package io.cinema.msnotifications.strategy;

import io.cinema.domain.exceptions.CinemaException;
import io.cinema.msnotifications.factory.NotificationMockFactory;
import io.cinema.msnotifications.service.EmailService;
import io.cinema.msnotifications.strategy.impl.GmailNotificationProviderStrategyImpl;
import io.cinema.msnotifications.util.TemplateEngine;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.UUID;

import static io.cinema.msnotifications.domain.enumerated.NotificationProvider.GOOGLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GmailNotificationProviderStrategyImplTest {
    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private GmailNotificationProviderStrategyImpl strategy;

    @Test
    void shouldBeGmailNotificationProvider() {
        // act & assert
        assertEquals(GOOGLE, strategy.getNotificationProvider());
    }

    @SneakyThrows
    @Test
    void shouldSendGmailNotification() {
        // arrange
        var notificationId = UUID.randomUUID();
        var notification = NotificationMockFactory.buildNotificationDTO(notificationId, GOOGLE);

        String template = "<h1>Test template!</h1>";
        when(templateEngine.buildTemplate(anyString(), anyMap())).thenReturn(template);

        // act
        strategy.send(notification);

        // assert
        verify(templateEngine).buildTemplate(anyString(), anyMap());
        verify(emailService).sendEmail(notification.recipient(), notification.subject(), template);
    }

    @SneakyThrows
    @Test
    void shouldThrowCinemaExceptionWhenSendGmailFails() {
        // arrange
        var notificationId = UUID.randomUUID();
        var notification = NotificationMockFactory.buildNotificationDTO(notificationId, GOOGLE);

        String template = "<h1>Test template!</h1>";
        when(templateEngine.buildTemplate(anyString(), anyMap())).thenReturn(template);

        doThrow(new IOException("Test error"))
                .when(emailService)
                .sendEmail(notification.recipient(), notification.subject(), template);

        // act & assert
        assertThatThrownBy(() -> strategy.send(notification))
                .isInstanceOf(CinemaException.class)
                .hasMessage("Something went wrong while sending the email: Test error");

        verify(templateEngine).buildTemplate(anyString(), anyMap());
        verify(emailService).sendEmail(notification.recipient(), notification.subject(), template);
    }


}