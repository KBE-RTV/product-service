package com.kbertv.productService.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.DTO.CallCreateDTO;
import com.kbertv.productService.model.DTO.CallRequestDTO;
import com.kbertv.productService.model.DTO.ComponentDetailDTO;
import com.kbertv.productService.model.DTO.ProductDetailDTO;
import com.kbertv.productService.model.PlanetarySystem;
import com.kbertv.productService.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
public class CallConsumer {

    private ObjectMapper objectMapper;
    private ProductService productService;
    private ResponseProducer responseProducer;

    public CallConsumer(ProductService productService, ResponseProducer responseProducer) {
        this.productService = productService;
        this.responseProducer = responseProducer;
    }

    @RabbitListener(queues = {"${rabbitmq.queue.call.name}"})
    public void consumeCallFromGateWay(String jsonMessage) {
        objectMapper = new ObjectMapper();
        try {
            CallRequestDTO callRequestDTO = objectMapper.readValue(jsonMessage, CallRequestDTO.class);
            if (callRequestDTO.getType().equals("product") && callRequestDTO.getDetailID() !=null){
                responseProducer.sendResponseToGateWay(getProductJson(callRequestDTO));
            }
            if (callRequestDTO.getType().equals("component") && callRequestDTO.getDetailID() !=null){
                responseProducer.sendResponseToGateWay(getComponentJson(callRequestDTO));
            }
            if(callRequestDTO.getType().equals("component") && callRequestDTO.getDetailID() ==null){
                responseProducer.sendResponseToGateWay(getAllComponentsJson(callRequestDTO));
            }
            if(callRequestDTO.getType().equals("product") && callRequestDTO.getDetailID() ==null){
                responseProducer.sendResponseToGateWay(getAllProductsJson(callRequestDTO));
            }
            log.info("Received and processed message: " + jsonMessage);
        }catch (Exception e){
            try {
                CallCreateDTO callCreateDTO = objectMapper.readValue(jsonMessage, CallCreateDTO.class);
                //TODO RMQ Call to Price Service for price of product
                log.info("Received and processed message: " + jsonMessage);
            }catch (Exception f){
                log.error("Message could not be parsed: "+jsonMessage +System.lineSeparator() +f +System.lineSeparator()+e);
            }
        }
    }

    /*
    @RabbitListener(queues = {"${rabbitmq.queue.call.price.name}"})
    public void consumeCallFromPriceService(String jsonMessage){
        logger.info("Received and processed message: " + jsonMessage);
    }
     */

    public String getAllComponentsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        ArrayList<CelestialBody> celestialBodies = (ArrayList<CelestialBody>) productService.getAllComponents();
        ComponentDetailDTO response = new ComponentDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    public String getAllProductsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) productService.getAllProducts();
        ProductDetailDTO response = new ProductDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
        return objectMapper.writeValueAsString(response);
    }

    public String getComponentJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        Optional<CelestialBody> result = productService.getComponent(callRequestDTO.getDetailID());
        ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
        result.ifPresent(celestialBodies::add);
        ComponentDetailDTO response = new ComponentDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    public String getProductJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        Optional<PlanetarySystem> result = productService.getProduct(callRequestDTO.getDetailID());
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        result.ifPresent(planetarySystems::add);
        ProductDetailDTO response = new ProductDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
        return objectMapper.writeValueAsString(response);
    }
}
