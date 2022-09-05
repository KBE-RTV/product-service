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

public interface IProductService {

    PlanetarySystem createPlanetarySystem(String name ,String owner, ArrayList<CelestialBody> celestialBodies);

    List<PlanetarySystem> getAllProducts();

    Optional<PlanetarySystem> getProduct(UUID id);

    List<CelestialBody> getAllComponents();

    Optional<CelestialBody> getComponent(UUID id);

    @EventListener(ApplicationReadyEvent.class)
    void getInventoryFromWarehouse();
}
