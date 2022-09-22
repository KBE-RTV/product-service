package com.kbertv.productService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.PlanetarySystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.util.*;

/**
 * Service with provides the functionality of the Product Service
 */
@Service
@Slf4j
public class ProductService implements IProductService{

    private final CelestialBodyRepository celestialBodyRepository;
    private final PlanetarySystemRepository planetarySystemRepository;
    @Value("${warehouse.baseurl}")
    private String warehouseBaseurl;

    /**
     * Instantiates a new Product service.
     *
     * @param celestialBodyRepository   the celestial body repository
     * @param planetarySystemRepository the planetary system repository
     */
    public ProductService(CelestialBodyRepository celestialBodyRepository, PlanetarySystemRepository planetarySystemRepository) {
        this.celestialBodyRepository = celestialBodyRepository;
        this.planetarySystemRepository = planetarySystemRepository;
    }

    @Override
    public PlanetarySystem createPlanetarySystem(String name, String owner, ArrayList<CelestialBody> celestialBodies) {
        if (isCompositionCorrect(celestialBodies)){
            return planetarySystemRepository.save(new PlanetarySystem(UUID.randomUUID(),name ,owner, celestialBodies,0f));
        }else{
            return null;
        }
    }

    @Override
    public  List<PlanetarySystem> getAllPlanetarySystems() {
        return planetarySystemRepository.findAll();
    }

    @Override
    @Cacheable(value = "planetarySystemCache")
    public Optional<PlanetarySystem> getPlanetarySystem(UUID id) {
        return planetarySystemRepository.findById(id);
    }

    @Override
    public List<CelestialBody> getAllCelestialBodies() {
        return celestialBodyRepository.findAll();
    }

    @Override
    @Cacheable(value = "celestialBodyCache")
    public Optional<CelestialBody> getCelestialBody(UUID id) {
        return celestialBodyRepository.findById(id);
    }

    private boolean isCompositionCorrect(ArrayList<CelestialBody> celestialBodies) {
        if (celestialBodies.isEmpty()){
            return false;
        }else{
            return celestialBodies.get(0).getType().equals("sun");
        }
    }

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void getInventoryFromWarehouse(){
        getCelestialBodiesFromWarehouse();
        getPlanetarySystemsFromWarehouse();
    }

    private void getCelestialBodiesFromWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            String celestialBodyJSON = restTemplate.getForEntity(warehouseBaseurl + "celestialBodies", String.class).getBody();
            CelestialBody[] celestialBodies = objectMapper.readValue(celestialBodyJSON, CelestialBody[].class);
            celestialBodyRepository.saveAll(Arrays.asList(celestialBodies));
            log.info("Celestial Bodies imported");
        }catch (RestClientException e){
            log.error("Warehouse not reachable: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Import failed: "+ e.getMessage());
        }
    }

    private void getPlanetarySystemsFromWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            String planetarySystemsJSON = restTemplate.getForEntity(warehouseBaseurl + "planetarySystems", String.class).getBody();
            PlanetarySystem[] planetarySystems = objectMapper.readValue(planetarySystemsJSON, PlanetarySystem[].class);
            planetarySystemRepository.saveAll(Arrays.asList(planetarySystems));
            log.info("Planetary Systems imported");
        }catch (RestClientException e){
            log.error("Warehouse not reachable: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Import failed: "+ e.getMessage());
        }
    }

    /**
     * mirrors the Product Service DB to the Warehouse DB
     */
    @PreDestroy
    public void saveInventoryToWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        String url = warehouseBaseurl + "planetarySystems";
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) planetarySystemRepository.findAll();
        planetarySystems.forEach((planetarySystem -> planetarySystem.setPrice(0)));
        restTemplate.postForObject(url,planetarySystems, ArrayList.class);
    }
}
