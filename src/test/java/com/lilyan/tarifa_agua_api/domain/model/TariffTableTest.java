package com.lilyan.tarifa_agua_api.domain.model;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TariffTableTest {

    @Test
    void deveAdicionarCategoria() {
        TariffTable tabela = new TariffTable("Teste", null, null);

        TariffCategory cat = tabela.addCategory(ConsumerCategory.COMERCIAL);

        assertNotNull(cat);
        assertEquals(1, tabela.getCategories().size());
        assertEquals(ConsumerCategory.COMERCIAL, cat.getCategory());
    }

    @Test
    void deveBuscarCategoriaExistente() {
        TariffTable tabela = new TariffTable("Teste", null, null);
        tabela.addCategory(ConsumerCategory.INDUSTRIAL);

        Optional<TariffCategory> encontrada = tabela.findCategory(ConsumerCategory.INDUSTRIAL);

        assertTrue(encontrada.isPresent());
        assertEquals(ConsumerCategory.INDUSTRIAL, encontrada.get().getCategory());
    }

    @Test
    void deveRetornarVazioParaCategoriaNaoExistente() {
        TariffTable tabela = new TariffTable("Teste", null, null);
        tabela.addCategory(ConsumerCategory.COMERCIAL);

        Optional<TariffCategory> resultado = tabela.findCategory(ConsumerCategory.PUBLICO);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveMarcarComoDeletada() {
        TariffTable tabela = new TariffTable("Teste", null, null);

        tabela.markAsDeleted();

        assertThrows(IllegalStateException.class, tabela::activate);
        assertThrows(IllegalStateException.class, tabela::inactivate);
    }

    @Test
    void deveImpedirNomeNulo() {
        assertThrows(NullPointerException.class,
                () -> new TariffTable(null, null, null));
    }

    @Test
    void deveRetornarListaImutavel() {
        TariffTable tabela = new TariffTable("Teste", null, null);
        tabela.addCategory(ConsumerCategory.COMERCIAL);

        assertThrows(UnsupportedOperationException.class,
                () -> tabela.getCategories().add(null));
    }

    @Test
    void deveAdicionarFaixaNaCategoria() {
        TariffTable tabela = new TariffTable("Teste", null, null);
        TariffCategory cat = tabela.addCategory(ConsumerCategory.PARTICULAR);

        cat.addRange(0, 10, new BigDecimal("2.50"));
        cat.addRange(11, 20, new BigDecimal("4.00"));

        assertEquals(2, cat.getRanges().size());
        assertEquals(0, cat.getRanges().get(0).getRangeStart());
        assertEquals(10, cat.getRanges().get(0).getRangeEnd());
    }

}
