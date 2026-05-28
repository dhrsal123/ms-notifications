package io.cinema.msnotifications.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "notifications-rmq")
public class QueueProperties {
    private String queueName;

    private String dlqName;

    private String exchangeName;

    private String dlqExchangeName;

    private String routingKey;

    private String dlqRoutingKey;

    private Integer queueTtl;
}
