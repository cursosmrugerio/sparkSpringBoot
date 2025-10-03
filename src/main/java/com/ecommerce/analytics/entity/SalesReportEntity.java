package com.ecommerce.analytics.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para reportes de ventas procesados
 * Almacena resultados de análisis de Spark en PostgreSQL
 */
@Entity
@Table(name = "sales_reports")
public class SalesReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "category")
    private String category;

    @Column(name = "region")
    private String region;

    @Column(name = "total_sales", nullable = false)
    private Double totalSales;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "transaction_count")
    private Long transactionCount;

    @Column(name = "avg_ticket")
    private Double avgTicket;

    @Column(name = "unique_customers")
    private Long uniqueCustomers;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getTotalSales() { return totalSales; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }

    public Long getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Long totalQuantity) { this.totalQuantity = totalQuantity; }

    public Long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Long transactionCount) { this.transactionCount = transactionCount; }

    public Double getAvgTicket() { return avgTicket; }
    public void setAvgTicket(Double avgTicket) { this.avgTicket = avgTicket; }

    public Long getUniqueCustomers() { return uniqueCustomers; }
    public void setUniqueCustomers(Long uniqueCustomers) { this.uniqueCustomers = uniqueCustomers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
