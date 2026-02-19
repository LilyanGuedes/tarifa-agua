package com.lilyan.tarifa_agua_api.repository;

import com.lilyan.tarifa_agua_api.domain.model.TariffTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffTableRepository extends JpaRepository<TariffTable, Long> {

    List<TariffTable> findAllByDeletedAtIsNull();

    Optional<TariffTable> findByIdAndDeletedAtIsNull(Long id);

    //buscar a tabela vigente, ignorando as deletadas
    @Query("""
        select t
        from TariffTable t
        where t.deletedAt is null
          and t.status = com.lilyan.tarifa_agua_api.domain.enums.TariffStatus.ACTIVE
          and (t.validFrom is null or t.validFrom <= :today)
          and (t.validTo is null or t.validTo >= :today)
        order by t.validFrom desc nulls last, t.createdAt desc
    """)
    List<TariffTable> findValidTables(LocalDate today);

}
