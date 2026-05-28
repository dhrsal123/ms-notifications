package io.cinema.msnotifications.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "cinema-client-configs")
public class WebClientProperties {
    private int maxRetries;
    private Duration timeBetweenRetries;
}
