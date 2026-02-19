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
        CreateTariffTableRequest req = buildRequestCompleta();
        req.setValidFrom(LocalDate.of(2025, 6, 1));
        req.setValidTo(LocalDate.of(2025, 1, 1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("data inicial"));
    }


    @Test
    void deveRejeitarFaixasVazias() {
        CreateTariffTableRequest req = buildRequestCompleta();
        req.getCategories().get(0).setRanges(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("vazias"));
    }

    @Test
    void deveRejeitarFaixaQueNaoComecaEmZero() {
        CreateTariffTableRequest req = buildRequestCompleta();
        req.getCategories().get(0).setRanges(List.of(
                buildFaixa(5, 20, "3.00")
        ));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("iniciar em 0"));
    }

    @Test
    void deveRejeitarFaixasComBuraco() {
        CreateTariffTableRequest req = buildRequestCompleta();
        req.getCategories().get(0).setRanges(List.of(
                buildFaixa(0, 10, "1.00"),
                buildFaixa(15, 30, "2.00") // buraco entre 11 e 14
        ));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("contínuas"));
    }

    @Test
    void deveRejeitarIntervaloInvalido() {
        CreateTariffTableRequest req = buildRequestCompleta();
        req.getCategories().get(0).setRanges(List.of(
                buildFaixa(0, 0, "1.00")
        ));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("[0-0]"));
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        CreateTariffTableRequest req = buildRequestCompleta();
        req.getCategories().get(0).setRanges(List.of(
                buildFaixa(0, 10, "-1.00")
        ));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validate(req));

        assertTrue(ex.getReason().contains("valor unitário"));
    }

// métodos utilizados nos testes

    private CreateTariffTableRequest buildRequestCompleta() {
        CreateTariffTableRequest req = new CreateTariffTableRequest();
        req.setName("Tabela Teste");
        req.setValidFrom(LocalDate.of(2025, 1, 1));
        req.setValidTo(LocalDate.of(2025, 12, 31));

        List<CategoryRangesRequest> categorias = new ArrayList<>();
        for (ConsumerCategory cat : ConsumerCategory.values()) {
            categorias.add(buildCategoria(cat));
        }
        req.setCategories(categorias);
        return req;
    }

    private CategoryRangesRequest buildCategoria(ConsumerCategory cat) {
        CategoryRangesRequest c = new CategoryRangesRequest();
        c.setCategory(cat);
        c.setRanges(List.of(
                buildFaixa(0, 10, "2.50"),
                buildFaixa(11, 20, "4.00"),
                buildFaixa(21, 99999, "6.00")
        ));
        return c;
    }

    private RangeRequest buildFaixa(int start, int end, String preco) {
        RangeRequest r = new RangeRequest();
        r.setStart(start);
        r.setEnd(end);
        r.setUnitPrice(new BigDecimal(preco));
        return r;
    }
}
