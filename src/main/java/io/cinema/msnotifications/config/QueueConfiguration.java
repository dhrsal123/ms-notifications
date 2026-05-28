package io.cinema.msnotifications.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class QueueConfiguration {
    private final QueueProperties queueProperties;

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(queueProperties.getExchangeName());
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(queueProperties.getQueueName())
                .ttl(queueProperties.getQueueTtl())
                .deadLetterExchange(queueProperties.getDlqExchangeName())
                .deadLetterRoutingKey(queueProperties.getDlqRoutingKey())
                .build();
    }

    @Bean
    public Binding notificationsBinding(
            TopicExchange topicExchange,
            Queue notificationsQueue
    ) {
        return BindingBuilder
                .bind(notificationsQueue)
                .to(topicExchange)
                .with(queueProperties.getRoutingKey());
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(queueProperties.getDlqExchangeName());
    }

    @Bean
    public Queue deadLettersQueue() {
        return new Queue(queueProperties.getDlqName());
    }

    @Bean
    public Binding deadLettersBinding(DirectExchange directExchange, Queue deadLettersQueue) {
        return BindingBuilder
                .bind(deadLettersQueue)
                .to(directExchange)
                .with(queueProperties.getDlqRoutingKey());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

}
