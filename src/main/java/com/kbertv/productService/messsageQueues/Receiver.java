package com.kbertv.productService.messsageQueues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.PlanetarySystem;
import com.kbertv.productService.model.dto.CallCreateDTO;
import com.kbertv.productService.model.dto.CallRequestDTO;
import com.kbertv.productService.model.dto.CelestialBodyDetailDTO;
import com.kbertv.productService.model.dto.PlanetarySystemDetailDTO;
import com.kbertv.productService.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Class wich receives messages form the queue
 */
@Service
@Slf4j
public class Receiver {

    private ObjectMapper objectMapper;
    private final IProductService productService;
    private final Sender sender;

    /**
     * Instantiates a new Receiver.
     *
     * @param productService the product service
     * @param sender         the sender
     */
    public Receiver(IProductService productService, Sender sender) {
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
            if (callRequestDTO.isRequestTypePlanetarySystem()){
                PlanetarySystemDetailDTO planetarySystemDetailDTO = getDTOPlanetarySystem(callRequestDTO);
                if (planetarySystemDetailDTO.isPriceCalculated()){
                    sender.sendResponseToGateWay(objectMapper.writeValueAsString(planetarySystemDetailDTO));
                }else{
                    sender.sendCallToPriceService(objectMapper.writeValueAsString(planetarySystemDetailDTO));
                }
            }
            if (callRequestDTO.isRequestTypeCelestialBody()){
                sender.sendResponseToGateWay(getDTOCelestialBodyAsJson(callRequestDTO));
            }
            if(callRequestDTO.isRequestTypeAllCelestialBodies()){
                sender.sendResponseToGateWay(getDTOAllCelestialBodiesAsJson(callRequestDTO));
            }
            if(callRequestDTO.isRequestTypeAllPlanetarySystems()){
                PlanetarySystemDetailDTO planetarySystemDetailDTO = getDTOAllPlanetarySystems(callRequestDTO);
                if (planetarySystemDetailDTO.isPriceCalculated()){
                    sender.sendResponseToGateWay(objectMapper.writeValueAsString(planetarySystemDetailDTO));
                }else{
                    sender.sendCallToPriceService(objectMapper.writeValueAsString(planetarySystemDetailDTO));
                }
            }
            log.info("Received and processed message: " + jsonMessage);
        }catch (Exception e){
            try {
                CallCreateDTO callCreateDTO = objectMapper.readValue(jsonMessage, CallCreateDTO.class);
                PlanetarySystemDetailDTO planetarySystemDetailDTO = createPlanetarySystem(callCreateDTO);
                if (planetarySystemDetailDTO.isPriceCalculated()){
                    sender.sendResponseToGateWay(objectMapper.writeValueAsString(planetarySystemDetailDTO));
                }else{
                    sender.sendCallToPriceService(objectMapper.writeValueAsString(planetarySystemDetailDTO));
                }
                log.info("Received and processed message: " + jsonMessage);
            }catch (Exception f){
                log.error("Message could not be parsed: "+jsonMessage +System.lineSeparator() +f +System.lineSeparator()+e);
            }
        }
    }

    /**
     * Forwards all messages to the Gateway and caches them
     * @param jsonMessage Message
     */
    @RabbitListener(queues = {"${rabbitmq.queue.response.price.name}"})
    public void consumeCallFromPriceService(String jsonMessage) {
        PlanetarySystemDetailDTO planetarySystemDetailDTO;
        try {
            planetarySystemDetailDTO = objectMapper.readValue(jsonMessage, PlanetarySystemDetailDTO.class);
            for (PlanetarySystem planetarySystem:planetarySystemDetailDTO.getPlanetarySystems()){
                productService.cachePlanetarySystem(planetarySystem);
            }
        }catch (Exception e){
            log.error("Message could not be parsed: "+jsonMessage +System.lineSeparator() +e);
        }
        sender.sendResponseToGateWay(jsonMessage);
        log.info("Received and processed message: " + jsonMessage);
    }

    /**
     * Helper Method to create {@link com.kbertv.productService.model.PlanetarySystem}
     * @param callCreateDTO {@link com.kbertv.productService.model.dto.CallCreateDTO}
     * @return {@link PlanetarySystemDetailDTO}
     */
    private PlanetarySystemDetailDTO createPlanetarySystem(CallCreateDTO callCreateDTO) {
        PlanetarySystem planetarySystem = productService.savePlanetarySystem(callCreateDTO.getPlanetarySystem());
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        if (planetarySystem != null){
            planetarySystems.add(planetarySystem);
        }
        return new PlanetarySystemDetailDTO(callCreateDTO.getRequestID(),planetarySystems);
    }

    /**
     * Helper Method to create and populate the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link CelestialBodyDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    private String getDTOAllCelestialBodiesAsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        CelestialBodyDetailDTO response = new CelestialBodyDetailDTO(callRequestDTO.getRequestID(), (ArrayList<CelestialBody>) productService.getAllCelestialBodies());
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and populate the correct response DTO.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link PlanetarySystemDetailDTO} as JSON String
     */
    private PlanetarySystemDetailDTO getDTOAllPlanetarySystems(CallRequestDTO callRequestDTO) {
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) productService.getAllPlanetarySystems();
        return new PlanetarySystemDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
    }

    /**
     * Helper Method to create and populate the correct response DTO and converts it to JSON String.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link CelestialBodyDetailDTO} as JSON String
     * @throws JsonProcessingException if the DTO could not be parsed as JSON String
     */
    private String getDTOCelestialBodyAsJson(CallRequestDTO callRequestDTO) throws JsonProcessingException {
        ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
        productService.getCelestialBody(callRequestDTO.getDetailID()).ifPresent(celestialBodies::add);
        CelestialBodyDetailDTO response = new CelestialBodyDetailDTO(callRequestDTO.getRequestID(),celestialBodies);
        return objectMapper.writeValueAsString(response);
    }

    /**
     * Helper Method to create and populate the correct response DTO.
     *
     * @param callRequestDTO {@link com.kbertv.productService.model.dto.CallRequestDTO}
     * @return {@link PlanetarySystemDetailDTO} as JSON String
     */
    private PlanetarySystemDetailDTO getDTOPlanetarySystem(CallRequestDTO callRequestDTO) {
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        productService.getPlanetarySystem(callRequestDTO.getDetailID()).ifPresent(planetarySystems::add);
        return new PlanetarySystemDetailDTO(callRequestDTO.getRequestID(),planetarySystems);
    }
}
