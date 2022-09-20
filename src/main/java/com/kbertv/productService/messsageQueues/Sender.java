package com.kbertv.productService.messsageQueues;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Class wich sends messages to queues
 */
@Service
@Slf4j
public class Sender {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.response.key}")
    private String responseRoutingKey;
    @Value("${rabbitmq.queue.call.price.key}")
    private String priceServiceCallRoutingKey;
    private static RabbitTemplate rabbitTemplate;

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        Sender.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send a response message to the gateway.
     *
     * @param jsonResponse message in JSON format
     */
    public void sendResponseToGateWay(String jsonResponse){
        rabbitTemplate.convertAndSend(exchange, responseRoutingKey, jsonResponse);
        log.info("Send Message to "+exchange +" with " +responseRoutingKey +": " + jsonResponse);
    }

    /**
     * Send call to price service.
     *
     * @param jsonCall request dto
     */
    public void sendCallToPriceService(String jsonCall){
        rabbitTemplate.convertAndSend(exchange,priceServiceCallRoutingKey,jsonCall);
        log.info("Send Message to "+exchange +" with " +responseRoutingKey +": " + jsonCall);
    }
}
