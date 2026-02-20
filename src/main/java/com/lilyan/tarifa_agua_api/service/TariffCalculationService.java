package com.lilyan.tarifa_agua_api.service;

import com.lilyan.tarifa_agua_api.controller.dto.CalculationResponse;
import com.lilyan.tarifa_agua_api.controller.dto.CreateCalculationRequest;
import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import com.lilyan.tarifa_agua_api.domain.model.ConsumptionRange;
import com.lilyan.tarifa_agua_api.domain.model.TariffCategory;
import com.lilyan.tarifa_agua_api.domain.model.TariffTable;
import com.lilyan.tarifa_agua_api.repository.TariffTableRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TariffCalculationService {

    private final TariffTableRepository tariffTableRepository;

    public TariffCalculationService(TariffTableRepository tariffTableRepository) {
        this.tariffTableRepository = tariffTableRepository;
    }

    @Transactional(readOnly = true)
    public CalculationResponse calculate(CreateCalculationRequest request) {
        ConsumerCategory categoria = request.categoria();
        int consumoTotal = request.consumo();

        TariffTable tabelaVigente = findCurrentTable();

        TariffCategory cat = tabelaVigente.findCategory(categoria)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatusCode.valueOf(422),
                        "A categoria informada não existe na tabela tarifária vigente."
                ));

        List<ConsumptionRange> faixas = new ArrayList<>(cat.getRanges());
        faixas.sort(Comparator.comparingInt(ConsumptionRange::getRangeStart));

        ensureCoverage(faixas, consumoTotal);

        BigDecimal total = BigDecimal.ZERO;
        List<CalculationResponse.RangeBreakdown> detalhamento = new ArrayList<>();

        int restante = consumoTotal;

        for (ConsumptionRange faixa : faixas) {
            if (restante <= 0) break;

            int capacidade = faixa.getRangeStart() == 0
                    ? faixa.getRangeEnd()
                    : faixa.getRangeEnd() - faixa.getRangeStart() + 1;

            int m3Cobrados = Math.min(restante, capacidade);

            BigDecimal subtotal = faixa.getUnitPrice().multiply(BigDecimal.valueOf(m3Cobrados));
            total = total.add(subtotal);

            detalhamento.add(new CalculationResponse.RangeBreakdown(
                    new CalculationResponse.Faixa(faixa.getRangeStart(), faixa.getRangeEnd()),
                    m3Cobrados,
                    faixa.getUnitPrice(),
                    subtotal
            ));

            restante -= m3Cobrados;
        }

        return new CalculationResponse(categoria, consumoTotal, total, detalhamento);
    }

    private TariffTable findCurrentTable() {
        List<TariffTable> valid = tariffTableRepository.findValidTables(LocalDate.now());
        if (valid.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatusCode.valueOf(422),
                    "Não existe tabela tarifária vigente para a data atual."
            );
        }
        return valid.get(0);
    }

    private void ensureCoverage(List<ConsumptionRange> faixas, int consumoTotal) {
        if (faixas.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "A categoria informada não possui faixas cadastradas.");
        }

        int maiorFim = faixas.stream()
                .mapToInt(ConsumptionRange::getRangeEnd)
                .max()
                .orElse(0);

        if (consumoTotal > maiorFim) {
            throw new ResponseStatusException(
                    HttpStatusCode.valueOf(422),
                    "As faixas cadastradas não cobrem o consumo informado."
            );
        }

    }
}
