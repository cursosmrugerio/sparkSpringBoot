package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.entity.SalesReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio JPA para reportes de ventas
 */
@Repository
public interface SalesReportRepository extends JpaRepository<SalesReportEntity, Long> {

    // Buscar por fecha de reporte
    List<SalesReportEntity> findByReportDate(LocalDate reportDate);

    // Buscar por rango de fechas
    List<SalesReportEntity> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    // Buscar por categoría
    List<SalesReportEntity> findByCategory(String category);

    // Buscar por región
    List<SalesReportEntity> findByRegion(String region);

    // Top reportes por ventas totales
    @Query("SELECT s FROM SalesReportEntity s ORDER BY s.totalSales DESC")
    List<SalesReportEntity> findTopByTotalSales();

    // Reportes de una categoría en un rango de fechas
    @Query("SELECT s FROM SalesReportEntity s WHERE s.category = :category " +
           "AND s.reportDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.reportDate DESC")
    List<SalesReportEntity> findByCategoryAndDateRange(
        @Param("category") String category,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
