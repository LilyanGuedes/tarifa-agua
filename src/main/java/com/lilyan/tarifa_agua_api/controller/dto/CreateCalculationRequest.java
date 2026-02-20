package com.lilyan.tarifa_agua_api.controller.dto;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateCalculationRequest(
        @NotNull(message = "Categoria é obrigatória.") ConsumerCategory categoria,
        @NotNull(message = "Consumo é obrigatório.") @Min(value = 0, message = "Consumo deve ser maior ou igual a 0.") Integer consumo
) {}
