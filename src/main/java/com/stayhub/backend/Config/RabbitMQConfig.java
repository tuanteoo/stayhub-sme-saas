package com.stayhub.backend.Config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BOOKING_DLX = "booking.dlx";

    public static final String PENDING_QUEUE = "booking.pending.queue";
    public static final String TIMEOUT_QUEUE = "booking.timeout.queue";

    public static final String ROUTING_KEY_CREATED = "booking.created";
    public static final String ROUTING_KEY_TIMEOUT = "booking.timeout";

    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(BOOKING_DLX);
    }

    @Bean
    public Queue pendingQueue() {
        return QueueBuilder.durable(PENDING_QUEUE)
                .withArgument("x-message-ttl", 900000)
                .withArgument("x-dead-letter-exchange", BOOKING_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_TIMEOUT)
                .build();
    }

    @Bean
    public Queue timeoutQueue() {
        return QueueBuilder.durable(TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding pendingBinding() {
        return BindingBuilder.bind(pendingQueue()).to(bookingExchange()).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding timeoutBinding() {
        return BindingBuilder.bind(timeoutQueue()).to(deadLetterExchange()).with(ROUTING_KEY_TIMEOUT);
    }
}
