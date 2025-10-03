package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.model.TopProduct;
import com.ecommerce.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para análisis de productos
 *
 * Endpoints:
 * - GET /api/products/top-selling              -> Top productos más vendidos
 * - GET /api/products/by-category/{category}   -> Productos por categoría
 * - GET /api/products/{productId}/analytics    -> Análisis de producto específico
 *
 * Demuestra:
 * - Window Functions (ranking)
 * - Joins complejos
 * - Filtrado por path variables
 * - Análisis detallado por entidad
 */
@RestController
@RequestMapping("/api/products")
public class ProductAnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Endpoint 1: TOP PRODUCTOS MÁS VENDIDOS
     *
     * GET /api/products/top-selling?limit=10
     *
     * Retorna ranking de productos más vendidos
     * Parámetro: limit (default: 10)
     *
     * Operaciones Spark demostradas:
     * - JOIN (transactions + products)
     * - groupBy + agg
     * - Window Functions para ranking
     * - orderBy + limit
     */
    @GetMapping("/top-selling")
    public ResponseEntity<List<TopProduct>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {

        List<TopProduct> topProducts = analyticsService.getTopProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    /**
     * Endpoint 2: PRODUCTOS POR CATEGORÍA CON VENTAS
     *
     * GET /api/products/by-category/{category}
     *
     * Ejemplo: GET /api/products/by-category/Electronics
     *
     * Retorna todos los productos de una categoría con sus métricas de venta
     *
     * Operaciones Spark demostradas:
     * - filter() por categoría
     * - JOIN para enriquecer datos
     * - Múltiples agregaciones
     */
    @GetMapping("/by-category/{category}")
    public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(
            @PathVariable String category) {

        List<Map<String, Object>> products = analyticsService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint 3: ANÁLISIS DE PRODUCTO ESPECÍFICO
     *
     * GET /api/products/{productId}/analytics
     *
     * Ejemplo: GET /api/products/PROD001/analytics
     *
     * Retorna análisis detallado de un producto:
     * - Info del producto (nombre, precio, stock)
     * - Métricas de ventas (revenue, cantidad, transacciones)
     * - Clientes únicos que compraron
     *
     * Operaciones Spark demostradas:
     * - filter() por product_id
     * - Combinación de info estática (products) y dinámica (transactions)
     * - Agregaciones específicas
     */
    @GetMapping("/{productId}/analytics")
    public ResponseEntity<Map<String, Object>> getProductAnalytics(
            @PathVariable String productId) {

        Map<String, Object> analytics = analyticsService.getProductAnalytics(productId);

        if (analytics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(analytics);
    }
}
