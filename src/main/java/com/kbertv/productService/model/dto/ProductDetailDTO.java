package com.kbertv.productService.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbertv.productService.model.PlanetarySystem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@ToString
public class ProductDetailDTO {
    private UUID requestID;
    private ArrayList<PlanetarySystem> planetarySystem;

    public ProductDetailDTO(@JsonProperty("requestID") UUID requestID,
                            @JsonProperty("planetarySystem") ArrayList<PlanetarySystem> planetarySystem){
        this.requestID = requestID;
        this.planetarySystem = planetarySystem;
    }
}