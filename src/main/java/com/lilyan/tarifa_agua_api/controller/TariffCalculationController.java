package com.lilyan.tarifa_agua_api.controller;

import com.lilyan.tarifa_agua_api.controller.dto.CalculationResponse;
import com.lilyan.tarifa_agua_api.controller.dto.CreateCalculationRequest;
import com.lilyan.tarifa_agua_api.service.TariffCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculos")
public class TariffCalculationController {

    private final TariffCalculationService service;

    public TariffCalculationController(TariffCalculationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public CalculationResponse calculate(@RequestBody @Valid CreateCalculationRequest request) {
        return service.calculate(request);
    }
}
