package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.service.OptimizationService;
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
 * Controlador REST para operaciones de optimización
 *
 * Endpoints:
 * - POST /api/optimization/cache - Cachear datasets principales
 * - DELETE /api/optimization/cache - Liberar caché
 * - GET /api/optimization/cache/info - Info del caché
 * - GET /api/optimization/transactions/enriched - Transacciones con UDFs aplicadas
 * - GET /api/optimization/transactions/clean - Transacciones limpias
 */
@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {

    @Autowired
    private OptimizationService optimizationService;

    /**
     * Cachear datasets principales en memoria
     */
    @PostMapping("/cache")
    public ResponseEntity<Map<String, Object>> cacheDatasets() {
        long startTime = System.currentTimeMillis();

        optimizationService.cacheMainDatasets();

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Datasets cacheados exitosamente");
        response.put("durationMs", duration);
        response.put("cacheInfo", optimizationService.getCacheInfo());

        return ResponseEntity.ok(response);
    }

    /**
     * Liberar caché de todos los datasets
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        optimizationService.unpersistAll();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Caché liberado exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Información del caché actual
     */
    @GetMapping("/cache/info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        return ResponseEntity.ok(optimizationService.getCacheInfo());
    }

    /**
     * Obtener transacciones con UDFs aplicadas (enriquecidas)
     */
    @GetMapping("/transactions/enriched")
    public ResponseEntity<List<Map<String, Object>>> getEnrichedTransactions(
        @RequestParam(defaultValue = "10") int limit
    ) {
        Dataset<Row> enriched = optimizationService.applyUDFsToTransactions();

        List<Map<String, Object>> result = enriched.limit(limit)
            .collectAsList()
            .stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("transactionId", row.getAs("transaction_id"));
                map.put("customerId", row.getAs("customer_id"));
                map.put("productId", row.getAs("product_id"));
                map.put("amount", row.getAs("amount"));
                map.put("quantity", row.getAs("quantity"));
                map.put("amountCategory", row.getAs("amount_category"));
                map.put("fraudRisk", row.getAs("fraud_risk"));
                map.put("discountPct", row.getAs("discount_pct"));
                map.put("amountWithDiscount", row.getAs("amount_with_discount"));
                map.put("transactionDate", row.getAs("transaction_date"));
                map.put("region", row.getAs("region"));
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Obtener transacciones limpias (sin nulls, duplicados, etc.)
     */
    @GetMapping("/transactions/clean")
    public ResponseEntity<Map<String, Object>> getCleanTransactions() {
        Dataset<Row> cleaned = optimizationService.cleanTransactions();

        long count = cleaned.count();

        List<Map<String, Object>> sample = cleaned.limit(10)
            .collectAsList()
            .stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                for (org.apache.spark.sql.types.StructField field : row.schema().fields()) {
                    map.put(field.name(), row.getAs(field.name()));
                }
                return map;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalCleanRecords", count);
        response.put("sample", sample);

        return ResponseEntity.ok(response);
    }

    /**
     * Realizar broadcast join optimizado
     */
    @GetMapping("/transactions/broadcast-join")
    public ResponseEntity<List<Map<String, Object>>> getBroadcastJoin(
        @RequestParam(defaultValue = "5") int limit
    ) {
        Dataset<Row> joined = optimizationService.broadcastJoinTransactionsWithProducts();

        List<Map<String, Object>> result = joined.limit(limit)
            .collectAsList()
            .stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("transactionId", row.getAs("transaction_id"));
                map.put("productId", row.getAs("product_id"));
                map.put("productName", row.getAs("product_name"));
                map.put("category", row.getAs("category"));
                map.put("amount", row.getAs("amount"));
                map.put("quantity", row.getAs("quantity"));
                map.put("price", row.getAs("price"));
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Registrar UDFs en SparkSession
     */
    @PostMapping("/udfs/register")
    public ResponseEntity<Map<String, String>> registerUDFs() {
        optimizationService.registerUDFs();

        Map<String, String> response = new HashMap<>();
        response.put("message", "UDFs registradas exitosamente");
        response.put("udfs", "validate_email, categorize_amount, detect_fraud, normalize_string, calculate_discount");

        return ResponseEntity.ok(response);
    }
}
