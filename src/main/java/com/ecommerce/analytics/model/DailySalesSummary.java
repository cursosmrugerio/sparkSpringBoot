package com.ecommerce.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar el resumen de ventas diarias
 *
 * Campos:
 * - date: Fecha del resumen (formato YYYY-MM-DD)
 * - totalSales: Total de ventas del día
 * - transactionCount: Número de transacciones
 * - avgTicket: Ticket promedio
 * - uniqueCustomers: Clientes únicos que compraron
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesSummary {

    private String date;
    private Double totalSales;
    private Long transactionCount;
    private Double avgTicket;
    private Long uniqueCustomers;
}
