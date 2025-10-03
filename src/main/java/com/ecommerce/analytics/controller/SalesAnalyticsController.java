package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.model.DailySalesSummary;
import com.ecommerce.analytics.model.SalesByCategory;
import com.ecommerce.analytics.model.SalesByRegion;
import com.ecommerce.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para análisis de ventas
 *
 * Endpoints:
 * - GET /api/sales/by-category        -> Ventas por categoría
 * - GET /api/sales/daily-summary      -> Resumen diario
 * - GET /api/sales/by-region          -> Ventas por región
 * - GET /api/sales/statistics         -> Estadísticas generales
 *
 * Demuestra:
 * - Agregaciones con Spark
 * - Joins entre datasets
 * - Filtros dinámicos por fecha
 * - Conversión de DataFrames a DTOs
 */
@RestController
@RequestMapping("/api/sales")
public class SalesAnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Endpoint 1: VENTAS POR CATEGORÍA
     *
     * GET /api/sales/by-category
     *
     * Retorna ventas agregadas por categoría de producto
     * Incluye: total ventas, cantidad vendida, ticket promedio
     *
     * Operaciones Spark demostradas:
     * - JOIN (transactions + products)
     * - groupBy() + agg()
     * - sum(), avg(), count()
     */
    @GetMapping("/by-category")
    public ResponseEntity<List<SalesByCategory>> getSalesByCategory() {
        List<SalesByCategory> sales = analyticsService.getSalesByCategory();
        return ResponseEntity.ok(sales);
    }

    /**
     * Endpoint 2: RESUMEN DIARIO DE VENTAS
     *
     * GET /api/sales/daily-summary?startDate=2024-10-01&endDate=2024-10-31
     *
     * Retorna resumen de ventas por día
     * Filtros opcionales: startDate, endDate
     *
     * Operaciones Spark demostradas:
     * - Filtrado por rango de fechas
     * - Funciones de fecha (to_date)
     * - groupBy por fecha
     * - countDistinct() para clientes únicos
     */
    @GetMapping("/daily-summary")
    public ResponseEntity<List<DailySalesSummary>> getDailySummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<DailySalesSummary> summary = analyticsService.getDailySummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Endpoint 3: VENTAS POR REGIÓN
     *
     * GET /api/sales/by-region?startDate=2024-10-01&endDate=2024-10-31
     *
     * Retorna ventas agregadas por región geográfica
     * Filtros opcionales: startDate, endDate
     *
     * Operaciones Spark demostradas:
     * - Filtrado dinámico por fechas
     * - Agregaciones múltiples
     * - orderBy con desc()
     */
    @GetMapping("/by-region")
    public ResponseEntity<List<SalesByRegion>> getSalesByRegion(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<SalesByRegion> sales = analyticsService.getSalesByRegion(startDate, endDate);
        return ResponseEntity.ok(sales);
    }

    /**
     * Endpoint 4: ESTADÍSTICAS GENERALES
     *
     * GET /api/sales/statistics
     *
     * Retorna métricas globales del negocio:
     * - Total revenue
     * - Ticket promedio
     * - Transacción máxima/mínima
     * - Total de transacciones
     * - Clientes únicos
     *
     * Operaciones Spark demostradas:
     * - Múltiples agregaciones en una sola pasada
     * - sum(), avg(), max(), min(), count(), countDistinct()
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = analyticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
}
