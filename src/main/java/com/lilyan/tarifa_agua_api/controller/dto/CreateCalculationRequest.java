package com.lilyan.tarifa_agua_api.controller.dto;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateCalculationRequest {

    @NotNull(message = "Categoria é obrigatória.")
    private ConsumerCategory categoria;

    @NotNull(message = "Consumo é obrigatório.")
    @Min(value = 0, message = "Consumo deve ser maior ou igual a 0.")
    private Integer consumo;

    public ConsumerCategory getCategoria() { return categoria; }

    public void setCategoria(ConsumerCategory categoria) { this.categoria = categoria; }

    public Integer getConsumo() { return consumo; }

    public void setConsumo(Integer consumo) { this.consumo = consumo; }
}
