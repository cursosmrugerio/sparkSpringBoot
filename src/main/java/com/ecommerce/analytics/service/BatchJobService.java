package com.ecommerce.analytics.service;

import com.ecommerce.analytics.entity.BatchJobExecutionEntity;
import com.ecommerce.analytics.repository.BatchJobExecutionRepository;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para procesamiento batch con Spark
 * Implementa ETL pipeline completo con manejo robusto de errores
 */
@Service
public class BatchJobService {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobService.class);

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private BatchJobExecutionRepository jobExecutionRepository;

    @Autowired
    private DataReaderService dataReaderService;

    @Autowired
    private PersistenceService persistenceService;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * Ejecuta pipeline ETL completo
     * Extract → Transform → Load
     */
    @Transactional
    public BatchJobExecutionEntity runETLPipeline(String jobParams) {
        String jobName = "ETL_DAILY_PIPELINE";
        BatchJobExecutionEntity execution = startJobExecution(jobName, jobParams);

        try {
            logger.info("🚀 Iniciando ETL Pipeline: {}", jobName);
            long recordsProcessed = 0;

            // EXTRACT: Leer datos de CSV
            logger.info("📥 EXTRACT: Leyendo datos de CSVs...");
            Dataset<Row> transactions = dataReaderService.readTransactions();
            Dataset<Row> products = dataReaderService.readProducts();
            Dataset<Row> customers = dataReaderService.readCustomers();

            long totalRecords = transactions.count();
            logger.info("✅ Datos extraídos: {} transacciones", totalRecords);

            // TRANSFORM: Limpiar y agregar
            logger.info("🔄 TRANSFORM: Limpiando y transformando datos...");
            Dataset<Row> cleanedData = cleanAndValidateData(transactions);
            Dataset<Row> enrichedData = enrichTransactionsWithProducts(cleanedData, products);
            Dataset<Row> aggregatedData = aggregateSalesData(enrichedData);

            recordsProcessed = cleanedData.count();
            logger.info("✅ Datos transformados: {} registros limpios", recordsProcessed);

            // LOAD: Guardar en PostgreSQL y Parquet
            logger.info("💾 LOAD: Guardando resultados...");
            saveToPostgreSQL(aggregatedData, "sales_aggregated");
            saveToParquet(enrichedData, "enriched_transactions");

            logger.info("✅ ETL Pipeline completado exitosamente");

            // Marcar como exitoso
            completeJobExecution(execution, recordsProcessed, 0);
            return execution;

        } catch (Exception e) {
            logger.error("❌ Error en ETL Pipeline: {}", e.getMessage(), e);
            failJobExecution(execution, e.getMessage());
            throw new RuntimeException("ETL Pipeline failed", e);
        }
    }

    /**
     * Procesa transacciones incrementales (solo nuevas)
     */
    @Transactional
    public BatchJobExecutionEntity processIncrementalData(LocalDateTime since) {
        String jobName = "INCREMENTAL_PROCESSING";
        String params = "since=" + since;
        BatchJobExecutionEntity execution = startJobExecution(jobName, params);

        try {
            logger.info("🚀 Procesamiento incremental desde: {}", since);

            Dataset<Row> transactions = dataReaderService.readTransactions();

            // Filtrar solo datos nuevos (simulado con timestamp)
            Dataset<Row> newData = transactions.filter(
                transactions.col("transaction_date").gt(since.toString())
            );

            long recordsProcessed = newData.count();
            logger.info("📊 Registros nuevos detectados: {}", recordsProcessed);

            if (recordsProcessed > 0) {
                // Procesar y guardar solo datos nuevos
                saveToPostgreSQL(newData, "incremental_transactions");
                logger.info("✅ Datos incrementales guardados");
            } else {
                logger.info("ℹ️ No hay datos nuevos para procesar");
            }

            completeJobExecution(execution, recordsProcessed, 0);
            return execution;

        } catch (Exception e) {
            logger.error("❌ Error en procesamiento incremental: {}", e.getMessage(), e);
            failJobExecution(execution, e.getMessage());
            throw new RuntimeException("Incremental processing failed", e);
        }
    }

    /**
     * Limpia y valida datos
     */
    private Dataset<Row> cleanAndValidateData(Dataset<Row> data) {
        return data
                .na().drop()  // Eliminar nulls
                .dropDuplicates("transaction_id")  // Eliminar duplicados
                .filter(data.col("amount").gt(0));  // Validar montos positivos
    }

    /**
     * Enriquece transacciones con información de productos
     */
    private Dataset<Row> enrichTransactionsWithProducts(Dataset<Row> transactions, Dataset<Row> products) {
        return transactions.join(
                products,
                transactions.col("product_id").equalTo(products.col("product_id")),
                "left"
        ).select(
                transactions.col("*"),
                products.col("product_name"),
                products.col("category"),
                products.col("price")
        );
    }

    /**
     * Agrega datos de ventas
     */
    private Dataset<Row> aggregateSalesData(Dataset<Row> data) {
        return data.groupBy("category")
                .agg(
                        org.apache.spark.sql.functions.sum("amount").alias("total_sales"),
                        org.apache.spark.sql.functions.count("transaction_id").alias("transaction_count"),
                        org.apache.spark.sql.functions.avg("amount").alias("avg_amount")
                );
    }

    /**
     * Guarda datos en PostgreSQL
     */
    private void saveToPostgreSQL(Dataset<Row> data, String tableName) {
        Map<String, String> jdbcOptions = new HashMap<>();
        jdbcOptions.put("url", jdbcUrl);
        jdbcOptions.put("dbtable", tableName);
        jdbcOptions.put("user", dbUser);
        jdbcOptions.put("password", dbPassword);
        jdbcOptions.put("driver", "org.postgresql.Driver");

        data.write()
                .format("jdbc")
                .options(jdbcOptions)
                .mode(SaveMode.Append)
                .save();

        logger.info("✅ Datos guardados en PostgreSQL: {}", tableName);
    }

    /**
     * Guarda datos en formato Parquet
     */
    private void saveToParquet(Dataset<Row> data, String fileName) {
        String path = "./output/" + fileName + "_" + System.currentTimeMillis() + ".parquet";
        data.write()
                .mode(SaveMode.Overwrite)
                .parquet(path);

        logger.info("✅ Datos guardados en Parquet: {}", path);
    }

    /**
     * Inicia una ejecución de job
     */
    private BatchJobExecutionEntity startJobExecution(String jobName, String params) {
        BatchJobExecutionEntity execution = new BatchJobExecutionEntity();
        execution.setJobName(jobName);
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        execution.setExecutionParams(params);
        execution.setRecordsProcessed(0L);
        execution.setRecordsFailed(0L);

        return jobExecutionRepository.save(execution);
    }

    /**
     * Completa una ejecución exitosa
     */
    private void completeJobExecution(BatchJobExecutionEntity execution, long recordsProcessed, long recordsFailed) {
        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(execution.getStartTime(), endTime).toMillis();

        execution.setStatus("SUCCESS");
        execution.setEndTime(endTime);
        execution.setDurationMs(durationMs);
        execution.setRecordsProcessed(recordsProcessed);
        execution.setRecordsFailed(recordsFailed);

        jobExecutionRepository.save(execution);
        logger.info("✅ Job completado: {} | Duración: {}ms | Registros: {}",
                execution.getJobName(), durationMs, recordsProcessed);
    }

    /**
     * Marca una ejecución como fallida
     */
    private void failJobExecution(BatchJobExecutionEntity execution, String errorMessage) {
        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(execution.getStartTime(), endTime).toMillis();

        execution.setStatus("FAILED");
        execution.setEndTime(endTime);
        execution.setDurationMs(durationMs);
        execution.setErrorMessage(errorMessage != null && errorMessage.length() > 2000
                ? errorMessage.substring(0, 2000)
                : errorMessage);

        jobExecutionRepository.save(execution);
        logger.error("❌ Job fallido: {} | Error: {}", execution.getJobName(), errorMessage);
    }
}
