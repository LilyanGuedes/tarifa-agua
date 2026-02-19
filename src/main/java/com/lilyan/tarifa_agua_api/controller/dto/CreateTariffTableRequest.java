package com.lilyan.tarifa_agua_api.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class CreateTariffTableRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    private LocalDate validFrom;
    private LocalDate validTo;

    @Valid
    @NotEmpty
    private List<CategoryRangesRequest> categories;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public List<CategoryRangesRequest> getCategories() { return categories; }
    public void setCategories(List<CategoryRangesRequest> categories) { this.categories = categories; }
}
