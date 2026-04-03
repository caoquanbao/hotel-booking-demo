package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Restrictions {
    @JsonProperty("min_stay")
    private Integer minStay;

    @JsonProperty("max_stay")
    private Integer maxStay;

    @JsonProperty("closed_to_arrival")
    private Boolean closedToArrival;

    @JsonProperty("closed_to_departure")
    private Boolean closedToDeparture;
}
