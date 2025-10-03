package com.ecommerce.analytics.service;

import com.ecommerce.analytics.model.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

/**
 * Servicio de análisis de datos con agregaciones de negocio
 *
 * Implementa operaciones avanzadas de Spark:
 * - Agregaciones (groupBy, agg)
 * - Funciones de agregación (sum, avg, count, max, min)
 * - Joins entre datasets
 * - Window functions para rankings
 * - Filtros dinámicos
 */
@Service
public class AnalyticsService {

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private DataReaderService dataReaderService;

    @Value("${spark.data.path}")
    private String dataPath;

    /**
     * 1. VENTAS POR CATEGORÍA
     *
     * Agrupa ventas por categoría de producto
     * Requiere JOIN entre transactions y products
     *
     * @return Lista de ventas agregadas por categoría
     */
    public List<SalesByCategory> getSalesByCategory() {
        // Leer datasets
        Dataset<Row> transactions = dataReaderService.readTransactions();
        Dataset<Row> products = dataReaderService.readProducts();

        // JOIN transactions con products para obtener categoría
        Dataset<Row> result = transactions
                .join(products, "product_id")
                .groupBy("category")
                .agg(
                        sum("amount").alias("totalSales"),
                        sum("quantity").alias("totalQuantity"),
                        avg("amount").alias("avgAmount"),
                        count("transaction_id").alias("transactionCount")
                )
                .orderBy(desc("totalSales"));

        // Convertir a List<SalesByCategory>
        return result.collectAsList().stream()
                .map(row -> {
                    SalesByCategory sales = new SalesByCategory();
                    sales.setCategory(row.getAs("category"));
                    sales.setTotalSales(row.getAs("totalSales"));
                    sales.setTotalQuantity(row.getAs("totalQuantity"));
                    sales.setAvgAmount(row.getAs("avgAmount"));
                    sales.setTransactionCount(row.getAs("transactionCount"));
                    return sales;
                })
                .toList();
    }

    /**
     * 2. TOP PRODUCTOS MÁS VENDIDOS
     *
     * Usa Window Functions para ranking
     *
     * @param limit Número de productos a retornar
     * @return Lista de top productos con ranking
     */
    public List<TopProduct> getTopProducts(int limit) {
        Dataset<Row> transactions = dataReaderService.readTransactions();
        Dataset<Row> products = dataReaderService.readProducts();

        // Agregar por producto
        Dataset<Row> productSales = transactions
                .join(products, "product_id")
                .groupBy("product_id", "product_name", "category")
                .agg(
                        sum("amount").alias("totalSales"),
                        sum("quantity").alias("quantity")
                )
                .orderBy(desc("totalSales"))
                .limit(limit);

        // Agregar ranking (Window Function)
        WindowSpec windowSpec = Window.orderBy(desc("totalSales"));
        Dataset<Row> result = productSales
                .withColumn("rank", row_number().over(windowSpec));

        // Convertir a List<TopProduct>
        return result.collectAsList().stream()
                .map(row -> {
                    TopProduct product = new TopProduct();
                    product.setProductId(row.getAs("product_id"));
                    product.setProductName(row.getAs("product_name"));
                    product.setCategory(row.getAs("category"));
                    product.setTotalSales(row.getAs("totalSales"));
                    product.setQuantity(row.getAs("quantity"));
                    product.setRank(row.getAs("rank"));
                    return product;
                })
                .toList();
    }

    /**
     * 3. ESTADÍSTICAS GENERALES
     *
     * Calcula métricas globales de ventas
     *
     * @return Map con estadísticas generales
     */
    public Map<String, Object> getStatistics() {
        Dataset<Row> transactions = dataReaderService.readTransactions();

        // Calcular estadísticas con una sola pasada
        Dataset<Row> stats = transactions.agg(
                sum("amount").alias("totalRevenue"),
                avg("amount").alias("avgTicket"),
                max("amount").alias("maxTransaction"),
                min("amount").alias("minTransaction"),
                count("transaction_id").alias("totalTransactions"),
                countDistinct("customer_id").alias("uniqueCustomers")
        );

        Row result = stats.first();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRevenue", result.getAs("totalRevenue"));
        statistics.put("avgTicket", result.getAs("avgTicket"));
        statistics.put("maxTransaction", result.getAs("maxTransaction"));
        statistics.put("minTransaction", result.getAs("minTransaction"));
        statistics.put("totalTransactions", result.getAs("totalTransactions"));
        statistics.put("uniqueCustomers", result.getAs("uniqueCustomers"));

        return statistics;
    }

    /**
     * 4. RESUMEN DIARIO DE VENTAS
     *
     * Agrupa ventas por día
     * Usa funciones de fecha (to_date, date_format)
     *
     * @param startDate Fecha inicio (formato: YYYY-MM-DD)
     * @param endDate Fecha fin (formato: YYYY-MM-DD)
     * @return Lista de resúmenes diarios
     */
    public List<DailySalesSummary> getDailySummary(String startDate, String endDate) {
        Dataset<Row> transactions = dataReaderService.readTransactions();

        // Filtrar por rango de fechas si se especifican
        if (startDate != null && endDate != null) {
            transactions = transactions.filter(
                    col("transaction_date").between(startDate, endDate)
            );
        }

        // Agrupar por fecha
        Dataset<Row> result = transactions
                .withColumn("date", to_date(col("transaction_date")))
                .groupBy("date")
                .agg(
                        sum("amount").alias("totalSales"),
                        count("transaction_id").alias("transactionCount"),
                        avg("amount").alias("avgTicket"),
                        countDistinct("customer_id").alias("uniqueCustomers")
                )
                .orderBy("date");

        // Convertir a List<DailySalesSummary>
        return result.collectAsList().stream()
                .map(row -> {
                    DailySalesSummary summary = new DailySalesSummary();
                    summary.setDate(row.getAs("date").toString());
                    summary.setTotalSales(row.getAs("totalSales"));
                    summary.setTransactionCount(row.getAs("transactionCount"));
                    summary.setAvgTicket(row.getAs("avgTicket"));
                    summary.setUniqueCustomers(row.getAs("uniqueCustomers"));
                    return summary;
                })
                .toList();
    }

    /**
     * 5. VENTAS POR REGIÓN
     *
     * Agrupa por región (campo de transactions)
     * Permite filtrado por fechas
     *
     * @param startDate Fecha inicio opcional
     * @param endDate Fecha fin opcional
     * @return Lista de ventas por región
     */
    public List<SalesByRegion> getSalesByRegion(String startDate, String endDate) {
        Dataset<Row> transactions = dataReaderService.readTransactions();

        // Filtrar por fechas si se especifican
        if (startDate != null && endDate != null) {
            transactions = transactions.filter(
                    col("transaction_date").between(startDate, endDate)
            );
        }

        // Agrupar por región
        Dataset<Row> result = transactions
                .groupBy("region")
                .agg(
                        sum("amount").alias("totalSales"),
                        countDistinct("customer_id").alias("customerCount"),
                        count("transaction_id").alias("transactionCount"),
                        avg("amount").alias("avgTicket")
                )
                .orderBy(desc("totalSales"));

        // Convertir a List<SalesByRegion>
        return result.collectAsList().stream()
                .map(row -> {
                    SalesByRegion region = new SalesByRegion();
                    region.setRegion(row.getAs("region"));
                    region.setTotalSales(row.getAs("totalSales"));
                    region.setCustomerCount(row.getAs("customerCount"));
                    region.setTransactionCount(row.getAs("transactionCount"));
                    region.setAvgTicket(row.getAs("avgTicket"));
                    return region;
                })
                .toList();
    }

    /**
     * 6. ANÁLISIS DE PRODUCTO ESPECÍFICO
     *
     * Obtiene métricas de un producto en particular
     *
     * @param productId ID del producto
     * @return Map con métricas del producto
     */
    public Map<String, Object> getProductAnalytics(String productId) {
        Dataset<Row> transactions = dataReaderService.readTransactions();
        Dataset<Row> products = dataReaderService.readProducts();

        // Filtrar por producto
        Dataset<Row> productTransactions = transactions
                .filter(col("product_id").equalTo(productId));

        // Calcular métricas
        Dataset<Row> metrics = productTransactions.agg(
                sum("amount").alias("totalRevenue"),
                sum("quantity").alias("totalQuantity"),
                count("transaction_id").alias("transactionCount"),
                avg("amount").alias("avgTicket"),
                countDistinct("customer_id").alias("uniqueCustomers")
        );

        // Obtener info del producto
        Dataset<Row> productInfo = products.filter(col("product_id").equalTo(productId));

        Map<String, Object> analytics = new HashMap<>();

        if (!productInfo.isEmpty()) {
            Row product = productInfo.first();
            analytics.put("productId", product.getAs("product_id"));
            analytics.put("productName", product.getAs("product_name"));
            analytics.put("category", product.getAs("category"));
            analytics.put("price", product.getAs("price"));
            analytics.put("stock", product.getAs("stock"));
        }

        if (!metrics.isEmpty()) {
            Row metric = metrics.first();
            analytics.put("totalRevenue", metric.getAs("totalRevenue"));
            analytics.put("totalQuantity", metric.getAs("totalQuantity"));
            analytics.put("transactionCount", metric.getAs("transactionCount"));
            analytics.put("avgTicket", metric.getAs("avgTicket"));
            analytics.put("uniqueCustomers", metric.getAs("uniqueCustomers"));
        }

        return analytics;
    }

    /**
     * 7. PRODUCTOS POR CATEGORÍA CON VENTAS
     *
     * Filtra productos por categoría y muestra sus ventas
     *
     * @param category Categoría a filtrar
     * @return Lista de productos con ventas
     */
    public List<Map<String, Object>> getProductsByCategory(String category) {
        Dataset<Row> transactions = dataReaderService.readTransactions();
        Dataset<Row> products = dataReaderService.readProducts();

        // Filtrar productos por categoría
        Dataset<Row> categoryProducts = products.filter(col("category").equalTo(category));

        // JOIN con transacciones para obtener ventas
        Dataset<Row> result = categoryProducts
                .join(transactions, "product_id")
                .groupBy("product_id", "product_name", "category", "price")
                .agg(
                        sum("amount").alias("totalSales"),
                        sum("quantity").alias("totalQuantity"),
                        count("transaction_id").alias("transactionCount")
                )
                .orderBy(desc("totalSales"));

        // Convertir a List<Map>
        return result.collectAsList().stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", row.getAs("product_id"));
                    map.put("productName", row.getAs("product_name"));
                    map.put("category", row.getAs("category"));
                    map.put("price", row.getAs("price"));
                    map.put("totalSales", row.getAs("totalSales"));
                    map.put("totalQuantity", row.getAs("totalQuantity"));
                    map.put("transactionCount", row.getAs("transactionCount"));
                    return map;
                })
                .toList();
    }
}
