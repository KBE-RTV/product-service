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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Class wich receives messages form the queue
 */
@Service
@Slf4j
public class Receiver {

    private final IProductService productService;
    private final Sender sender;
    private ObjectMapper objectMapper;

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
    public String consumeCallFromGateWay(String jsonMessage) {
        objectMapper = new ObjectMapper();
        try {
            CallRequestDTO callRequestDTO = objectMapper.readValue(jsonMessage, CallRequestDTO.class);
            if (callRequestDTO.isRequestTypePlanetarySystem()) {
                PlanetarySystemDetailDTO planetarySystemDetailDTO = getDTOPlanetarySystem(callRequestDTO);
                if (planetarySystemDetailDTO.isPriceCalculated()) {
                    return objectMapper.writeValueAsString(planetarySystemDetailDTO);
                } else {
                    return cacheResponseFromPriceService(sender.sendAndReceiveCallToPriceService(objectMapper.writeValueAsString(planetarySystemDetailDTO)));
                }
            }
            if (callRequestDTO.isRequestTypeCelestialBody()) {
                return getDTOCelestialBodyAsJson(callRequestDTO);
            }
            if (callRequestDTO.isRequestTypeAllCelestialBodies()) {
                return getDTOAllCelestialBodiesAsJson(callRequestDTO);
            }
            if (callRequestDTO.isRequestTypeAllPlanetarySystems()) {
                PlanetarySystemDetailDTO planetarySystemDetailDTO = getDTOAllPlanetarySystems(callRequestDTO);
                return getDTOWithPrice(planetarySystemDetailDTO);
            }
            log.error("Message could be parsed but request not read");
        } catch (Exception e) {
            try {
                CallCreateDTO callCreateDTO = objectMapper.readValue(jsonMessage, CallCreateDTO.class);
                PlanetarySystemDetailDTO planetarySystemDetailDTO = createPlanetarySystem(callCreateDTO);
                return getDTOWithPrice(planetarySystemDetailDTO);
            } catch (Exception f) {
                log.error("Message could not be parsed: " + jsonMessage + System.lineSeparator() + f + System.lineSeparator() + e);
            }
        }
        return "";
    }

    private String getDTOWithPrice(PlanetarySystemDetailDTO planetarySystemDetailDTO) throws JsonProcessingException, ExecutionException, InterruptedException {
        if (planetarySystemDetailDTO.isPriceCalculated()) {
            String response = objectMapper.writeValueAsString(planetarySystemDetailDTO);
            log.info("Received and processed message: " + objectMapper.writeValueAsString(planetarySystemDetailDTO));
            return response;
        } else {
            String response = cacheResponseFromPriceService(sender.sendAndReceiveCallToPriceService(objectMapper.writeValueAsString(planetarySystemDetailDTO)));
            log.info("Received and processed message: " + objectMapper.writeValueAsString(planetarySystemDetailDTO));
            return response;
        }
    }

    public String cacheResponseFromPriceService(String jsonMessage) {
        try {
            PlanetarySystemDetailDTO planetarySystemDetailDTO = objectMapper.readValue(jsonMessage, PlanetarySystemDetailDTO.class);
            for (PlanetarySystem planetarySystem : planetarySystemDetailDTO.getPlanetarySystems()) {
                productService.cachePlanetarySystem(planetarySystem);
            }
        } catch (Exception e) {
            log.error("Message could not be parsed: " + jsonMessage + System.lineSeparator() + e);
        }
        return jsonMessage;
    }

    /**
     * Helper Method to create {@link com.kbertv.productService.model.PlanetarySystem}
     *
     * @param callCreateDTO {@link com.kbertv.productService.model.dto.CallCreateDTO}
     * @return {@link PlanetarySystemDetailDTO}
     */
    private PlanetarySystemDetailDTO createPlanetarySystem(CallCreateDTO callCreateDTO) {
        PlanetarySystem planetarySystem = productService.savePlanetarySystem(callCreateDTO.getPlanetarySystem());
        ArrayList<PlanetarySystem> planetarySystems = new ArrayList<>();
        if (planetarySystem != null) {
            planetarySystems.add(planetarySystem);
        }
        return new PlanetarySystemDetailDTO(callCreateDTO.getRequestID(), planetarySystems);
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
        return new PlanetarySystemDetailDTO(callRequestDTO.getRequestID(), planetarySystems);
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
        CelestialBodyDetailDTO response = new CelestialBodyDetailDTO(callRequestDTO.getRequestID(), celestialBodies);
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
        return new PlanetarySystemDetailDTO(callRequestDTO.getRequestID(), planetarySystems);
    }
}
