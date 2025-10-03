package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.service.DataReaderService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
public class DataExplorationController {

    @Autowired
    private DataReaderService dataReaderService;

    /**
     * Endpoint de verificación: Lee transacciones y muestra información básica
     * GET /api/data/transactions?limit=10
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(defaultValue = "10") int limit) {

        Dataset<Row> transactions = dataReaderService.readTransactions();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", transactions.count());
        response.put("schema", getSchemaInfo(transactions));
        response.put("data", getDataAsList(transactions, limit));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para productos
     * GET /api/data/products?limit=10
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "10") int limit) {

        Dataset<Row> products = dataReaderService.readProducts();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", products.count());
        response.put("schema", getSchemaInfo(products));
        response.put("data", getDataAsList(products, limit));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para clientes
     * GET /api/data/customers?limit=10
     */
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomers(
            @RequestParam(defaultValue = "10") int limit) {

        Dataset<Row> customers = dataReaderService.readCustomers();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", customers.count());
        response.put("schema", getSchemaInfo(customers));
        response.put("data", getDataAsList(customers, limit));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de health check para Spark
     * GET /api/data/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("spark", "Running");
        response.put("message", "Spark integration is working correctly");

        return ResponseEntity.ok(response);
    }

    // Métodos auxiliares

    private List<String> getSchemaInfo(Dataset<Row> dataset) {
        return List.of(dataset.schema().fields()).stream()
                .map(field -> field.name() + ": " + field.dataType().simpleString())
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDataAsList(Dataset<Row> dataset, int limit) {
        return dataset.limit(limit)
                .collectAsList()
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < row.size(); i++) {
                        map.put(row.schema().fields()[i].name(), row.get(i));
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }
}
