package com.lilyan.tarifa_agua_api.controller;

import com.lilyan.tarifa_agua_api.controller.dto.CreateTariffTableRequest;
import com.lilyan.tarifa_agua_api.controller.dto.TariffTableResponse;
import com.lilyan.tarifa_agua_api.domain.model.ConsumptionRange;
import com.lilyan.tarifa_agua_api.domain.model.TariffCategory;
import com.lilyan.tarifa_agua_api.domain.model.TariffTable;
import com.lilyan.tarifa_agua_api.service.TariffTableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

        List<TariffTable> tables = tariffTableService.findAll();
        List<TariffTableResponse> response = new ArrayList<>();

        for (TariffTable table : tables) {

            TariffTableResponse dto = new TariffTableResponse();
            dto.id = table.getId();
            dto.name = table.getName();
            dto.validFrom = table.getValidFrom();
            dto.validTo = table.getValidTo();

            dto.categories = new ArrayList<>();

            for (TariffCategory category : table.getCategories()) {

                TariffTableResponse.Category catDto = new TariffTableResponse.Category();
                catDto.category = category.getCategory().name();

                catDto.ranges = new ArrayList<>();

                for (ConsumptionRange range : category.getRanges()) {

                    TariffTableResponse.Range rangeDto = new TariffTableResponse.Range();
                    rangeDto.start = range.getRangeStart();
                    rangeDto.end = range.getRangeEnd();
                    rangeDto.unitPrice = range.getUnitPrice();

                    catDto.ranges.add(rangeDto);
                }

                dto.categories.add(catDto);
            }

            response.add(dto);
        }

        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tariffTableService.delete(id);
    }


}
