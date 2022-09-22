package com.kbertv.productService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class PlanetarySystem implements Serializable {

    @Id
    private UUID id;
    private String name;
    private String owner;
    private ArrayList<CelestialBody> celestialBodies = new ArrayList<>();
    private float price;
}