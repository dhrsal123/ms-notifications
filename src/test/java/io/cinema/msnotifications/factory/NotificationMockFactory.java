package io.cinema.msnotifications.factory;

import io.cinema.msnotifications.domain.dto.NotificationDTO;
import io.cinema.msnotifications.domain.enumerated.NotificationProvider;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.UUID;

@UtilityClass
public class NotificationMockFactory {
    public static NotificationDTO buildNotificationDTO(UUID notificationId, NotificationProvider provider) {
        return new NotificationDTO(
                notificationId,
                "CONFIRM_BOOKING",
                "Confirmation of your booking",
                provider,
                "testcustomer@gmail.com",
                Map.of()
        );
    }
}
