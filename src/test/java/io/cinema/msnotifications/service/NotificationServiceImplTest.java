package io.cinema.msnotifications.service;

import io.cinema.msnotifications.domain.enumerated.NotificationProvider;
import io.cinema.msnotifications.factory.NotificationMockFactory;
import io.cinema.msnotifications.service.impl.NotificationServiceImpl;
import io.cinema.msnotifications.strategy.NotificationProviderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock
    private NotificationProviderStrategy strategy;

    private NotificationServiceImpl notificationService;


    @BeforeEach
    void setUp() {
        when(strategy.getNotificationProvider()).thenReturn(NotificationProvider.GOOGLE);
        this.notificationService = new NotificationServiceImpl(List.of(strategy));
    }

    @Test
    void shouldRedirectToImplementation() {
        // arrange
        var notificationId = UUID.randomUUID();
        var notification = NotificationMockFactory.buildNotificationDTO(notificationId, NotificationProvider.GOOGLE);

        // act
        notificationService.sendNotification(notification);

        // assert
        verify(strategy).send(notification);
    }

    @Test
    void shouldThrowWhenStrategyIsNotFound() {
        // arrange
        var notificationId = UUID.randomUUID();
        var notification = NotificationMockFactory.buildNotificationDTO(notificationId, null);

        // act & assert
        assertThatThrownBy(() -> notificationService.sendNotification(notification))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessage("Invalid payload");

        verify(strategy, never()).send(any());
    }

    @Test
    void shouldKeepFirstStrategyWhenDuplicateProvidersExist() {
        // arrange
        var firstStrategy = mock(NotificationProviderStrategy.class);
        var secondStrategy = mock(NotificationProviderStrategy.class);

        when(firstStrategy.getNotificationProvider()).thenReturn(NotificationProvider.GOOGLE);
        when(secondStrategy.getNotificationProvider()).thenReturn(NotificationProvider.GOOGLE);

        var serviceWithDuplicates = new NotificationServiceImpl(List.of(firstStrategy, secondStrategy));
        var notification = NotificationMockFactory.buildNotificationDTO(UUID.randomUUID(), NotificationProvider.GOOGLE);

        // act
        serviceWithDuplicates.sendNotification(notification);

        // assert
        verify(firstStrategy).send(notification);
        verify(secondStrategy, never()).send(notification);
    }
}