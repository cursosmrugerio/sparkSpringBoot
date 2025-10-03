package com.ecommerce.analytics.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataReaderService {

    @Autowired
    private SparkSession sparkSession;

    @Value("${spark.data.path}")
    private String dataPath;

    /**
     * Lee archivo CSV de transacciones
     * Demuestra: lectura básica, inferencia de schema, manejo de headers
     */
    public Dataset<Row> readTransactions() {
        String filePath = dataPath + "/transactions.csv";

        return sparkSession.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(filePath);
    }

    /**
     * Lee archivo CSV de productos
     */
    public Dataset<Row> readProducts() {
        String filePath = dataPath + "/products.csv";

        return sparkSession.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(filePath);
    }

    /**
     * Lee archivo CSV de clientes
     */
    public Dataset<Row> readCustomers() {
        String filePath = dataPath + "/customers.csv";

        return sparkSession.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(filePath);
    }

    /**
     * Muestra información básica de un DataFrame
     * Útil para entender la estructura de los datos
     */
    public void showDatasetInfo(Dataset<Row> dataset, String datasetName) {
        System.out.println("=== Dataset: " + datasetName + " ===");
        System.out.println("Total de registros: " + dataset.count());
        System.out.println("\nSchema:");
        dataset.printSchema();
        System.out.println("\nPrimeras 5 filas:");
        dataset.show(5);
    }
}
