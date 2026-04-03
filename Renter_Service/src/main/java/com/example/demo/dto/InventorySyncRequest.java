package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class InventorySyncRequest {

    @NotBlank
    @JsonProperty("request_id")
    private String requestId;

    @NotBlank
    @JsonProperty("schema_version")
    private String schemaVersion;

    @NotNull
    @JsonProperty("timestamp")
    private Instant timestamp;

    @NotBlank
    @JsonProperty("timezone")
    private String timezone;

    @NotBlank
    @JsonProperty("hotel_id")
    private String hotelId;

    @NotBlank
    @JsonProperty("source")
    private String source;

    @NotEmpty
    @Valid
    @JsonProperty("inventories")
    private List<InventoryItem> inventories;
}
