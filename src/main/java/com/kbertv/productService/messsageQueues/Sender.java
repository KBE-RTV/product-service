package com.kbertv.productService.messsageQueues;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * Class wich sends messages to queues
 */
@Service
@Slf4j
public class Sender {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.queue.call.price.key}")
    private String priceServiceCallRoutingKey;
    private AsyncRabbitTemplate asyncRabbitTemplate;

    @Autowired
    public void setAsyncRabbitTemplateForCurrencyService(AsyncRabbitTemplate asyncRabbitTemplateForCurrencyService) {
        this.asyncRabbitTemplate = asyncRabbitTemplateForCurrencyService;
    }

    /**
     * Send call to price service.
     *
     * @param jsonCall request dto
     */
    public String sendAndReceiveCallToPriceService(String jsonCall) throws ExecutionException, InterruptedException {
        AsyncRabbitTemplate.RabbitConverterFuture<String> future = asyncRabbitTemplate.convertSendAndReceive(exchange, priceServiceCallRoutingKey, jsonCall);
        log.info("Send Message to " + exchange + " with " + priceServiceCallRoutingKey + ": " + jsonCall);
        return future.get();
    }
}
