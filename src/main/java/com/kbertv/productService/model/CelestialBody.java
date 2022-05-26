package com.kbertv.productService.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "components")
public class CelestialBody{

    @Id
    private UUID id;
    private String name;
    private int amount;
    private float price;
    private CelestialBodyTypes type;
    private int orbital;
    private float radius;
    private float volume;
    private float mass;
    private float gravity;
    private float rotationVelocity;
    private float orbitalVelocity;
    private float surfaceTemperature;

    public void addAmount(int amount){
        if (amount>0){
            this.amount +=amount;
        }
    }
}