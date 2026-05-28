package io.cinema.msnotifications.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {
    private final WebClientProperties webClientProperties;

    @Bean
    public WebClient webClient() {
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("defaultCircuitBreaker");

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> next.exchange(request)
                        .retryWhen(
                                Retry.backoff(
                                                webClientProperties.getMaxRetries(),
                                                webClientProperties.getTimeBetweenRetries()
                                        )
                                        .doAfterRetry(retry -> log.info("Retrying request..."))
                        )
                )
                .filter(
                        (request, next) ->
                                circuitBreaker.executeSupplier(() -> next.exchange(request))
                )
                .build();

    }
}
