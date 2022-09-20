package com.kbertv.productService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration for RabbitMQ
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String topicExchangeName;
    @Value("${rabbitmq.queue.call.name}")
    private String callQueue;
    @Value("${rabbitmq.routing.call.key}")
    private String callRoutingKey;
    public static TopicExchange exchange;

    @Autowired
    public void setExchange(@Lazy TopicExchange exchange) {
        RabbitMQConfig.exchange = exchange;
    }

    @Bean
    Queue queue() {
        return new Queue(callQueue, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    /**
     * Binds the queue to the exchange with a routing key
     *
     * @param queue   queue to bind
     * @param exchange exchange to bind to
     * @return binding
     */
    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(callRoutingKey);
    }

    /**
     * Gets an Jackson2JsonMessageConverter
     *
     * @return Jackson2JsonMessageConverter
     */
    @Bean
    public Jackson2JsonMessageConverter producerMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates a RabbitMQ Template and connects the message converter to it
     *
     * @param connectionFactory connection factory
     * @return rabbit template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerMessageConverter());
        return rabbitTemplate;
    }
}
