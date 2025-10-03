package com.ecommerce.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar ventas agregadas por región
 *
 * Campos:
 * - region: Nombre de la región (North, South, East, West)
 * - totalSales: Total de ventas en la región
 * - customerCount: Número de clientes únicos
 * - transactionCount: Número de transacciones
 * - avgTicket: Ticket promedio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesByRegion {

    private String region;
    private Double totalSales;
    private Long customerCount;
    private Long transactionCount;
    private Double avgTicket;
}
