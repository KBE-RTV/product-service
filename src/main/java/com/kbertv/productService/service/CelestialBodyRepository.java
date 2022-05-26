package com.kbertv.productService.service;

import com.kbertv.productService.model.CelestialBody;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CelestialBodyRepository extends MongoRepository<CelestialBody, UUID> {
}
