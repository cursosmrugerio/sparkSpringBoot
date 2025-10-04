package com.ecommerce.analytics.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para reportes diarios generados automáticamente
 * Almacena métricas agregadas del día
 */
@Entity
@Table(name = "daily_reports")
public class DailyReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @Column(name = "total_sales", nullable = false)
    private Double totalSales;

    @Column(name = "total_transactions", nullable = false)
    private Long totalTransactions;

    @Column(name = "unique_customers", nullable = false)
    private Long uniqueCustomers;

    @Column(name = "avg_ticket", nullable = false)
    private Double avgTicket;

    @Column(name = "top_category")
    private String topCategory;

    @Column(name = "top_product")
    private String topProduct;

    @Column(name = "fraud_alerts_count")
    private Long fraudAlertsCount;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public Double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Double totalSales) {
        this.totalSales = totalSales;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Long getUniqueCustomers() {
        return uniqueCustomers;
    }

    public void setUniqueCustomers(Long uniqueCustomers) {
        this.uniqueCustomers = uniqueCustomers;
    }

    public Double getAvgTicket() {
        return avgTicket;
    }

    public void setAvgTicket(Double avgTicket) {
        this.avgTicket = avgTicket;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public String getTopProduct() {
        return topProduct;
    }

    public void setTopProduct(String topProduct) {
        this.topProduct = topProduct;
    }

    public Long getFraudAlertsCount() {
        return fraudAlertsCount;
    }

    public void setFraudAlertsCount(Long fraudAlertsCount) {
        this.fraudAlertsCount = fraudAlertsCount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
