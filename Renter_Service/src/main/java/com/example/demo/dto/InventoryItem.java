package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryItem {

    @NotBlank
    @JsonProperty("room_type_id")
    private String roomTypeId;

    @NotBlank
    @JsonProperty("rate_plan_id")
    private String ratePlanId;

    @NotNull
    @Valid
    @JsonProperty("date_range")
    private DateRange dateRange;

    @NotNull
    @Min(0)
    @JsonProperty("total_inventory")
    private Integer totalInventory;

    @NotBlank
    @JsonProperty("status")
    private String status;

    @Valid
    @JsonProperty("restrictions")
    private Restrictions restrictions;
}
