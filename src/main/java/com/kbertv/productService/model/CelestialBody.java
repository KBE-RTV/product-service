package com.kbertv.productService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "components")
public class CelestialBody implements Serializable {

    @Id
    private UUID id;
    private String name;
    private int amount;
    private float price;
    private String type;
    private int orbital;
    private float radius;
    private float volume;
    private float mass;
    private float gravity;
    private float rotationVelocity;
    private float orbitalVelocity;
    private float surfaceTemperature;
}