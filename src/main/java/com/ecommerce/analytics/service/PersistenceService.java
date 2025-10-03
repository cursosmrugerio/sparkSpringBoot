package com.ecommerce.analytics.service;

import com.ecommerce.analytics.entity.FraudAlertEntity;
import com.ecommerce.analytics.entity.ProductPerformanceEntity;
import com.ecommerce.analytics.entity.SalesReportEntity;
import com.ecommerce.analytics.repository.FraudAlertRepository;
import com.ecommerce.analytics.repository.ProductPerformanceRepository;
import com.ecommerce.analytics.repository.SalesReportRepository;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de persistencia bidireccional con PostgreSQL
 *
 * Implementa:
 * - Escritura de DataFrames de Spark a PostgreSQL (JDBC)
 * - Lectura desde PostgreSQL usando Spring Data JPA
 * - Conversión entre DataFrames y Entidades JPA
 */
@Service
public class PersistenceService {

    @Autowired
    private SalesReportRepository salesReportRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private ProductPerformanceRepository productPerformanceRepository;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * Guarda DataFrame de Spark directamente en PostgreSQL usando JDBC
     */
    public void saveDataFrameToPostgres(Dataset<Row> df, String tableName, SaveMode saveMode) {
        Map<String, String> jdbcOptions = new HashMap<>();
        jdbcOptions.put("url", jdbcUrl);
        jdbcOptions.put("dbtable", tableName);
        jdbcOptions.put("user", dbUser);
        jdbcOptions.put("password", dbPassword);
        jdbcOptions.put("driver", "org.postgresql.Driver");

        df.write()
            .format("jdbc")
            .options(jdbcOptions)
            .mode(saveMode)
            .save();

        System.out.println("✅ DataFrame guardado en PostgreSQL: " + tableName);
    }

    /**
     * Guarda reporte de ventas en PostgreSQL
     */
    @Transactional
    public void saveSalesReport(LocalDate reportDate, String category, String region,
                                 Double totalSales, Long totalQuantity, Long transactionCount,
                                 Double avgTicket, Long uniqueCustomers) {
        SalesReportEntity report = new SalesReportEntity();
        report.setReportDate(reportDate);
        report.setCategory(category);
        report.setRegion(region);
        report.setTotalSales(totalSales);
        report.setTotalQuantity(totalQuantity);
        report.setTransactionCount(transactionCount);
        report.setAvgTicket(avgTicket);
        report.setUniqueCustomers(uniqueCustomers);

        salesReportRepository.save(report);
    }

    /**
     * Guarda múltiples reportes de ventas desde un DataFrame
     */
    @Transactional
    public int saveSalesReportsFromDataFrame(Dataset<Row> salesData, LocalDate reportDate) {
        List<Row> rows = salesData.collectAsList();

        List<SalesReportEntity> reports = rows.stream()
            .map(row -> {
                SalesReportEntity report = new SalesReportEntity();
                report.setReportDate(reportDate);
                report.setCategory(row.getAs("category"));
                report.setRegion(row.getAs("region"));
                report.setTotalSales(row.getAs("totalSales"));
                report.setTotalQuantity(row.getAs("totalQuantity"));
                report.setTransactionCount(row.getAs("transactionCount"));
                report.setAvgTicket(row.getAs("avgTicket"));
                report.setUniqueCustomers(row.getAs("uniqueCustomers"));
                return report;
            })
            .collect(Collectors.toList());

        salesReportRepository.saveAll(reports);
        System.out.println("✅ Guardados " + reports.size() + " reportes de ventas");
        return reports.size();
    }

    /**
     * Guarda alerta de fraude en PostgreSQL
     */
    @Transactional
    public FraudAlertEntity saveFraudAlert(String transactionId, String customerId, String productId,
                                           Double amount, Integer quantity, String riskLevel,
                                           String amountCategory, String transactionDate, String region,
                                           Double deviationFromMean, Boolean isOutlier) {
        FraudAlertEntity alert = new FraudAlertEntity();
        alert.setTransactionId(transactionId);
        alert.setCustomerId(customerId);
        alert.setProductId(productId);
        alert.setAmount(amount);
        alert.setQuantity(quantity != null ? quantity.longValue() : null);
        alert.setRiskLevel(riskLevel);
        alert.setAmountCategory(amountCategory);
        alert.setTransactionDate(transactionDate);
        alert.setRegion(region);
        alert.setDeviationFromMean(deviationFromMean);
        alert.setIsOutlier(isOutlier);

        return fraudAlertRepository.save(alert);
    }

    /**
     * Guarda múltiples alertas de fraude desde un DataFrame
     */
    @Transactional
    public int saveFraudAlertsFromDataFrame(Dataset<Row> fraudData) {
        List<Row> rows = fraudData.collectAsList();

        List<FraudAlertEntity> alerts = rows.stream()
            .map(row -> {
                FraudAlertEntity alert = new FraudAlertEntity();
                alert.setTransactionId(row.getAs("transaction_id"));
                alert.setCustomerId(row.getAs("customer_id"));
                alert.setProductId(row.getAs("product_id"));
                alert.setAmount(row.getAs("amount"));
                Integer quantity = row.getAs("quantity");
                alert.setQuantity(quantity != null ? quantity.longValue() : null);
                alert.setRiskLevel(row.getAs("fraud_risk"));
                alert.setAmountCategory(row.getAs("amount_category"));
                java.sql.Timestamp timestamp = row.getAs("transaction_date");
                alert.setTransactionDate(timestamp != null ? timestamp.toString() : null);
                alert.setRegion(row.getAs("region"));
                alert.setDeviationFromMean(row.getAs("deviation"));
                alert.setIsOutlier(row.getAs("is_outlier"));
                return alert;
            })
            .collect(Collectors.toList());

        fraudAlertRepository.saveAll(alerts);
        System.out.println("✅ Guardadas " + alerts.size() + " alertas de fraude");
        return alerts.size();
    }

    /**
     * Guarda métricas de rendimiento de productos
     */
    @Transactional
    public int saveProductPerformanceFromDataFrame(Dataset<Row> productData) {
        List<Row> rows = productData.collectAsList();

        List<ProductPerformanceEntity> performances = rows.stream()
            .map(row -> {
                ProductPerformanceEntity perf = new ProductPerformanceEntity();
                perf.setProductId(row.getAs("product_id"));
                perf.setProductName(row.getAs("product_name"));
                perf.setCategory(row.getAs("category"));
                perf.setTotalRevenue(row.getAs("totalSales"));
                perf.setTotalQuantity(row.getAs("quantity"));
                perf.setTransactionCount(row.getAs("transactionCount"));
                perf.setAvgTicket(row.getAs("avgTicket"));
                perf.setUniqueCustomers(row.getAs("uniqueCustomers"));
                perf.setRankOverall(row.getAs("rank"));
                perf.setAnalysisDate(LocalDateTime.now());
                return perf;
            })
            .collect(Collectors.toList());

        productPerformanceRepository.saveAll(performances);
        System.out.println("✅ Guardadas " + performances.size() + " métricas de productos");
        return performances.size();
    }

    /**
     * Lee reportes de ventas desde PostgreSQL
     */
    public List<SalesReportEntity> getSalesReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesReportRepository.findByReportDateBetween(startDate, endDate);
    }

    /**
     * Lee alertas de fraude no revisadas
     */
    public List<FraudAlertEntity> getUnreviewedFraudAlerts() {
        return fraudAlertRepository.findByReviewed(false);
    }

    /**
     * Lee alertas de alto riesgo
     */
    public List<FraudAlertEntity> getHighRiskAlerts() {
        return fraudAlertRepository.findHighRiskUnreviewed();
    }

    /**
     * Marca alerta como revisada
     */
    @Transactional
    public void markAlertAsReviewed(Long alertId) {
        fraudAlertRepository.findById(alertId).ifPresent(alert -> {
            alert.setReviewed(true);
            fraudAlertRepository.save(alert);
        });
    }

    /**
     * Obtiene top productos por revenue
     */
    public List<ProductPerformanceEntity> getTopProductsByRevenue(int limit) {
        return productPerformanceRepository.findTopByRevenue()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene métricas de un producto
     */
    public List<ProductPerformanceEntity> getProductPerformance(String productId) {
        return productPerformanceRepository.findByProductId(productId);
    }

    /**
     * Estadísticas de la base de datos
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSalesReports", salesReportRepository.count());
        stats.put("totalFraudAlerts", fraudAlertRepository.count());
        stats.put("unreviewedAlerts", fraudAlertRepository.findByReviewed(false).size());
        stats.put("highRiskAlerts", fraudAlertRepository.findByRiskLevel("ALTO_RIESGO").size());
        stats.put("totalProductPerformance", productPerformanceRepository.count());
        return stats;
    }
}
