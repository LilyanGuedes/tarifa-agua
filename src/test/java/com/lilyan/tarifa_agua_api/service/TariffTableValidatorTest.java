package com.lilyan.tarifa_agua_api.service;

import com.lilyan.tarifa_agua_api.controller.dto.CategoryRangesRequest;
import com.lilyan.tarifa_agua_api.controller.dto.CreateTariffTableRequest;
import com.lilyan.tarifa_agua_api.controller.dto.RangeRequest;
import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TariffTableValidatorTest {

    private final TariffTableValidator validator = new TariffTableValidator();

    @Test
    void deveAceitarRequestValida() {
        CreateTariffTableRequest req = buildRequestCompleta();
        assertDoesNotThrow(() -> validator.validate(req));
    }

    @Test
    void deveRejeitarVigenciaInvalida() {
        CreateTariffTableRequest req = buildRequest(
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 1, 1),
                buildTodasCategorias()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("data inicial"));
    }


    @Test
    void deveRejeitarFaixasVazias() {
        List<CategoryRangesRequest> categorias = buildTodasCategorias();
        categorias.set(0, new CategoryRangesRequest(categorias.get(0).category(), List.of()));

        CreateTariffTableRequest req = buildRequest(categorias);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("vazias"));
    }

    @Test
    void deveRejeitarFaixaQueNaoComecaEmZero() {
        List<CategoryRangesRequest> categorias = buildTodasCategorias();
        categorias.set(0, new CategoryRangesRequest(categorias.get(0).category(), List.of(
                new RangeRequest(5, 20, new BigDecimal("3.00"))
        )));

        CreateTariffTableRequest req = buildRequest(categorias);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("iniciar em 0"));
    }

    @Test
    void deveRejeitarFaixasComBuraco() {
        List<CategoryRangesRequest> categorias = buildTodasCategorias();
        categorias.set(0, new CategoryRangesRequest(categorias.get(0).category(), List.of(
                new RangeRequest(0, 10, new BigDecimal("1.00")),
                new RangeRequest(15, 30, new BigDecimal("2.00")) // buraco entre 11 e 14
        )));

        CreateTariffTableRequest req = buildRequest(categorias);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("continuas"));
    }

    @Test
    void deveRejeitarIntervaloInvalido() {
        List<CategoryRangesRequest> categorias = buildTodasCategorias();
        categorias.set(0, new CategoryRangesRequest(categorias.get(0).category(), List.of(
                new RangeRequest(0, 0, new BigDecimal("1.00"))
        )));

        CreateTariffTableRequest req = buildRequest(categorias);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("[0-0]"));
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        List<CategoryRangesRequest> categorias = buildTodasCategorias();
        categorias.set(0, new CategoryRangesRequest(categorias.get(0).category(), List.of(
                new RangeRequest(0, 10, new BigDecimal("-1.00"))
        )));

        CreateTariffTableRequest req = buildRequest(categorias);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("valor unitário"));
    }

    @Test
    void deveRejeitarFaixasSemCoberturaSuficiente() {
        List<CategoryRangesRequest> categorias = buildTodasCategorias();
        categorias.set(0, new CategoryRangesRequest(categorias.get(0).category(), List.of(
                new RangeRequest(0, 10, new BigDecimal("1.00")),
                new RangeRequest(11, 100, new BigDecimal("2.00"))
        )));

        CreateTariffTableRequest req = buildRequest(categorias);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("99999"));
    }

// métodos utilizados nos testes

    private CreateTariffTableRequest buildRequestCompleta() {
        return buildRequest(buildTodasCategorias());
    }

    private CreateTariffTableRequest buildRequest(List<CategoryRangesRequest> categorias) {
        return new CreateTariffTableRequest(
                "Tabela Teste",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                categorias
        );
    }

    private CreateTariffTableRequest buildRequest(LocalDate validFrom, LocalDate validTo, List<CategoryRangesRequest> categorias) {
        return new CreateTariffTableRequest("Tabela Teste", validFrom, validTo, categorias);
    }

    private List<CategoryRangesRequest> buildTodasCategorias() {
        List<CategoryRangesRequest> categorias = new ArrayList<>();
        for (ConsumerCategory cat : ConsumerCategory.values()) {
            categorias.add(new CategoryRangesRequest(cat, List.of(
                    new RangeRequest(0, 10, new BigDecimal("2.50")),
                    new RangeRequest(11, 20, new BigDecimal("4.00")),
                    new RangeRequest(21, 99999, new BigDecimal("6.00"))
            )));
        }
        return categorias;
    }
}
