package com.kbertv.productService.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kbertv.productService.model.CelestialBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class ComponentDetailDTO {
    private UUID requestID;
    private CelestialBody celestialBody;

    public ComponentDetailDTO(@JsonProperty("requestID") UUID requestID,
                              @JsonProperty("celestialBody") CelestialBody celestialBody){
        this.requestID = requestID;
        this.celestialBody = celestialBody;
    }
}
