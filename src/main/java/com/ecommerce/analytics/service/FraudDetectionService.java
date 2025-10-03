package com.ecommerce.analytics.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

/**
 * Servicio de detección de fraude y anomalías
 *
 * Implementa:
 * - Detección de outliers usando desviación estándar
 * - Análisis de patrones sospechosos
 * - Categorización de riesgo con UDFs
 * - Identificación de transacciones anómalas
 */
@Service
public class FraudDetectionService {

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private OptimizationService optimizationService;

    @Autowired
    private PersistenceService persistenceService;

    /**
     * Detecta transacciones sospechosas usando múltiples criterios
     *
     * Criterios de detección:
     * 1. Monto > 3 desviaciones estándar de la media
     * 2. Cantidad excesiva (> 10 unidades)
     * 3. Monto muy alto (> umbral definido)
     */
    public Dataset<Row> detectSuspiciousTransactions(double stdDevThreshold) {
        // Registrar UDFs si no están registradas
        optimizationService.registerUDFs();

        Dataset<Row> transactions = optimizationService.getTransactions();

        // Calcular estadísticas
        Row stats = transactions.agg(
            avg("amount").alias("mean"),
            stddev("amount").alias("stddev")
        ).first();

        double mean = stats.getDouble(0);
        double stddev = stats.getDouble(1);

        System.out.println("📊 Estadísticas de montos:");
        System.out.println("  - Media: " + mean);
        System.out.println("  - Desviación estándar: " + stddev);

        // Detectar outliers (> N desviaciones estándar)
        double upperBound = mean + (stdDevThreshold * stddev);
        double lowerBound = mean - (stdDevThreshold * stddev);

        System.out.println("  - Límite superior (outlier): " + upperBound);
        System.out.println("  - Límite inferior (outlier): " + lowerBound);

        // Aplicar detección de fraude
        Dataset<Row> suspicious = transactions
            // Calcular desviación de la media
            .withColumn("deviation", abs(col("amount").minus(lit(mean))).divide(lit(stddev)))
            // Marcar outliers
            .withColumn("is_outlier",
                col("amount").gt(lit(upperBound)).or(col("amount").lt(lit(lowerBound))))
            // Aplicar UDFs
            .withColumn("amount_category", expr("categorize_amount(amount)"))
            .withColumn("fraud_risk", expr("detect_fraud(amount, quantity)"))
            // Filtrar solo transacciones sospechosas
            .filter(
                col("is_outlier").equalTo(true)
                .or(col("fraud_risk").equalTo("ALTO_RIESGO"))
                .or(col("fraud_risk").equalTo("MEDIO_RIESGO"))
            )
            .orderBy(desc("deviation"));

        long suspiciousCount = suspicious.count();
        System.out.println("⚠️ Transacciones sospechosas detectadas: " + suspiciousCount);

        return suspicious;
    }

    /**
     * Detecta y guarda alertas de fraude en PostgreSQL
     */
    public int detectAndSaveFraudAlerts(double stdDevThreshold) {
        Dataset<Row> suspicious = detectSuspiciousTransactions(stdDevThreshold);

        // Guardar en PostgreSQL
        int savedCount = persistenceService.saveFraudAlertsFromDataFrame(suspicious);

        System.out.println("✅ Alertas guardadas en PostgreSQL: " + savedCount);

        return savedCount;
    }

    /**
     * Analiza patrones de fraude por cliente
     */
    public List<Map<String, Object>> analyzeCustomerFraudPatterns(double stdDevThreshold) {
        Dataset<Row> suspicious = detectSuspiciousTransactions(stdDevThreshold);

        // Agrupar por cliente
        Dataset<Row> customerPatterns = suspicious
            .groupBy("customer_id")
            .agg(
                count("transaction_id").alias("suspicious_count"),
                sum("amount").alias("total_suspicious_amount"),
                avg("amount").alias("avg_suspicious_amount"),
                max("deviation").alias("max_deviation"),
                collect_list("fraud_risk").alias("risk_levels")
            )
            .orderBy(desc("suspicious_count"));

        // Convertir a List<Map>
        return customerPatterns.collectAsList().stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("customerId", row.getAs("customer_id"));
                map.put("suspiciousCount", row.getAs("suspicious_count"));
                map.put("totalSuspiciousAmount", row.getAs("total_suspicious_amount"));
                map.put("avgSuspiciousAmount", row.getAs("avg_suspicious_amount"));
                map.put("maxDeviation", row.getAs("max_deviation"));
                map.put("riskLevels", row.getAs("risk_levels"));
                return map;
            })
            .collect(Collectors.toList());
    }

    /**
     * Analiza patrones de fraude por producto
     */
    public List<Map<String, Object>> analyzeProductFraudPatterns(double stdDevThreshold) {
        Dataset<Row> suspicious = detectSuspiciousTransactions(stdDevThreshold);
        Dataset<Row> products = optimizationService.getProducts();

        // JOIN con productos para obtener información adicional
        Dataset<Row> productPatterns = suspicious
            .join(products, "product_id")
            .groupBy("product_id", "product_name", "category")
            .agg(
                count("transaction_id").alias("fraud_count"),
                sum("amount").alias("total_fraud_amount"),
                avg("deviation").alias("avg_deviation")
            )
            .orderBy(desc("fraud_count"));

        // Convertir a List<Map>
        return productPatterns.collectAsList().stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", row.getAs("product_id"));
                map.put("productName", row.getAs("product_name"));
                map.put("category", row.getAs("category"));
                map.put("fraudCount", row.getAs("fraud_count"));
                map.put("totalFraudAmount", row.getAs("total_fraud_amount"));
                map.put("avgDeviation", row.getAs("avg_deviation"));
                return map;
            })
            .collect(Collectors.toList());
    }

    /**
     * Estadísticas generales de fraude
     */
    public Map<String, Object> getFraudStatistics(double stdDevThreshold) {
        Dataset<Row> transactions = optimizationService.getTransactions();
        Dataset<Row> suspicious = detectSuspiciousTransactions(stdDevThreshold);

        long totalTransactions = transactions.count();
        long suspiciousCount = suspicious.count();

        // Contar por nivel de riesgo
        Map<String, Long> riskCounts = suspicious
            .groupBy("fraud_risk")
            .count()
            .collectAsList()
            .stream()
            .collect(Collectors.toMap(
                row -> row.getString(0),
                row -> row.getLong(1)
            ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", totalTransactions);
        stats.put("suspiciousTransactions", suspiciousCount);
        stats.put("fraudRate", (double) suspiciousCount / totalTransactions * 100);
        stats.put("riskLevelCounts", riskCounts);

        // Monto total sospechoso
        Row amountStats = suspicious.agg(
            sum("amount").alias("total"),
            avg("amount").alias("avg")
        ).first();

        if (amountStats.get(0) != null) {
            stats.put("totalSuspiciousAmount", amountStats.getDouble(0));
            stats.put("avgSuspiciousAmount", amountStats.getDouble(1));
        }

        return stats;
    }

    /**
     * Detecta transacciones duplicadas sospechosas
     * (mismo cliente, mismo producto, mismo monto, en corto periodo)
     */
    public Dataset<Row> detectDuplicateSuspiciousTransactions() {
        Dataset<Row> transactions = optimizationService.getTransactions();

        // Buscar duplicados por cliente, producto y monto
        Dataset<Row> duplicates = transactions
            .groupBy("customer_id", "product_id", "amount")
            .agg(
                count("transaction_id").alias("occurrence_count"),
                collect_list("transaction_id").alias("transaction_ids"),
                collect_list("transaction_date").alias("dates")
            )
            .filter(col("occurrence_count").gt(1))
            .orderBy(desc("occurrence_count"));

        long duplicateCount = duplicates.count();
        System.out.println("🔄 Grupos de transacciones duplicadas sospechosas: " + duplicateCount);

        return duplicates;
    }
}
