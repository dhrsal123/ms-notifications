package io.cinema.msnotifications.domain.dto;

import io.cinema.msnotifications.domain.enumerated.NotificationProvider;

import java.util.Map;

public record NotificationDTO(
        String notificationId,
        String notificationType,

        NotificationProvider provider,

        String recipient,

        Map<String, Object> templateModel

) {
}
