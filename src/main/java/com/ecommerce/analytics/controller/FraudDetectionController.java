package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.entity.FraudAlertEntity;
import com.ecommerce.analytics.service.FraudDetectionService;
import com.ecommerce.analytics.service.PersistenceService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para detección de fraude y anomalías
 *
 * Endpoints:
 * - GET /api/fraud/detect - Detectar transacciones sospechosas
 * - POST /api/fraud/detect-and-save - Detectar y guardar alertas
 * - GET /api/fraud/statistics - Estadísticas de fraude
 * - GET /api/fraud/customer-patterns - Patrones de fraude por cliente
 * - GET /api/fraud/product-patterns - Patrones de fraude por producto
 * - GET /api/fraud/duplicates - Transacciones duplicadas sospechosas
 * - GET /api/fraud/alerts - Obtener alertas guardadas
 * - PUT /api/fraud/alerts/{id}/review - Marcar alerta como revisada
 */
@RestController
@RequestMapping("/api/fraud")
public class FraudDetectionController {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private PersistenceService persistenceService;

    /**
     * Detectar transacciones sospechosas
     */
    @GetMapping("/detect")
    public ResponseEntity<List<Map<String, Object>>> detectSuspiciousTransactions(
        @RequestParam(defaultValue = "3.0") double stdDevThreshold,
        @RequestParam(defaultValue = "20") int limit
    ) {
        Dataset<Row> suspicious = fraudDetectionService.detectSuspiciousTransactions(stdDevThreshold);

        List<Map<String, Object>> result = suspicious.limit(limit)
            .collectAsList()
            .stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("transactionId", row.getAs("transaction_id"));
                map.put("customerId", row.getAs("customer_id"));
                map.put("productId", row.getAs("product_id"));
                map.put("amount", row.getAs("amount"));
                map.put("quantity", row.getAs("quantity"));
                map.put("deviation", row.getAs("deviation"));
                map.put("isOutlier", row.getAs("is_outlier"));
                map.put("amountCategory", row.getAs("amount_category"));
                map.put("fraudRisk", row.getAs("fraud_risk"));
                map.put("transactionDate", row.getAs("transaction_date"));
                map.put("region", row.getAs("region"));
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Detectar y guardar alertas de fraude en PostgreSQL
     */
    @PostMapping("/detect-and-save")
    public ResponseEntity<Map<String, Object>> detectAndSaveAlerts(
        @RequestParam(defaultValue = "3.0") double stdDevThreshold
    ) {
        int savedCount = fraudDetectionService.detectAndSaveFraudAlerts(stdDevThreshold);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Alertas de fraude detectadas y guardadas");
        response.put("alertsSaved", savedCount);
        response.put("stdDevThreshold", stdDevThreshold);

        return ResponseEntity.ok(response);
    }

    /**
     * Estadísticas generales de fraude
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFraudStatistics(
        @RequestParam(defaultValue = "3.0") double stdDevThreshold
    ) {
        Map<String, Object> stats = fraudDetectionService.getFraudStatistics(stdDevThreshold);
        return ResponseEntity.ok(stats);
    }

    /**
     * Patrones de fraude por cliente
     */
    @GetMapping("/customer-patterns")
    public ResponseEntity<List<Map<String, Object>>> getCustomerFraudPatterns(
        @RequestParam(defaultValue = "3.0") double stdDevThreshold
    ) {
        List<Map<String, Object>> patterns = fraudDetectionService.analyzeCustomerFraudPatterns(stdDevThreshold);
        return ResponseEntity.ok(patterns);
    }

    /**
     * Patrones de fraude por producto
     */
    @GetMapping("/product-patterns")
    public ResponseEntity<List<Map<String, Object>>> getProductFraudPatterns(
        @RequestParam(defaultValue = "3.0") double stdDevThreshold
    ) {
        List<Map<String, Object>> patterns = fraudDetectionService.analyzeProductFraudPatterns(stdDevThreshold);
        return ResponseEntity.ok(patterns);
    }

    /**
     * Detectar transacciones duplicadas sospechosas
     */
    @GetMapping("/duplicates")
    public ResponseEntity<List<Map<String, Object>>> getDuplicateSuspiciousTransactions() {
        Dataset<Row> duplicates = fraudDetectionService.detectDuplicateSuspiciousTransactions();

        List<Map<String, Object>> result = duplicates.limit(10)
            .collectAsList()
            .stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("customerId", row.getAs("customer_id"));
                map.put("productId", row.getAs("product_id"));
                map.put("amount", row.getAs("amount"));
                map.put("occurrenceCount", row.getAs("occurrence_count"));
                map.put("transactionIds", row.getAs("transaction_ids"));
                map.put("dates", row.getAs("dates"));
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Obtener alertas guardadas en PostgreSQL
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<FraudAlertEntity>> getAlerts(
        @RequestParam(required = false) String riskLevel,
        @RequestParam(defaultValue = "false") boolean onlyUnreviewed
    ) {
        List<FraudAlertEntity> alerts;

        if (onlyUnreviewed) {
            alerts = persistenceService.getUnreviewedFraudAlerts();
        } else if (riskLevel != null) {
            alerts = persistenceService.getHighRiskAlerts();
        } else {
            alerts = persistenceService.getUnreviewedFraudAlerts();
        }

        return ResponseEntity.ok(alerts);
    }

    /**
     * Marcar alerta como revisada
     */
    @PutMapping("/alerts/{id}/review")
    public ResponseEntity<Map<String, String>> markAlertAsReviewed(@PathVariable Long id) {
        persistenceService.markAlertAsReviewed(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Alerta marcada como revisada");
        response.put("alertId", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener alertas de alto riesgo
     */
    @GetMapping("/alerts/high-risk")
    public ResponseEntity<List<FraudAlertEntity>> getHighRiskAlerts() {
        List<FraudAlertEntity> alerts = persistenceService.getHighRiskAlerts();
        return ResponseEntity.ok(alerts);
    }
}
