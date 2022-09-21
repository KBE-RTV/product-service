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
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

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
                ProductDetailDTO productDetailDTO = getProduct(callRequestDTO);
                if (isPriceCalculated(productDetailDTO)){
                    sender.sendResponseToGateWay(objectMapper.writeValueAsString(productDetailDTO));
                }else{
                    sender.sendCallToPriceService(objectMapper.writeValueAsString(productDetailDTO));
                }
            }
            if (callRequestDTO.getType().equals("component") && callRequestDTO.getDetailID() !=null){
                sender.sendResponseToGateWay(getComponentJson(callRequestDTO));
            }
            if(callRequestDTO.getType().equals("component") && callRequestDTO.getDetailID() ==null){
                sender.sendResponseToGateWay(getAllComponentsJson(callRequestDTO));
            }
            if(callRequestDTO.getType().equals("product") && callRequestDTO.getDetailID() ==null){
                ProductDetailDTO productDetailDTO = getAllProducts(callRequestDTO);
                if (isPriceCalculated(productDetailDTO)){
                    sender.sendResponseToGateWay(objectMapper.writeValueAsString(productDetailDTO));
                }else{
                    sender.sendCallToPriceService(objectMapper.writeValueAsString(productDetailDTO));
                }
            }
            log.info("Received and processed message: " + jsonMessage);
        }catch (Exception e){
            try {
                CallCreateDTO callCreateDTO = objectMapper.readValue(jsonMessage, CallCreateDTO.class);
                ProductDetailDTO productDetailDTO = createPlanetarySystem(callCreateDTO);
                if (isPriceCalculated(productDetailDTO)){
                    sender.sendResponseToGateWay(objectMapper.writeValueAsString(productDetailDTO));
                }else{
                    sender.sendCallToPriceService(objectMapper.writeValueAsString(productDetailDTO));
                }
                log.info("Received and processed message: " + jsonMessage);
            }catch (Exception f){
                log.error("Message could not be parsed: "+jsonMessage +System.lineSeparator() +f +System.lineSeparator()+e);
            }
        }
    }

    /**
     * Helper Method to check if the price of a Product is already calculated.
     * @param productDetailDTO {@link com.kbertv.productService.model.dto.ProductDetailDTO}
     * @return true if price is calculated, else false
     */
    private boolean isPriceCalculated(ProductDetailDTO productDetailDTO){
        ArrayList<PlanetarySystem> planetarySystems = productDetailDTO.getPlanetarySystems();
        boolean flag = true;
        for (PlanetarySystem planetarySystem : planetarySystems) {
            if (planetarySystem.getPrice() == 0) {
                flag = false;
                break;
            }
        }
        return flag;
    }


    /**
     * Forwards all messages to the Gateway and caches them
     * @param jsonMessage Message
     */
    @RabbitListener(queues = {"${rabbitmq.queue.response.price.name}"})
    public void consumeCallFromPriceService(String jsonMessage) {
        ProductDetailDTO productDetailDTO;
        try {
            productDetailDTO = objectMapper.readValue(jsonMessage, ProductDetailDTO.class);
            ArrayList<PlanetarySystem> planetarySystems = productDetailDTO.getPlanetarySystems();
            for (PlanetarySystem planetarySystem:planetarySystems){
                putPlanetarySystemWithPriceInCache(planetarySystem.getId(),planetarySystem);
            }
        }catch (Exception e){
            log.error("Message could not be parsed: "+jsonMessage +System.lineSeparator() +e);
        }
        sender.sendResponseToGateWay(jsonMessage);
        log.info("Received and processed message: " + jsonMessage);
    }

    /**
     * Helper Method to cache {@link com.kbertv.productService.model.PlanetarySystem}
     * @param uuid ID of {@link com.kbertv.productService.model.PlanetarySystem}
     * @param planetarySystem {@link com.kbertv.productService.model.PlanetarySystem}
     * @return {@link com.kbertv.productService.model.PlanetarySystem}
     */
    @CachePut(cacheNames = "productCache", key = "#p0")
    public PlanetarySystem putPlanetarySystemWithPriceInCache(UUID uuid, PlanetarySystem planetarySystem){
        return planetarySystem;
    }

    /**
     * Helper Method to create {@link com.kbertv.productService.model.PlanetarySystem}
     * @param callCreateDTO {@link com.kbertv.productService.model.dto.CallCreateDTO}
     * @return {@link com.kbertv.productService.model.dto.ProductDetailDTO}
     */
    private ProductDetailDTO createPlanetarySystem(CallCreateDTO callCreateDTO) {
        PlanetarySystem callPlanetarySystem = callCreateDTO.getPlanetarySystem();
        PlanetarySystem planetarySystem = productService.createPlanetarySystem(callPlanetarySystem.getName(),callPlanetarySystem.getOwner(),callPlanetarySystem.getCelestialBodies());
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        if (planetarySystem != null){
            planetarySystems.add(planetarySystem);
        }
        return new ProductDetailDTO(callCreateDTO.getRequestID(),planetarySystems);
    }

    /**
     * Helper Method to create and fill the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ComponentDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    private String getAllComponentsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        ArrayList<CelestialBody> celestialBodies = (ArrayList<CelestialBody>) productService.getAllComponents();
        ComponentDetailDTO response = new ComponentDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and fill the correct response DTO.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ProductDetailDTO} as JSON String
     */
    private ProductDetailDTO getAllProducts(CallRequestDTO callRequestDTO) {
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) productService.getAllProducts();
        return new ProductDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
    }

    /**
     * Helper Method to create and fill the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ComponentDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    private String getComponentJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        Optional<CelestialBody> result = productService.getComponent(callRequestDTO.getDetailID());
        ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
        result.ifPresent(celestialBodies::add);
        ComponentDetailDTO response = new ComponentDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and fill the correct response DTO.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link com.kbertv.productService.model.dto.ProductDetailDTO} as JSON String
     */
    private ProductDetailDTO getProduct(CallRequestDTO callRequestDTO) {
        Optional<PlanetarySystem> result = productService.getProduct(callRequestDTO.getDetailID());
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        result.ifPresent(planetarySystems::add);
        return new ProductDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
    }
}
