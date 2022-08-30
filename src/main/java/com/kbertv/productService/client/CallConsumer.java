package com.kbertv.productService.client;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class CallConsumer {
    @RabbitListener(queues = {"${rabbitmq.queue.call.name}"})
    public void consumeCall(String jsonMessage){
        System.out.println("RECEIVED: " + jsonMessage);
    }
}
