package com.ecommerce.analytics.service;

import com.ecommerce.analytics.entity.DailyReportEntity;
import com.ecommerce.analytics.repository.DailyReportRepository;
import com.ecommerce.analytics.repository.FraudAlertRepository;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.apache.spark.sql.functions.*;

/**
 * Servicio para generación de reportes automáticos
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private DataReaderService dataReaderService;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    /**
     * Genera reporte diario automático
     */
    @Transactional
    public DailyReportEntity generateDailyReport(LocalDate reportDate) {
        logger.info("📊 Generando reporte diario para: {}", reportDate);

        // Verificar si ya existe
        if (dailyReportRepository.existsByReportDate(reportDate)) {
            logger.warn("⚠️ Reporte ya existe para {}", reportDate);
            return dailyReportRepository.findByReportDate(reportDate).orElse(null);
        }

        Dataset<Row> transactions = dataReaderService.readTransactions();
        Dataset<Row> products = dataReaderService.readProducts();

        // Filtrar transacciones del día
        Dataset<Row> dayTransactions = transactions.filter(
            to_date(col("transaction_date")).equalTo(lit(reportDate.toString()))
        );

        // Calcular métricas
        Row stats = dayTransactions.agg(
            sum("amount").alias("total_sales"),
            count("transaction_id").alias("total_transactions"),
            countDistinct("customer_id").alias("unique_customers"),
            avg("amount").alias("avg_ticket")
        ).first();

        // Extraer métricas con valores por defecto
        Double totalSales = stats.isNullAt(0) ? 0.0 : stats.getDouble(0);
        Long totalTransactions = stats.isNullAt(1) ? 0L : stats.getLong(1);
        Long uniqueCustomers = stats.isNullAt(2) ? 0L : stats.getLong(2);
        Double avgTicket = stats.isNullAt(3) ? 0.0 : stats.getDouble(3);

        // Top categoría y producto
        Dataset<Row> withCategory = dayTransactions.join(products, "product_id");

        String topCategory = "N/A";
        String topProduct = "N/A";

        if (withCategory.count() > 0) {
            Row categoryRow = withCategory.groupBy("category")
                .count()
                .orderBy(desc("count"))
                .first();
            if (categoryRow != null && !categoryRow.isNullAt(0)) {
                topCategory = categoryRow.getString(0);
            }

            Row productRow = withCategory.groupBy("product_name")
                .count()
                .orderBy(desc("count"))
                .first();
            if (productRow != null && !productRow.isNullAt(0)) {
                topProduct = productRow.getString(0);
            }
        }

        // Contar alertas de fraude del día
        long fraudCount = fraudAlertRepository.count();

        // Crear entidad
        DailyReportEntity report = new DailyReportEntity();
        report.setReportDate(reportDate);
        report.setTotalSales(totalSales);
        report.setTotalTransactions(totalTransactions);
        report.setUniqueCustomers(uniqueCustomers);
        report.setAvgTicket(avgTicket);
        report.setTopCategory(topCategory);
        report.setTopProduct(topProduct);
        report.setFraudAlertsCount(fraudCount);

        DailyReportEntity saved = dailyReportRepository.save(report);
        logger.info("✅ Reporte diario generado: Total Sales = ${}", saved.getTotalSales());

        return saved;
    }
}
