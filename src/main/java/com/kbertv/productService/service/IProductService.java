package com.kbertv.productService.service;

import com.kbertv.productService.model.PlanetarySystem;

import java.util.ArrayList;
import java.util.UUID;

public interface IProductService {

    PlanetarySystem createPlanetarySystem(String name, String owner, ArrayList<UUID> celestialBodies);

}
