package com.lilyan.tarifa_agua_api.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RangeRequest(
        @NotNull @Min(0) Integer start,
        @NotNull @Min(0) Integer end,
        @NotNull BigDecimal unitPrice
) {}
