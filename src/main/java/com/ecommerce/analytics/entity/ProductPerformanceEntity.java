package com.ecommerce.analytics.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para métricas de rendimiento de productos
 * Almacena análisis de productos procesados por Spark
 */
@Entity
@Table(name = "product_performance")
public class ProductPerformanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "total_revenue", nullable = false)
    private Double totalRevenue;

    @Column(name = "total_quantity", nullable = false)
    private Long totalQuantity;

    @Column(name = "transaction_count", nullable = false)
    private Long transactionCount;

    @Column(name = "avg_ticket")
    private Double avgTicket;

    @Column(name = "unique_customers")
    private Long uniqueCustomers;

    @Column(name = "rank_overall")
    private Integer rankOverall;

    @Column(name = "rank_in_category")
    private Integer rankInCategory;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (analysisDate == null) {
            analysisDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Long getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Long totalQuantity) { this.totalQuantity = totalQuantity; }

    public Long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Long transactionCount) { this.transactionCount = transactionCount; }

    public Double getAvgTicket() { return avgTicket; }
    public void setAvgTicket(Double avgTicket) { this.avgTicket = avgTicket; }

    public Long getUniqueCustomers() { return uniqueCustomers; }
    public void setUniqueCustomers(Long uniqueCustomers) { this.uniqueCustomers = uniqueCustomers; }

    public Integer getRankOverall() { return rankOverall; }
    public void setRankOverall(Integer rankOverall) { this.rankOverall = rankOverall; }

    public Integer getRankInCategory() { return rankInCategory; }
    public void setRankInCategory(Integer rankInCategory) { this.rankInCategory = rankInCategory; }

    public LocalDateTime getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDateTime analysisDate) { this.analysisDate = analysisDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
