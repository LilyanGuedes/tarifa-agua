package com.lilyan.tarifa_agua_api.controller.dto;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CategoryRangesRequest {

    @NotNull
    private ConsumerCategory category;

    @Valid
    @NotEmpty
    private List<RangeRequest> ranges;

    public ConsumerCategory getCategory() { return category; }
    public void setCategory(ConsumerCategory category) { this.category = category; }

    public List<RangeRequest> getRanges() { return ranges; }
    public void setRanges(List<RangeRequest> ranges) { this.ranges = ranges; }
}
