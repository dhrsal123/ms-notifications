package io.cinema.msnotifications.service;

import io.cinema.msnotifications.domain.dto.NotificationDTO;

public interface NotificationService {
    void sendNotification(NotificationDTO notification);
}
