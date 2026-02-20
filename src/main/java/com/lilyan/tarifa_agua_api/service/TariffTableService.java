package com.lilyan.tarifa_agua_api.service;

import com.lilyan.tarifa_agua_api.controller.dto.CategoryRangesRequest;
import com.lilyan.tarifa_agua_api.controller.dto.CreateTariffTableRequest;
import com.lilyan.tarifa_agua_api.controller.dto.RangeRequest;
import com.lilyan.tarifa_agua_api.domain.model.TariffCategory;
import com.lilyan.tarifa_agua_api.domain.model.TariffTable;
import com.lilyan.tarifa_agua_api.repository.TariffTableRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TariffTableService {

    private final TariffTableRepository tariffTableRepository;
    private final TariffTableValidator validator;

    public TariffTableService(TariffTableRepository tariffTableRepository, TariffTableValidator validator) {
        this.tariffTableRepository = tariffTableRepository;
        this.validator = validator;
    }

    @Transactional
    public TariffTable create(CreateTariffTableRequest request) {
        validator.validate(request);

        TariffTable table = new TariffTable(
                request.name(),
                request.validFrom(),
                request.validTo()
        );

        for (CategoryRangesRequest c : request.categories()) {
            TariffCategory category = table.addCategory(c.category());

            List<RangeRequest> sortedRanges = new ArrayList<>(c.ranges());
            sortedRanges.sort(Comparator.comparingInt(RangeRequest::start));

            for (RangeRequest r : sortedRanges) {
                category.addRange(r.start(), r.end(), r.unitPrice());
            }
        }

        return tariffTableRepository.save(table);
    }

    @Transactional(readOnly = true)
    public List<TariffTable> findAll() {
        return tariffTableRepository.findAllByDeletedAtIsNull();
    }

    @Transactional
    public void delete(Long id) {
        TariffTable table = tariffTableRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Tabela não encontrada."));

        table.markAsDeleted();
    }


}
