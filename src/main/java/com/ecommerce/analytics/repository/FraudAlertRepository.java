package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.entity.FraudAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para alertas de fraude
 */
@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlertEntity, Long> {

    // Buscar por nivel de riesgo
    List<FraudAlertEntity> findByRiskLevel(String riskLevel);

    // Buscar alertas no revisadas
    List<FraudAlertEntity> findByReviewed(Boolean reviewed);

    // Buscar por cliente
    List<FraudAlertEntity> findByCustomerId(String customerId);

    // Buscar outliers
    List<FraudAlertEntity> findByIsOutlier(Boolean isOutlier);

    // Alertas de alto riesgo no revisadas
    @Query("SELECT f FROM FraudAlertEntity f WHERE f.riskLevel = 'ALTO_RIESGO' AND f.reviewed = false " +
           "ORDER BY f.createdAt DESC")
    List<FraudAlertEntity> findHighRiskUnreviewed();

    // Contar alertas por nivel de riesgo
    @Query("SELECT f.riskLevel, COUNT(f) FROM FraudAlertEntity f " +
           "WHERE f.reviewed = false GROUP BY f.riskLevel")
    List<Object[]> countAlertsByRiskLevel();

    // Top clientes con más alertas
    @Query("SELECT f.customerId, COUNT(f) as alertCount FROM FraudAlertEntity f " +
           "GROUP BY f.customerId ORDER BY alertCount DESC")
    List<Object[]> findTopCustomersWithAlerts();
}
