package io.cinema.msnotifications.service.impl;

import io.cinema.msnotifications.domain.dto.NotificationDTO;
import io.cinema.msnotifications.domain.enumerated.NotificationProvider;
import io.cinema.msnotifications.service.NotificationService;
import io.cinema.msnotifications.strategy.NotificationProviderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    private final Map<NotificationProvider, NotificationProviderStrategy> strategies;

    public NotificationServiceImpl(List<NotificationProviderStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(
                        Collectors
                                .toMap(
                                        NotificationProviderStrategy::getNotificationProvider,
                                        strategy -> strategy,
                                        (existing, replacement) -> existing
                                )
                );
    }


    @Override
    @RabbitListener(queues = "${notifications-rmq.queueName}")
    public void sendNotification(NotificationDTO notification) {
        log.info("Received notification event: {}", notification);

        var strategy = strategies.get(notification.provider());

        if (Objects.isNull(strategy)) {
            log.error("No strategy found for provider: {}", notification.provider());

            throw new AmqpRejectAndDontRequeueException("Invalid payload");
        }

        strategy.send(notification);
    }
}
