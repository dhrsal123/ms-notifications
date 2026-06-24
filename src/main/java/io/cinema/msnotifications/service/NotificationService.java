package io.cinema.msnotifications.service;

import io.cinema.msnotifications.domain.dto.NotificationDTO;
import jakarta.validation.Valid;

public interface NotificationService {
    void sendNotification(@Valid NotificationDTO notification);
}
