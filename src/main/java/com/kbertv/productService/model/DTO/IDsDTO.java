package com.kbertv.productService.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@ToString
public class IDsDTO {
    private UUID requestID;
    private ArrayList<UUID> uuids;

    public IDsDTO(@JsonProperty("requestID") UUID requestID,
                  @JsonProperty("uuids") ArrayList<UUID> uuids){
        this.requestID = requestID;
        this.uuids = uuids;
    }
}
