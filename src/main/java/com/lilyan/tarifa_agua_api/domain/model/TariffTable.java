package com.lilyan.tarifa_agua_api.domain.model;

import com.lilyan.tarifa_agua_api.domain.enums.ConsumerCategory;
import com.lilyan.tarifa_agua_api.domain.enums.TariffStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tariff_table")
@EntityListeners(AuditingEntityListener.class)
public class TariffTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TariffStatus status = TariffStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tariffTable", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<TariffCategory> categories = new ArrayList<>();

    protected TariffTable() {
    }

    public TariffTable(String name, LocalDate validFrom, LocalDate validTo) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.status = TariffStatus.ACTIVE;
    }


    public void activate() {
        ensureNotDeleted();
        this.status = TariffStatus.ACTIVE;
    }

    public void inactivate() {
        ensureNotDeleted();
        this.status = TariffStatus.INACTIVE;
    }

    public void markAsDeleted() {
        this.status = TariffStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public TariffCategory addCategory(ConsumerCategory category) {
        Objects.requireNonNull(category, "category cannot be null");

        TariffCategory created = new TariffCategory(this, category);
        this.categories.add(created);
        return created;
    }

    public Optional<TariffCategory> findCategory(ConsumerCategory category) {
        return categories.stream()
                .filter(c -> c.getCategory() == category)
                .findFirst();
    }

    public List<TariffCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    private void ensureNotDeleted() {
        if (this.status == TariffStatus.DELETED) {
            throw new IllegalStateException("Tariff table is deleted");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TariffTable other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}