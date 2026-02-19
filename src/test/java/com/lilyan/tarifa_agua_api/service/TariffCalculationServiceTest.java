package com.lilyan.tarifa_agua_api.service;

import com.lilyan.tarifa_agua_api.controller.dto.CalculationResponse;
import com.lilyan.tarifa_agua_api.controller.dto.CreateCalculationRequest;
import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import com.lilyan.tarifa_agua_api.domain.model.TariffCategory;
import com.lilyan.tarifa_agua_api.domain.model.TariffTable;
import com.lilyan.tarifa_agua_api.repository.TariffTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffCalculationServiceTest {

    @Mock
    private TariffTableRepository tariffTableRepository;

    @InjectMocks
    private TariffCalculationService service;

    @Test
    void deveCalcularExemploDoDesafio() {
        // cenario do desafio: Industrial, 18m³, faixas 0-10@R$1 e 11-20@R$2
        TariffTable tabela = buildTabela(ConsumerCategory.INDUSTRIAL,
                new int[]{0, 10}, new BigDecimal("1.00"),
                new int[]{11, 20}, new BigDecimal("2.00")
        );
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of(tabela));

        CalculationResponse resp = service.calculate(request(ConsumerCategory.INDUSTRIAL, 18));

        assertEquals(ConsumerCategory.INDUSTRIAL, resp.getCategoria());
        assertEquals(18, resp.getConsumoTotal());
        assertEquals(0, new BigDecimal("26.00").compareTo(resp.getValorTotal()));

        assertEquals(2, resp.getDetalhamento().size());

        CalculationResponse.RangeBreakdown faixa1 = resp.getDetalhamento().get(0);
        assertEquals(10, faixa1.getM3Cobrados());
        assertEquals(0, new BigDecimal("10.00").compareTo(faixa1.getSubtotal()));

        CalculationResponse.RangeBreakdown faixa2 = resp.getDetalhamento().get(1);
        assertEquals(8, faixa2.getM3Cobrados());
        assertEquals(0, new BigDecimal("16.00").compareTo(faixa2.getSubtotal()));
    }

    @Test
    void deveCalcularConsumoEmUmaFaixaSo() {
        TariffTable tabela = buildTabela(ConsumerCategory.PARTICULAR,
                new int[]{0, 10}, new BigDecimal("3.00"),
                new int[]{11, 99999}, new BigDecimal("5.00")
        );
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of(tabela));

        CalculationResponse resp = service.calculate(request(ConsumerCategory.PARTICULAR, 7));

        assertEquals(0, new BigDecimal("21.00").compareTo(resp.getValorTotal()));
        assertEquals(1, resp.getDetalhamento().size());
        assertEquals(7, resp.getDetalhamento().get(0).getM3Cobrados());
    }

    @Test
    void deveCalcularConsumoExatoNaFaixa() {
        TariffTable tabela = buildTabela(ConsumerCategory.COMERCIAL,
                new int[]{0, 10}, new BigDecimal("2.00"),
                new int[]{11, 20}, new BigDecimal("4.00")
        );
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of(tabela));

        CalculationResponse resp = service.calculate(request(ConsumerCategory.COMERCIAL, 10));

        assertEquals(0, new BigDecimal("20.00").compareTo(resp.getValorTotal()));
        assertEquals(1, resp.getDetalhamento().size());
    }

    @Test
    void deveCalcularConsumoComTresFaixas() {
        TariffTable tabela = buildTabela(ConsumerCategory.PUBLICO,
                new int[]{0, 10}, new BigDecimal("1.00"),
                new int[]{11, 20}, new BigDecimal("2.00"),
                new int[]{21, 30}, new BigDecimal("3.00")
        );
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of(tabela));

        // consumo 25: 10x1 + 10x2 + 5x3 = 10 + 20 + 15 = 45
        CalculationResponse resp = service.calculate(request(ConsumerCategory.PUBLICO, 25));

        assertEquals(0, new BigDecimal("45.00").compareTo(resp.getValorTotal()));
        assertEquals(3, resp.getDetalhamento().size());
    }

    @Test
    void deveLancarErroSemTabelaVigente() {
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.calculate(request(ConsumerCategory.INDUSTRIAL, 10)));

        assertTrue(ex.getReason().contains("tabela tarifária vigente"));
    }

    @Test
    void deveLancarErroCategoriaNaoEncontrada() {
        // tabela só com comercial
        TariffTable tabela = buildTabela(ConsumerCategory.COMERCIAL,
                new int[]{0, 10}, new BigDecimal("1.00")
        );
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of(tabela));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.calculate(request(ConsumerCategory.INDUSTRIAL, 10)));

        assertTrue(ex.getReason().contains("categoria informada"));
    }

    @Test
    void deveLancarErroConsumoExcedeCobertura() {
        TariffTable tabela = buildTabela(ConsumerCategory.INDUSTRIAL,
                new int[]{0, 10}, new BigDecimal("1.00")
        );
        when(tariffTableRepository.findValidTables(any(LocalDate.class)))
                .thenReturn(List.of(tabela));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.calculate(request(ConsumerCategory.INDUSTRIAL, 15)));

        assertTrue(ex.getReason().contains("não cobrem"));
    }

    // métodos utilizados nos testes

    private CreateCalculationRequest request(ConsumerCategory cat, int consumo) {
        CreateCalculationRequest req = new CreateCalculationRequest();
        req.setCategoria(cat);
        req.setConsumo(consumo);
        return req;
    }


    private TariffTable buildTabela(ConsumerCategory categoria, Object... faixas) {
        TariffTable tabela = new TariffTable("Teste", null, null);
        TariffCategory cat = tabela.addCategory(categoria);

        for (int i = 0; i < faixas.length; i += 2) {
            int[] range = (int[]) faixas[i];
            BigDecimal price = (BigDecimal) faixas[i + 1];
            cat.addRange(range[0], range[1], price);
        }

        return tabela;
    }
}
