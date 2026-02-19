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

    void validate(CreateTariffTableRequest req) {
        validarVigencia(req);
        validarCategorias(req.getCategories());
        validarFaixasPorCategoria(req.getCategories());
    }

    private void validarVigencia(CreateTariffTableRequest req) {
        if (req.getValidFrom() != null && req.getValidTo() != null &&
                req.getValidFrom().isAfter(req.getValidTo())) {
            throw erro("A data inicial de vigência deve ser menor ou igual a data final.");
        }
    }

    private void validarCategorias(List<CategoryRangesRequest> categorias) {
        Set<ConsumerCategory> set = new HashSet<>();

        for (CategoryRangesRequest c : categorias) {
            if (!set.add(c.getCategory())) {
                throw erro("Categoria " + c.getCategory() + " repetida na requisiçao.");
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
            validarFaixas(c.getCategory(), c.getRanges());
        }
    }

    private void validarFaixas(ConsumerCategory categoria, List<RangeRequest> faixas) {
        if (faixas == null || faixas.isEmpty()) {
            throw erro("As faixas de consumo não podem estar vazias para a categoria: " + categoria);
        }

        List<RangeRequest> sortedRanges = new ArrayList<>(faixas);
        sortedRanges.sort(Comparator.comparingInt(RangeRequest::getStart));

        if (!Objects.equals(sortedRanges.get(0).getStart(), 0)) {
            throw erro("A primeira faixa deve iniciar em 0 m³ para a categoria: " + categoria);
        }

        int nextStart = 0;

        for (RangeRequest f : sortedRanges) {
            Integer start = f.getStart();
            Integer end = f.getEnd();

            if (start == null || end == null) {
                throw erro("Faixa inválida informada para a categoria: " + categoria);
            }

            if (start < 0 || start >= end) {
                throw erro("Intervalo inválido [" + start + "-" + end + "] para a categoria: " + categoria);
            }

            if (!Objects.equals(start, nextStart)) {
                throw erro("As faixas devem ser contínuas para a categoria " + categoria +
                        " (esperado início " + nextStart + ", informado " + start + ").");
            }

            if (f.getUnitPrice() == null || f.getUnitPrice().signum() < 0) {
                throw erro("O valor unitário deve ser maior ou igual a zero para a categoria: " + categoria);
            }

            nextStart = end + 1;
        }

        // verificar novamente sobre cobertura suficiente
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
