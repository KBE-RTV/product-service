package com.kbertv.productService.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbertv.productService.model.PlanetarySystem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class ProductDetailDTO {
    private UUID requestID;
    private PlanetarySystem planetarySystem;

    public ProductDetailDTO(@JsonProperty("requestID") UUID requestID,
                            @JsonProperty("planetarySystem") PlanetarySystem planetarySystem){
        this.requestID = requestID;
        this.planetarySystem = planetarySystem;
    }
}
