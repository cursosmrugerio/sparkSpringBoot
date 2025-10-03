package com.ecommerce.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar ventas agregadas por categoría de producto
 *
 * Campos:
 * - category: Nombre de la categoría (Electronics, Accessories, etc.)
 * - totalSales: Suma total de ventas en la categoría
 * - totalQuantity: Cantidad total de productos vendidos
 * - avgAmount: Ticket promedio por transacción
 * - transactionCount: Número de transacciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesByCategory {

    private String category;
    private Double totalSales;
    private Long totalQuantity;
    private Double avgAmount;
    private Long transactionCount;
}
