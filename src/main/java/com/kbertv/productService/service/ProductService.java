package com.kbertv.productService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbertv.productService.exception.CelestialBodyNotFoundException;
import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.CelestialBodyTypes;
import com.kbertv.productService.model.PlanetarySystem;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
public class ProductService implements IProductService{

    private final CelestialBodyRepository celestialBodyRepository;
    private final PlanetarySystemRepository planetarySystemRepository;

    public ProductService(CelestialBodyRepository celestialBodyRepository, PlanetarySystemRepository planetarySystemRepository) {
        this.celestialBodyRepository = celestialBodyRepository;
        this.planetarySystemRepository = planetarySystemRepository;
    }

    @Override
    public PlanetarySystem createPlanetarySystem(String name ,String owner, ArrayList<UUID> celestialBodies) {

        if (isCompositionCorrect(celestialBodies)){
            return planetarySystemRepository.save(new PlanetarySystem(UUID.randomUUID(),name ,owner, celestialBodies));
        }else{
            return null;
        }

    }

    private boolean isCompositionCorrect(ArrayList<UUID> celestialBodyIds) {
        if (celestialBodyIds.isEmpty()){
            return false;
        }else {
            ArrayList<CelestialBody> celestialBodies;
            try {
                celestialBodies = idToObjects(celestialBodyIds);
            } catch (CelestialBodyNotFoundException e) {
                return false;
            }
            return celestialBodies.get(0).getType().equals(CelestialBodyTypes.sun);
        }
    }

    private ArrayList<CelestialBody> idToObjects(ArrayList<UUID> idList) throws CelestialBodyNotFoundException {
        ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
        for (UUID uuid : idList) {
            celestialBodies.add(celestialBodyRepository.findById(uuid).orElseThrow(()->new CelestialBodyNotFoundException(uuid+" not found")));
        }
        return celestialBodies;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void getInventoryFromWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            String celestialBodyJSON = restTemplate.getForEntity("http://localhost:8080/components", String.class).getBody();
            String planetarySystemsJSON = restTemplate.getForEntity("http://localhost:8080/products", String.class).getBody();

            CelestialBody[] celestialBodies = objectMapper.readValue(celestialBodyJSON, CelestialBody[].class);
            PlanetarySystem[] planetarySystems = objectMapper.readValue(planetarySystemsJSON, PlanetarySystem[].class);

            celestialBodyRepository.saveAll(Arrays.asList(celestialBodies));
            planetarySystemRepository.saveAll(Arrays.asList(planetarySystems));

            System.out.println("Warehouse imported");
        }catch (RestClientException e){
            System.err.println("Warehouse not reachable");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.err.println("Import failed");
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void saveInventoryToWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/products/";
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) planetarySystemRepository.findAll();

        for (PlanetarySystem system : planetarySystems) {
            HttpEntity<PlanetarySystem> request = new HttpEntity<>(system);
            url += "" + system.getId().toString();
            restTemplate.postForObject(url, request, PlanetarySystem.class);
        }
    }
}
