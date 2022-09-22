package com.kbertv.productService.service;

import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.PlanetarySystem;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for the Product service.
 */
public interface IProductService {

    /**
     * Caches planetary system
     */
    void cachePlanetarySystem(PlanetarySystem planetarySystem);

    /**
     * Saves a planetary system and caches it.
     *
     * @return the saved planetary system
     */
    PlanetarySystem savePlanetarySystem(PlanetarySystem planetarySystem);

    /**
     * Gets all planetary systems from the DB
     *
     * @return List of all planetary systems
     */
    List<PlanetarySystem> getAllPlanetarySystems();

    /**
     * Gets a specific planetary system from the DB
     *
     * @param id UUID of the planetary system
     * @return Optional with planetary system if UUID could be found.
     * Empty Optional if UUID could not be found.
     */
    Optional<PlanetarySystem> getPlanetarySystem(UUID id);

    /**
     * Gets all celestial bodies form the DB
     *
     * @return List of all celestial bodies
     */
    List<CelestialBody> getAllCelestialBodies();

    /**
     * Gets a specific celestial body from the DB
     *
     * @param id UUID of the celestial body
     * @return Optional with celestial body if UUID could be found.
     * Empty Optional if UUID could not be found.
     */
    Optional<CelestialBody> getCelestialBody(UUID id);

    /**
     * mirrors the Warehouse DB to the Product Service DB
     */
    @EventListener(ApplicationReadyEvent.class)
    void getInventoryFromWarehouse();
}
