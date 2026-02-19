package com.lilyan.tarifa_agua_api.repository;

import com.lilyan.tarifa_agua_api.domain.model.TariffCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TariffCategoryRepository extends JpaRepository<TariffCategory, Long> {
}