package com.kbertv.productService.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class CallRequestDTO {
    private UUID requestID;
    private UUID detailID;
    private String type;

    public CallRequestDTO(@JsonProperty("requestID") UUID requestID,
                          @JsonProperty("detailID") UUID detailID,
                          @JsonProperty("type") String type) {
        this.requestID = requestID;
        this.detailID = detailID;
        this.type = type;
    }

    public boolean isRequestTypeAllPlanetarySystems() {
        return type.equals("planetarySystem") && detailID == null;
    }

    public boolean isRequestTypePlanetarySystem() {
        return type.equals("planetarySystem") && detailID != null;
    }

    public boolean isRequestTypeAllCelestialBodies() {
        return type.equals("celestialBody") && detailID == null;
    }

    public boolean isRequestTypeCelestialBody() {
        return type.equals("celestialBody") && detailID != null;
    }
}