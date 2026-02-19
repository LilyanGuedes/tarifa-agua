package com.lilyan.tarifa_agua_api.domain.model;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
        name = "tariff_category",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tariff_table_id", "category"})
)
@EntityListeners(AuditingEntityListener.class)
public class TariffCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ConsumerCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_table_id", nullable = false)
    private TariffTable tariffTable;

    @OneToMany(mappedBy = "tariffCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ConsumptionRange> ranges = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TariffCategory() {
    }

    TariffCategory(TariffTable tariffTable, ConsumerCategory category) {
        this.tariffTable = Objects.requireNonNull(tariffTable, "tariffTable cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");
    }

    public ConsumptionRange addRange(int start, int end, java.math.BigDecimal unitPrice) {
        ConsumptionRange range = new ConsumptionRange(this, start, end, unitPrice);
        this.ranges.add(range);
        return range;
    }

    public void removeRange(Long rangeId) {
        Iterator<ConsumptionRange> it = ranges.iterator();
        while (it.hasNext()) {
            ConsumptionRange r = it.next();
            if (Objects.equals(r.getId(), rangeId)) {
                r.detachFromCategory();
                it.remove();
                return;
            }
        }
    }


    public List<ConsumptionRange> getRanges() {
        return Collections.unmodifiableList(ranges);
    }


    public ConsumerCategory getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TariffCategory other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
