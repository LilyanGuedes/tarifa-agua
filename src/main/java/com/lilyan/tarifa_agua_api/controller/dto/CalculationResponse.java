package com.lilyan.tarifa_agua_api.controller.dto;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;

import java.math.BigDecimal;
import java.util.List;

public record CalculationResponse(
        ConsumerCategory categoria,
        Integer consumoTotal,
        BigDecimal valorTotal,
        List<RangeBreakdown> detalhamento
) {
    public record RangeBreakdown(Faixa faixa, Integer m3Cobrados, BigDecimal valorUnitario, BigDecimal subtotal) {}
    public record Faixa(Integer inicio, Integer fim) {}
}
