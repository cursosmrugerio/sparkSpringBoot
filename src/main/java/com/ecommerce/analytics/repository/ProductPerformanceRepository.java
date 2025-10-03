package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.entity.ProductPerformanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para métricas de rendimiento de productos
 */
@Repository
public interface ProductPerformanceRepository extends JpaRepository<ProductPerformanceEntity, Long> {

    // Buscar por producto
    List<ProductPerformanceEntity> findByProductId(String productId);

    // Buscar por categoría
    List<ProductPerformanceEntity> findByCategory(String category);

    // Top productos por revenue
    @Query("SELECT p FROM ProductPerformanceEntity p ORDER BY p.totalRevenue DESC")
    List<ProductPerformanceEntity> findTopByRevenue();

    // Top N productos
    @Query("SELECT p FROM ProductPerformanceEntity p ORDER BY p.rankOverall ASC")
    List<ProductPerformanceEntity> findTopNProducts();

    // Productos de una categoría ordenados por rank
    @Query("SELECT p FROM ProductPerformanceEntity p WHERE p.category = :category " +
           "ORDER BY p.rankInCategory ASC")
    List<ProductPerformanceEntity> findByCategoryOrderByRank(@Param("category") String category);

    // Último análisis de un producto
    @Query("SELECT p FROM ProductPerformanceEntity p WHERE p.productId = :productId " +
           "ORDER BY p.analysisDate DESC")
    Optional<ProductPerformanceEntity> findLatestByProductId(@Param("productId") String productId);

    // Productos analizados en una fecha específica
    List<ProductPerformanceEntity> findByAnalysisDateBetween(LocalDateTime start, LocalDateTime end);
}
