package com.lilyan.tarifa_agua_api.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "consumption_range")
@EntityListeners(AuditingEntityListener.class)
public class ConsumptionRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "range_start", nullable = false)
    private Integer rangeStart;

    @Column(name = "range_end", nullable = false)
    private Integer rangeEnd;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_category_id", nullable = false)
    private TariffCategory tariffCategory;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ConsumptionRange() {
        // hibernate precisa dele pra funfar
    }

    ConsumptionRange(TariffCategory tariffCategory, int start, int end, BigDecimal unitPrice) {
        this.tariffCategory = Objects.requireNonNull(tariffCategory, "tariffCategory cannot be null");
        this.rangeStart = start;
        this.rangeEnd = end;
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice cannot be null");
    }


    void detachFromCategory() {
        this.tariffCategory = null;
    }

    public Long getId() { return id; }
    public Integer getRangeStart() { return rangeStart; }
    public Integer getRangeEnd() { return rangeEnd; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public TariffCategory getTariffCategory() { return tariffCategory; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsumptionRange other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
