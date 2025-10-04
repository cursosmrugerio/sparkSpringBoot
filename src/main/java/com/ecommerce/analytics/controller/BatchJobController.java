package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.entity.BatchJobExecutionEntity;
import com.ecommerce.analytics.entity.DailyReportEntity;
import com.ecommerce.analytics.repository.BatchJobExecutionRepository;
import com.ecommerce.analytics.repository.DailyReportRepository;
import com.ecommerce.analytics.service.BatchJobService;
import com.ecommerce.analytics.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para monitoreo y gestión de batch jobs
 */
@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    @Autowired
    private BatchJobService batchJobService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private BatchJobExecutionRepository jobExecutionRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    /**
     * Ejecuta ETL pipeline manualmente
     */
    @PostMapping("/etl/run")
    public ResponseEntity<Map<String, Object>> runETL() {
        BatchJobExecutionEntity execution = batchJobService.runETLPipeline("manual");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "ETL Pipeline ejecutado");
        response.put("execution", execution);

        return ResponseEntity.ok(response);
    }

    /**
     * Ejecuta procesamiento incremental manualmente
     */
    @PostMapping("/incremental/run")
    public ResponseEntity<Map<String, Object>> runIncremental(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        if (since == null) {
            since = LocalDateTime.now().minusHours(1);
        }

        BatchJobExecutionEntity execution = batchJobService.processIncrementalData(since);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Procesamiento incremental ejecutado");
        response.put("since", since);
        response.put("execution", execution);

        return ResponseEntity.ok(response);
    }

    /**
     * Genera reporte diario manualmente
     */
    @PostMapping("/report/generate")
    public ResponseEntity<DailyReportEntity> generateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {

        DailyReportEntity report = reportService.generateDailyReport(reportDate);
        return ResponseEntity.ok(report);
    }

    /**
     * Obtiene historial de ejecuciones de jobs
     */
    @GetMapping("/executions")
    public ResponseEntity<List<BatchJobExecutionEntity>> getExecutions(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String status) {

        List<BatchJobExecutionEntity> executions;

        if (jobName != null) {
            executions = jobExecutionRepository.findByJobNameOrderByStartTimeDesc(jobName);
        } else if (status != null) {
            executions = jobExecutionRepository.findByStatusOrderByStartTimeDesc(status);
        } else {
            executions = jobExecutionRepository.findTop10ByOrderByStartTimeDesc();
        }

        return ResponseEntity.ok(executions);
    }

    /**
     * Obtiene dashboard de métricas de jobs
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Últimas ejecuciones
        List<BatchJobExecutionEntity> recentExecutions =
                jobExecutionRepository.findTop10ByOrderByStartTimeDesc();
        dashboard.put("recentExecutions", recentExecutions);

        // Conteo por estado
        List<Object[]> statusCounts = jobExecutionRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put((String) row[0], (Long) row[1]);
        }
        dashboard.put("executionsByStatus", statusMap);

        // Reportes recientes
        List<DailyReportEntity> recentReports =
                dailyReportRepository.findTop30ByOrderByReportDateDesc();
        dashboard.put("recentReports", recentReports);

        // Estadísticas generales
        long totalJobs = jobExecutionRepository.count();
        long totalReports = dailyReportRepository.count();
        dashboard.put("totalJobs", totalJobs);
        dashboard.put("totalReports", totalReports);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene métricas de un job específico
     */
    @GetMapping("/metrics/{jobName}")
    public ResponseEntity<Map<String, Object>> getJobMetrics(@PathVariable String jobName) {
        Object[] metrics = jobExecutionRepository.getJobMetrics(jobName);

        Map<String, Object> result = new HashMap<>();
        if (metrics != null && metrics.length >= 3) {
            result.put("jobName", jobName);
            result.put("avgDurationMs", metrics[0]);
            result.put("totalRecordsProcessed", metrics[1]);
            result.put("successfulExecutions", metrics[2]);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Obtiene reportes diarios en un rango
     */
    @GetMapping("/reports")
    public ResponseEntity<List<DailyReportEntity>> getReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyReportEntity> reports =
                dailyReportRepository.findByReportDateBetweenOrderByReportDateDesc(startDate, endDate);

        return ResponseEntity.ok(reports);
    }
}
