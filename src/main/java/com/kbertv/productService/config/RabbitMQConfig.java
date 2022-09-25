package com.kbertv.productService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
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

    public static TopicExchange exchange;
    @Value("${rabbitmq.exchange.name}")
    private String topicExchangeName;
    @Value("${rabbitmq.queue.call.name}")
    private String callQueue;
    @Value("${rabbitmq.queue.call.key}")
    private String callRoutingKey;
    @Value("${rabbitmq.queue.response.name}")
    private String responseQueue;
    @Value("${rabbitmq.queue.response.key}")
    private String responseRoutingKey;
    @Value("${rabbitmq.queue.response.price.name}")
    private String priceServiceResponseQueue;

    @Autowired
    public void setExchange(@Lazy TopicExchange exchange) {
        RabbitMQConfig.exchange = exchange;
    }

    @Bean
    Queue queue() {
        return new Queue(callQueue, false);
    }

    @Bean
    Queue responseQueue() {
        return new Queue(responseQueue, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    /**
     * Binds the call queue to the exchange with the call RoutingKey
     *
     * @return binding
     */
    @Bean
    Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange).with(callRoutingKey);
    }

    /**
     * Binds the response queue to the exchange with the response RoutingKey
     *
     * @return binding
     */
    @Bean
    Binding responseBinding() {
        return BindingBuilder.bind(responseQueue()).to(exchange).with(responseRoutingKey);
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


    /**
     * Creates an async RabbitMQ Template with the RabbitMQ Template and links the productServiceResponse Queue for responses
     *
     * @param rabbitTemplate the rabbit template
     * @return the async rabbit template
     */
    @Bean(name = "asyncRabbitTemplateForProductService")
    public AsyncRabbitTemplate asyncRabbitTemplateForProductService(RabbitTemplate rabbitTemplate) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(rabbitTemplate.getConnectionFactory());
        container.setQueueNames(priceServiceResponseQueue);
        return new AsyncRabbitTemplate(rabbitTemplate, container);
    }
}
