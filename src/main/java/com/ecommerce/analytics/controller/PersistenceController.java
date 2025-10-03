package com.ecommerce.analytics.controller;

import com.ecommerce.analytics.entity.ProductPerformanceEntity;
import com.ecommerce.analytics.entity.SalesReportEntity;
import com.ecommerce.analytics.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones de persistencia
 *
 * Endpoints:
 * - GET /api/persistence/reports - Obtener reportes guardados
 * - GET /api/persistence/products/performance - Rendimiento de productos
 * - GET /api/persistence/stats - Estadísticas de la base de datos
 */
@RestController
@RequestMapping("/api/persistence")
public class PersistenceController {

    @Autowired
    private PersistenceService persistenceService;

    /**
     * Obtener reportes de ventas guardados
     */
    @GetMapping("/reports")
    public ResponseEntity<List<SalesReportEntity>> getSalesReports(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<SalesReportEntity> reports;

        if (startDate != null && endDate != null) {
            reports = persistenceService.getSalesReportsByDateRange(startDate, endDate);
        } else {
            // Por defecto, últimos 30 días
            reports = persistenceService.getSalesReportsByDateRange(
                LocalDate.now().minusDays(30),
                LocalDate.now()
            );
        }

        return ResponseEntity.ok(reports);
    }

    /**
     * Obtener rendimiento de productos
     */
    @GetMapping("/products/performance")
    public ResponseEntity<List<ProductPerformanceEntity>> getProductPerformance(
        @RequestParam(required = false) String productId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<ProductPerformanceEntity> performance;

        if (productId != null) {
            performance = persistenceService.getProductPerformance(productId);
        } else {
            performance = persistenceService.getTopProductsByRevenue(limit);
        }

        return ResponseEntity.ok(performance);
    }

    /**
     * Obtener top productos por revenue
     */
    @GetMapping("/products/top-revenue")
    public ResponseEntity<List<ProductPerformanceEntity>> getTopProductsByRevenue(
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<ProductPerformanceEntity> topProducts = persistenceService.getTopProductsByRevenue(limit);
        return ResponseEntity.ok(topProducts);
    }

    /**
     * Estadísticas de la base de datos
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        Map<String, Object> stats = persistenceService.getDatabaseStats();
        return ResponseEntity.ok(stats);
    }
}
