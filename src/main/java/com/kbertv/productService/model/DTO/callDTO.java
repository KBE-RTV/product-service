package com.kbertv.productService.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class callDTO {
    private UUID requestID;
    private UUID detailID;
    private String type;

    public callDTO(@JsonProperty("requestID") UUID requestID,
                   @JsonProperty("detailID") UUID detailID,
                   @JsonProperty("type") String type){
        this.requestID = requestID;
        this.detailID = detailID;
        this.type = type;
    }
}
