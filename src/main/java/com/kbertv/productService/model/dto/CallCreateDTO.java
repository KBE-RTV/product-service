package com.kbertv.productService.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbertv.productService.model.PlanetarySystem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class CallCreateDTO {
    private UUID requestID;
    private PlanetarySystem planetarySystem;

    public CallCreateDTO(@JsonProperty("requestID") UUID requestID,
                         @JsonProperty("detailID") PlanetarySystem planetarySystem) {
        this.requestID = requestID;
        this.planetarySystem = planetarySystem;
    }
}
