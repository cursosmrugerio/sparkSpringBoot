package com.ecommerce.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar los productos más vendidos
 *
 * Campos:
 * - productId: Identificador del producto
 * - productName: Nombre del producto
 * - category: Categoría del producto
 * - totalSales: Total de ventas generadas
 * - quantity: Cantidad de unidades vendidas
 * - rank: Posición en el ranking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProduct {

    private String productId;
    private String productName;
    private String category;
    private Double totalSales;
    private Long quantity;
    private Integer rank;
}
