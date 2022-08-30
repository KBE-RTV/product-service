package com.kbertv.productService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbertv.productService.exception.CelestialBodyNotFoundException;
import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.PlanetarySystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.util.*;

@Service
public class ProductService implements IProductService{

    private final CelestialBodyRepository celestialBodyRepository;
    private final PlanetarySystemRepository planetarySystemRepository;
    @Value("${warehouse.baseurl}")
    private String WAREHOUSE_BASEURL;

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

    @Override
    @Cacheable(value = "allProductsCache")
    public List<PlanetarySystem> getAllProducts() {
        return planetarySystemRepository.findAll();
    }

    @Override
    @Cacheable(value = "productCache")
    public Optional<PlanetarySystem> getProduct(UUID id) {
        return planetarySystemRepository.findById(id);
    }

    @Override
    @Cacheable(value = "allComponentsCache")
    public List<CelestialBody> getAllComponents() {
        return celestialBodyRepository.findAll();
    }

    @Override
    @Cacheable(value = "componentCache")
    public Optional<CelestialBody> getComponent(UUID id) {
        return celestialBodyRepository.findById(id);
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
            return celestialBodies.get(0).getType().equals("sun");
        }
    }

    private ArrayList<CelestialBody> idToObjects(ArrayList<UUID> idList) throws CelestialBodyNotFoundException {
        ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
        for (UUID uuid : idList) {
            celestialBodies.add(celestialBodyRepository.findById(uuid).orElseThrow(()->new CelestialBodyNotFoundException(uuid+" not found")));
        }
        return celestialBodies;
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
            String celestialBodyJSON = restTemplate.getForEntity(WAREHOUSE_BASEURL + "components", String.class).getBody();
            CelestialBody[] celestialBodies = objectMapper.readValue(celestialBodyJSON, CelestialBody[].class);
            celestialBodyRepository.saveAll(Arrays.asList(celestialBodies));
            System.out.println("Celestial Bodies imported");
        }catch (RestClientException e){
            System.err.println("Warehouse not reachable: " + e.getMessage());
        } catch (JsonProcessingException e) {
            System.err.println("Import failed: "+ e.getMessage());
        }
    }

    private void getPlanetarySystemsFromWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            String planetarySystemsJSON = restTemplate.getForEntity(WAREHOUSE_BASEURL + "products", String.class).getBody();
            PlanetarySystem[] planetarySystems = objectMapper.readValue(planetarySystemsJSON, PlanetarySystem[].class);
            planetarySystemRepository.saveAll(Arrays.asList(planetarySystems));
            System.out.println("Planetary Systems imported");
        }catch (RestClientException e){
            System.err.println("Warehouse not reachable: " + e.getMessage());
        } catch (JsonProcessingException e) {
            System.err.println("Import failed: "+ e.getMessage());
        }
    }

    @PreDestroy
    public void saveInventoryToWarehouse(){
        RestTemplate restTemplate = new RestTemplate();
        String url = WAREHOUSE_BASEURL + "products";
        ArrayList<PlanetarySystem> planetarySystems = (ArrayList<PlanetarySystem>) planetarySystemRepository.findAll();
        restTemplate.postForObject(url,planetarySystems, ArrayList.class);
    }
}
