package com.escola.biblioteca.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_EVENTS = "biblioteca.events";
    public static final String EXCHANGE_DLX = "biblioteca.events.dlx";

    public static final String ROUTING_KEY_CIRCULATION_LOAN = "circulation.loan";
    public static final String ROUTING_KEY_CIRCULATION_HOLD = "circulation.hold";
    public static final String ROUTING_KEY_CIRCULATION_FINE = "circulation.fine";
    public static final String ROUTING_KEY_CATALOG_ITEM = "catalog.item";

    public static final String QUEUE_CIRCULATION_LOAN = "circulation.loan.queue";
    public static final String QUEUE_CIRCULATION_HOLD = "circulation.hold.queue";
    public static final String QUEUE_CIRCULATION_FINE = "circulation.fine.queue";
    public static final String QUEUE_CATALOG_ITEM = "catalog.item.queue";
    public static final String QUEUE_DLQ = "biblioteca.events.dlq";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_EVENTS);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("dlq");
    }

    @Bean
    public Queue loanQueue() {
        return QueueBuilder.durable(QUEUE_CIRCULATION_LOAN)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Binding loanBinding(Queue loanQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(loanQueue).to(eventsExchange).with(ROUTING_KEY_CIRCULATION_LOAN);
    }

    @Bean
    public Queue holdQueue() {
        return QueueBuilder.durable(QUEUE_CIRCULATION_HOLD)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Binding holdBinding(Queue holdQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(holdQueue).to(eventsExchange).with(ROUTING_KEY_CIRCULATION_HOLD);
    }

    @Bean
    public Queue fineQueue() {
        return QueueBuilder.durable(QUEUE_CIRCULATION_FINE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Binding fineBinding(Queue fineQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(fineQueue).to(eventsExchange).with(ROUTING_KEY_CIRCULATION_FINE);
    }

    @Bean
    public Queue catalogItemQueue() {
        return QueueBuilder.durable(QUEUE_CATALOG_ITEM)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Binding catalogItemBinding(Queue catalogItemQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(catalogItemQueue).to(eventsExchange).with(ROUTING_KEY_CATALOG_ITEM);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
