package com.lilyan.tarifa_agua_api.controller.dto;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;

import java.math.BigDecimal;
import java.util.List;

public class CalculationResponse {

    private ConsumerCategory categoria;
    private Integer consumoTotal;
    private BigDecimal valorTotal;
    private List<RangeBreakdown> detalhamento;

    public CalculationResponse(
            ConsumerCategory categoria,
            Integer consumoTotal,
            BigDecimal valorTotal,
            List<RangeBreakdown> detalhamento
    ) {
        this.categoria = categoria;
        this.consumoTotal = consumoTotal;
        this.valorTotal = valorTotal;
        this.detalhamento = detalhamento;
    }

    public ConsumerCategory getCategoria() { return categoria; }
    public Integer getConsumoTotal() { return consumoTotal; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public List<RangeBreakdown> getDetalhamento() { return detalhamento; }

    public static class RangeBreakdown {
        private Faixa faixa;
        private Integer m3Cobrados;
        private BigDecimal valorUnitario;
        private BigDecimal subtotal;

        public RangeBreakdown(Faixa faixa, Integer m3Cobrados, BigDecimal valorUnitario, BigDecimal subtotal) {
            this.faixa = faixa;
            this.m3Cobrados = m3Cobrados;
            this.valorUnitario = valorUnitario;
            this.subtotal = subtotal;
        }

        public Faixa getFaixa() { return faixa; }
        public Integer getM3Cobrados() { return m3Cobrados; }
        public BigDecimal getValorUnitario() { return valorUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    public static class Faixa {
        private Integer inicio;
        private Integer fim;

        public Faixa(Integer inicio, Integer fim) {
            this.inicio = inicio;
            this.fim = fim;
        }

        public Integer getInicio() { return inicio; }
        public Integer getFim() { return fim; }
    }
}
