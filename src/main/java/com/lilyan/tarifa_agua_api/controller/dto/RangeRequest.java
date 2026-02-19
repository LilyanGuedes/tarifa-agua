package com.lilyan.tarifa_agua_api.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RangeRequest {

    @NotNull
    @Min(0)
    private Integer start;

    @NotNull
    @Min(0)
    private Integer end;

    @NotNull
    private BigDecimal unitPrice;

    public Integer getStart() { return start; }
    public void setStart(Integer start) { this.start = start; }

    public Integer getEnd() { return end; }
    public void setEnd(Integer end) { this.end = end; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
