package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DateRange {
    @NotNull
    @JsonProperty("start")
    private LocalDate start;

    @NotNull
    @JsonProperty("end")
    private LocalDate end;
}
