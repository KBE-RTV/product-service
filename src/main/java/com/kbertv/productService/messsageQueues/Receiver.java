package com.kbertv.productService.messsageQueues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.dto.CallCreateDTO;
import com.kbertv.productService.model.dto.CallRequestDTO;
import com.kbertv.productService.model.dto.ComponentDetailDTO;
import com.kbertv.productService.model.dto.ProductDetailDTO;
import com.kbertv.productService.model.PlanetarySystem;
import com.kbertv.productService.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Class wich receives messages form the queue
 */
@Service
@Slf4j
public class Receiver {

    private ObjectMapper objectMapper;
    private ProductService productService;
    private Sender sender;

    /**
     * Instantiates a new Receiver.
     *
     * @param productService the product service
     * @param sender         the sender
     */
    public Receiver(ProductService productService, Sender sender) {
        this.productService = productService;
        this.sender = sender;
    }

    /**
     * Process messages from gateway and sends responses.
     *
     * @param jsonMessage Messages in JSON format
     */
    @RabbitListener(queues = {"${rabbitmq.queue.call.name}"})
    public void consumeCallFromGateWay(String jsonMessage) {
        objectMapper = new ObjectMapper();
        try {
            CallRequestDTO callRequestDTO = objectMapper.readValue(jsonMessage, CallRequestDTO.class);
            if (callRequestDTO.getType().equals("product") && callRequestDTO.getDetailID() !=null){
                sender.sendResponseToGateWay(getProductJson(callRequestDTO));
            }
            if (callRequestDTO.getType().equals("component") && callRequestDTO.getDetailID() !=null){
                sender.sendResponseToGateWay(getComponentJson(callRequestDTO));
            }
            if(callRequestDTO.getType().equals("component") && callRequestDTO.getDetailID() ==null){
                sender.sendResponseToGateWay(getAllComponentsJson(callRequestDTO));
            }
            if(callRequestDTO.getType().equals("product") && callRequestDTO.getDetailID() ==null){
                sender.sendResponseToGateWay(getAllProductsJson(callRequestDTO));
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

    /**
     * Helper Method to create and fill the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ComponentDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    public String getAllComponentsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        ArrayList<CelestialBody> celestialBodies = (ArrayList<CelestialBody>) productService.getAllComponents();
        ComponentDetailDTO response = new ComponentDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and fill the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ProductDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    public String getAllProductsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) productService.getAllProducts();
        ProductDetailDTO response = new ProductDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and fill the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ComponentDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    public String getComponentJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        Optional<CelestialBody> result = productService.getComponent(callRequestDTO.getDetailID());
        ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
        result.ifPresent(celestialBodies::add);
        ComponentDetailDTO response = new ComponentDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and fill the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ProductDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    public String getProductJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        Optional<PlanetarySystem> result = productService.getProduct(callRequestDTO.getDetailID());
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        result.ifPresent(planetarySystems::add);
        ProductDetailDTO response = new ProductDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
        return objectMapper.writeValueAsString(response);
    }
}
