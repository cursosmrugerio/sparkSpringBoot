package com.ecommerce.analytics.service;

import com.ecommerce.analytics.udf.CustomUDFs;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.storage.StorageLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

/**
 * Servicio de optimización de Spark
 *
 * Implementa:
 * - Caché estratégico de DataFrames
 * - Particionamiento de datos
 * - Broadcast joins para tablas pequeñas
 * - UDFs registradas y aplicadas
 * - Limpieza y validación de datos
 */
@Service
public class OptimizationService {

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private DataReaderService dataReaderService;

    private Dataset<Row> cachedTransactions;
    private Dataset<Row> cachedProducts;
    private Dataset<Row> cachedCustomers;

    /**
     * Registra todas las UDFs en SparkSession
     */
    public void registerUDFs() {
        // Registrar UDF de validación de email
        sparkSession.udf().register("validate_email",
            new CustomUDFs.ValidateEmail(), DataTypes.BooleanType);

        // Registrar UDF de categorización de montos
        sparkSession.udf().register("categorize_amount",
            new CustomUDFs.CategorizeAmount(), DataTypes.StringType);

        // Registrar UDF de detección de fraude
        sparkSession.udf().register("detect_fraud",
            new CustomUDFs.DetectFraud(), DataTypes.StringType);

        // Registrar UDF de normalización
        sparkSession.udf().register("normalize_string",
            new CustomUDFs.NormalizeString(), DataTypes.StringType);

        // Registrar UDF de cálculo de descuento
        sparkSession.udf().register("calculate_discount",
            new CustomUDFs.CalculateDiscount(), DataTypes.DoubleType);

        System.out.println("✅ UDFs registradas exitosamente");
    }

    /**
     * Cachea datasets principales en memoria
     * Usa MEMORY_AND_DISK para evitar OOM errors
     */
    public void cacheMainDatasets() {
        if (cachedTransactions == null) {
            cachedTransactions = dataReaderService.readTransactions()
                .persist(StorageLevel.MEMORY_AND_DISK());
            System.out.println("✅ Transacciones cacheadas: " + cachedTransactions.count() + " registros");
        }

        if (cachedProducts == null) {
            cachedProducts = dataReaderService.readProducts()
                .persist(StorageLevel.MEMORY_AND_DISK());
            System.out.println("✅ Productos cacheados: " + cachedProducts.count() + " registros");
        }

        if (cachedCustomers == null) {
            cachedCustomers = dataReaderService.readCustomers()
                .persist(StorageLevel.MEMORY_AND_DISK());
            System.out.println("✅ Clientes cacheados: " + cachedCustomers.count() + " registros");
        }
    }

    /**
     * Libera caché de todos los datasets
     */
    public void unpersistAll() {
        if (cachedTransactions != null) {
            cachedTransactions.unpersist();
            cachedTransactions = null;
        }
        if (cachedProducts != null) {
            cachedProducts.unpersist();
            cachedProducts = null;
        }
        if (cachedCustomers != null) {
            cachedCustomers.unpersist();
            cachedCustomers = null;
        }
        System.out.println("✅ Caché liberado");
    }

    /**
     * Obtiene dataset de transacciones (cacheado si está disponible)
     */
    public Dataset<Row> getTransactions() {
        if (cachedTransactions != null) {
            return cachedTransactions;
        }
        return dataReaderService.readTransactions();
    }

    /**
     * Obtiene dataset de productos (cacheado si está disponible)
     */
    public Dataset<Row> getProducts() {
        if (cachedProducts != null) {
            return cachedProducts;
        }
        return dataReaderService.readProducts();
    }

    /**
     * Obtiene dataset de clientes (cacheado si está disponible)
     */
    public Dataset<Row> getCustomers() {
        if (cachedCustomers != null) {
            return cachedCustomers;
        }
        return dataReaderService.readCustomers();
    }

    /**
     * Reparticiona dataset por columna específica
     * Útil para optimizar joins y agregaciones
     */
    public Dataset<Row> repartitionByColumn(Dataset<Row> df, String columnName, int numPartitions) {
        return df.repartition(numPartitions, col(columnName));
    }

    /**
     * Limpia y valida datos de transacciones
     * - Elimina nulls
     * - Valida rangos de valores
     * - Elimina duplicados
     */
    public Dataset<Row> cleanTransactions() {
        Dataset<Row> transactions = getTransactions();

        Dataset<Row> cleaned = transactions
            // Eliminar filas con nulls críticos
            .na().drop(new String[]{"transaction_id", "product_id", "customer_id", "amount"})
            // Filtrar valores válidos
            .filter(col("amount").gt(0))
            .filter(col("quantity").gt(0))
            // Eliminar duplicados por transaction_id
            .dropDuplicates("transaction_id");

        long originalCount = transactions.count();
        long cleanedCount = cleaned.count();

        System.out.println("🧹 Limpieza completada:");
        System.out.println("  - Registros originales: " + originalCount);
        System.out.println("  - Registros limpios: " + cleanedCount);
        System.out.println("  - Registros eliminados: " + (originalCount - cleanedCount));

        return cleaned;
    }

    /**
     * Aplica UDFs a transacciones para enriquecimiento
     */
    public Dataset<Row> applyUDFsToTransactions() {
        registerUDFs(); // Asegurar que UDFs están registradas

        Dataset<Row> transactions = getTransactions();

        // Aplicar UDFs usando expresiones SQL
        return transactions
            .withColumn("amount_category", expr("categorize_amount(amount)"))
            .withColumn("fraud_risk", expr("detect_fraud(amount, quantity)"))
            .withColumn("discount_pct", expr("calculate_discount(amount)"))
            .withColumn("amount_with_discount",
                col("amount").minus(col("amount").multiply(col("discount_pct").divide(100))));
    }

    /**
     * Broadcast join optimizado para tablas pequeñas
     * Útil cuando products o customers son pequeños
     */
    public Dataset<Row> broadcastJoinTransactionsWithProducts() {
        Dataset<Row> transactions = getTransactions();
        Dataset<Row> products = getProducts();

        // Broadcast el DataFrame pequeño (products)
        return transactions.join(broadcast(products), "product_id");
    }

    /**
     * Estadísticas de particionamiento
     */
    public Map<String, Object> getPartitionStats(Dataset<Row> df) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("numPartitions", df.rdd().getNumPartitions());
        return stats;
    }

    /**
     * Información del caché actual
     */
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("transactionsCached", cachedTransactions != null);
        info.put("productsCached", cachedProducts != null);
        info.put("customersCached", cachedCustomers != null);

        if (cachedTransactions != null) {
            info.put("transactionsCount", cachedTransactions.count());
        }
        if (cachedProducts != null) {
            info.put("productsCount", cachedProducts.count());
        }
        if (cachedCustomers != null) {
            info.put("customersCount", cachedCustomers.count());
        }

        return info;
    }
}
