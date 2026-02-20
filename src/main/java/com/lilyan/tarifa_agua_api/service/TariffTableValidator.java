package com.lilyan.tarifa_agua_api.service;

import com.lilyan.tarifa_agua_api.controller.dto.CategoryRangesRequest;
import com.lilyan.tarifa_agua_api.controller.dto.CreateTariffTableRequest;
import com.lilyan.tarifa_agua_api.controller.dto.RangeRequest;
import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
class TariffTableValidator {

    private static final int COBERTURA_MINIMA = 99999;

    void validate(CreateTariffTableRequest req) {
        validarVigencia(req);
        validarCategorias(req.categories());
        validarFaixasPorCategoria(req.categories());
    }

    private void validarVigencia(CreateTariffTableRequest req) {
        if (req.validFrom() != null && req.validTo() != null &&
                req.validFrom().isAfter(req.validTo())) {
            throw erro("A data inicial de vigência deve ser menor ou igual a data final.");
        }
    }

    private void validarCategorias(List<CategoryRangesRequest> categorias) {
        Set<ConsumerCategory> set = new HashSet<>();

        for (CategoryRangesRequest c : categorias) {
            if (!set.add(c.category())) {
                throw erro("Categoria " + c.category() + " repetida na requisiçao.");
            }
        }

        // requisito: tabela completa com as 4 categorias
        for (ConsumerCategory obrigatoria : ConsumerCategory.values()) {
            if (!set.contains(obrigatoria)) {
                throw erro("A tabela tarifária deve conter a categoria: " + obrigatoria);
            }
        }
    }

    private void validarFaixasPorCategoria(List<CategoryRangesRequest> categorias) {
        for (CategoryRangesRequest c : categorias) {
            validarFaixas(c.category(), c.ranges());
        }
    }

    private void validarFaixas(ConsumerCategory categoria, List<RangeRequest> faixas) {
        if (faixas == null || faixas.isEmpty()) {
            throw erro("As faixas de consumo não podem estar vazias para a categoria: " + categoria);
        }

        List<RangeRequest> sortedRanges = new ArrayList<>(faixas);
        sortedRanges.sort(Comparator.comparingInt(RangeRequest::start));

        if (!Objects.equals(sortedRanges.get(0).start(), 0)) {
            throw erro("A primeira faixa deve iniciar em 0 m³ para a categoria: " + categoria);
        }

        int nextStart = 0;

        for (RangeRequest f : sortedRanges) {
            Integer start = f.start();
            Integer end = f.end();

            if (start == null || end == null) {
                throw erro("Faixa invalida informada para a categoria: " + categoria);
            }

            if (start < 0 || start >= end) {
                throw erro("Intervalo invalido [" + start + "-" + end + "] para a categoria: " + categoria);
            }

            if (!Objects.equals(start, nextStart)) {
                throw erro("As faixas devem ser continuas para a categoria " + categoria +
                        " (esperado início " + nextStart + ", informado " + start + ").");
            }

            if (f.unitPrice() == null || f.unitPrice().signum() < 0) {
                throw erro("O valor unitário deve ser maior ou igual a zero para a categoria: " + categoria);
            }

            nextStart = end + 1;
        }

        int maiorFim = sortedRanges.get(sortedRanges.size() - 1).end();
        if (maiorFim < COBERTURA_MINIMA) {
            throw erro("A última faixa deve cobrir até pelo menos " + COBERTURA_MINIMA +
                    " m³ para a categoria: " + categoria);
        }
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
