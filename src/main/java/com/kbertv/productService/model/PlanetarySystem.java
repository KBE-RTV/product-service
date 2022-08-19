package com.kbertv.productService.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class PlanetarySystem {

    @Id
    private UUID id;

    private String name;

    private String owner;

    private ArrayList<UUID> celestialBodies = new ArrayList<>();
}