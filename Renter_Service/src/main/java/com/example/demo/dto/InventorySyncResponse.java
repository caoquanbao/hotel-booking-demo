package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventorySyncResponse {
    @JsonProperty("request_id")
    private String requestId;

    private String status;
    private String message;
}
