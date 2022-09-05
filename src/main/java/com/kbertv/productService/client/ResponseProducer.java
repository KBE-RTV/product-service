package com.kbertv.productService.client;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
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
        System.out.println("SENT TO GATEWAY: " + jsonResponse + "\n");
    }

    public void sendCallToPriceService(String jsonCall){
        rabbitTemplate.convertAndSend(exchange,priceServiceCallRoutingKey,jsonCall);
        System.out.println("SENT TO PRICE SERVICE: " + jsonCall + "\n");
    }
}
