package com.lilyan.tarifa_agua_api.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TariffTableResponse(
        Long id,
        String name,
        LocalDate validFrom,
        LocalDate validTo,
        List<Category> categories
) {
    public record Category(String category, List<Range> ranges) {}
    public record Range(Integer start, Integer end, BigDecimal unitPrice) {}
}
