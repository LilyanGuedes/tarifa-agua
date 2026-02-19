package com.lilyan.tarifa_agua_api.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TariffTableResponse {

    public Long id;
    public String name;
    public LocalDate validFrom;
    public LocalDate validTo;
    public List<Category> categories;

    public static class Category {
        public String category;
        public List<Range> ranges;
    }

    public static class Range {
        public Integer start;
        public Integer end;
        public BigDecimal unitPrice;
    }
}
