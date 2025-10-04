package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.entity.DailyReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para daily_reports
 */
@Repository
public interface DailyReportRepository extends JpaRepository<DailyReportEntity, Long> {

    /**
     * Encuentra reporte por fecha
     */
    Optional<DailyReportEntity> findByReportDate(LocalDate reportDate);

    /**
     * Encuentra reportes en un rango de fechas
     */
    List<DailyReportEntity> findByReportDateBetweenOrderByReportDateDesc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Encuentra los últimos N reportes
     */
    List<DailyReportEntity> findTop30ByOrderByReportDateDesc();

    /**
     * Verifica si existe reporte para una fecha
     */
    boolean existsByReportDate(LocalDate reportDate);

    /**
     * Obtiene totales agregados de un período
     */
    @Query("SELECT SUM(r.totalSales), SUM(r.totalTransactions), AVG(r.avgTicket) " +
           "FROM DailyReportEntity r WHERE r.reportDate BETWEEN ?1 AND ?2")
    Object[] getPeriodTotals(LocalDate startDate, LocalDate endDate);
}
