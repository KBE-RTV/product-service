package com.kbertv.productService.service;

import com.kbertv.productService.model.PlanetarySystem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PlanetarySystemRepository extends MongoRepository<PlanetarySystem, UUID> {
}
