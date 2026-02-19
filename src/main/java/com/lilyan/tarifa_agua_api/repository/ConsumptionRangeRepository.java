package com.lilyan.tarifa_agua_api.repository;

import com.lilyan.tarifa_agua_api.domain.model.ConsumptionRange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumptionRangeRepository extends JpaRepository<ConsumptionRange, Long> {
}
