package com.kbertv.productService.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbertv.productService.model.CelestialBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@ToString
public class CelestialBodyDetailDTO {
    private UUID requestID;
    private ArrayList<CelestialBody> celestialBody;

    public CelestialBodyDetailDTO(@JsonProperty("requestID") UUID requestID,
                                  @JsonProperty("celestialBody") ArrayList<CelestialBody> celestialBody) {
        this.requestID = requestID;
        this.celestialBody = celestialBody;
    }
}
