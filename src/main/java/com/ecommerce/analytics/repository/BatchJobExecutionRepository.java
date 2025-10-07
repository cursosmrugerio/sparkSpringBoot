package com.ecommerce.analytics.repository;

import com.ecommerce.analytics.entity.BatchJobExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para batch_job_executions
 */
@Repository
public interface BatchJobExecutionRepository extends JpaRepository<BatchJobExecutionEntity, Long> {

    /**
     * Encuentra las últimas N ejecuciones ordenadas por fecha
     */
    List<BatchJobExecutionEntity> findTop10ByOrderByStartTimeDesc();

    /**
     * Encuentra ejecuciones por nombre de job
     */
    List<BatchJobExecutionEntity> findByJobNameOrderByStartTimeDesc(String jobName);

    /**
     * Encuentra ejecuciones por estado
     */
    List<BatchJobExecutionEntity> findByStatusOrderByStartTimeDesc(String status);

    /**
     * Encuentra la última ejecución exitosa de un job
     */
    Optional<BatchJobExecutionEntity> findFirstByJobNameAndStatusOrderByEndTimeDesc(String jobName, String status);

    /**
     * Encuentra ejecuciones en un rango de fechas
     */
    List<BatchJobExecutionEntity> findByStartTimeBetweenOrderByStartTimeDesc(
            LocalDateTime start, LocalDateTime end);

    /**
     * Cuenta ejecuciones por estado
     */
    @Query("SELECT e.status, COUNT(e) FROM BatchJobExecutionEntity e GROUP BY e.status")
    List<Object[]> countByStatus();

    /**
     * Obtiene métricas agregadas de ejecuciones
     */
    @Query("SELECT AVG(e.durationMs), SUM(e.recordsProcessed), COUNT(e) " +
           "FROM BatchJobExecutionEntity e WHERE e.jobName = ?1 AND e.status = 'SUCCESS'")
    List<Object[]> getJobMetrics(String jobName);
}
