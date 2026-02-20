package com.lilyan.tarifa_agua_api.controller;

import com.lilyan.tarifa_agua_api.controller.dto.CreateTariffTableRequest;
import com.lilyan.tarifa_agua_api.controller.dto.TariffTableResponse;
import com.lilyan.tarifa_agua_api.domain.model.TariffTable;
import com.lilyan.tarifa_agua_api.service.TariffTableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tabelas-tarifarias")
public class TariffTableController {

    private final TariffTableService tariffTableService;

    public TariffTableController(TariffTableService tariffTableService) {
        this.tariffTableService = tariffTableService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTariffTableResponse create(@RequestBody @Valid CreateTariffTableRequest request) {
        TariffTable created = tariffTableService.create(request);
        return new CreateTariffTableResponse(created.getId(), created.getName());
    }

    public record CreateTariffTableResponse(Long id, String name) {}

    @GetMapping
    public List<TariffTableResponse> list() {
        return tariffTableService.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tariffTableService.delete(id);
    }

    private TariffTableResponse toResponse(TariffTable table) {
        List<TariffTableResponse.Category> categories = table.getCategories().stream()
                .map(cat -> new TariffTableResponse.Category(
                        cat.getCategory().name(),
                        cat.getRanges().stream()
                                .map(r -> new TariffTableResponse.Range(
                                        r.getRangeStart(),
                                        r.getRangeEnd(),
                                        r.getUnitPrice()
                                ))
                                .toList()
                ))
                .toList();

        return new TariffTableResponse(
                table.getId(),
                table.getName(),
                table.getValidFrom(),
                table.getValidTo(),
                categories
        );
    }
}
