package com.lilyan.tarifa_agua_api.controller.dto;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CategoryRangesRequest(
        @NotNull ConsumerCategory category,
        @Valid @NotEmpty List<RangeRequest> ranges
) {}
