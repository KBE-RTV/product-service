package com.kbertv.productService.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
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