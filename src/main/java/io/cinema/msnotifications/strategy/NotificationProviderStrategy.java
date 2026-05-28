package io.cinema.msnotifications.strategy;

import io.cinema.msnotifications.domain.dto.NotificationDTO;
import io.cinema.msnotifications.domain.enumerated.NotificationProvider;

public interface NotificationProviderStrategy {
    NotificationProvider getNotificationProvider();

    void send(NotificationDTO notification);
}
