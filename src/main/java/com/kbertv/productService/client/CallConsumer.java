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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CallConsumer {

    private ObjectMapper objectMapper = new ObjectMapper();
    private ProductService productService;
    private ResponseProducer responseProducer;

    public CallConsumer(ProductService productService, ResponseProducer responseProducer) {
        this.productService = productService;
        this.responseProducer = responseProducer;
    }

    @RabbitListener(queues = {"${rabbitmq.queue.call.name}"})
    public void consumeCallFromGateWay(String jsonMessage){
        try {
            CallRequestDTO callRequestDTO = objectMapper.convertValue(jsonMessage, CallRequestDTO.class);
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
        }catch (Exception e){
            CallCreateDTO callCreateDTO = objectMapper.convertValue(jsonMessage, CallCreateDTO.class);

        }
        System.out.println("RECEIVED FROM GATEWAY: " + jsonMessage);
    }

    @RabbitListener(queues = {"${rabbitmq.queue.call.price.name}"})
    public void consumeCallFromPriceService(String jsonMessage){
        System.out.println("RECEIVED FROM PRICE SERVICE: " + jsonMessage);
    }

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
