package com.kbertv.productService.service;

import com.kbertv.productService.model.CelestialBody;
import com.kbertv.productService.model.PlanetarySystem;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for the Product service.
 */
public interface IProductService {

    /**
     * Creates a planetary system.
     *
     * @param name            Name of the System
     * @param owner           Name of the Owner
     * @param celestialBodies Celestial Bodies of the System
     * @param price           Price of the System
     * @return the created planetary system
     */
    PlanetarySystem createPlanetarySystem(String name, String owner, ArrayList<CelestialBody> celestialBodies, float price);

    /**
     * Gets all Products form the DB
     *
     * @return List of all Products
     */
    List<PlanetarySystem> getAllProducts();

    /**
     * Gets a specific Product from the DB
     *
     * @param id UUID of the Product
     * @return  Optional with Product if UUID could be found.
     *          Empty Optional if UUID could not be found.
     */
    Optional<PlanetarySystem> getProduct(UUID id);

    /**
     * Gets all Components form the DB
     *
     * @return List of all Components
     */
    List<CelestialBody> getAllComponents();

    /**
     * Gets a specific Component from the DB
     *
     * @param id UUID of the Component
     * @return  Optional with Component if UUID could be found.
     *          Empty Optional if UUID could not be found.
     */
    Optional<CelestialBody> getComponent(UUID id);

    /**
     * mirrors the Warehouse DB to the Product Service DB
     */
    @EventListener(ApplicationReadyEvent.class)
    void getInventoryFromWarehouse();
}
