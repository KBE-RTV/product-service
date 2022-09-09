package com.kbertv.productService.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResponseProducer {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.response.key}")
    private String responseRoutingKey;
    @Value("${rabbitmq.queue.call.price.key}")
    private String priceServiceCallRoutingKey;
    private static RabbitTemplate rabbitTemplate;
    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        ResponseProducer.rabbitTemplate = rabbitTemplate;
    }

    public void sendResponseToGateWay(String jsonResponse){
        rabbitTemplate.convertAndSend(exchange, responseRoutingKey, jsonResponse);
        log.info("Send Message to "+exchange +" with " +responseRoutingKey +": " + jsonResponse);
    }

    public void sendCallToPriceService(String jsonCall){
        rabbitTemplate.convertAndSend(exchange,priceServiceCallRoutingKey,jsonCall);
        log.info("Send Message to "+exchange +" with " +responseRoutingKey +": " + jsonCall);
    }
}
