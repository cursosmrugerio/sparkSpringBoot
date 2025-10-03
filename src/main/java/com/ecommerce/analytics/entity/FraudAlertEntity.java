package com.ecommerce.analytics.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para alertas de fraude detectadas
 * Almacena transacciones sospechosas identificadas por Spark
 */
@Entity
@Table(name = "fraud_alerts")
public class FraudAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel; // BAJO_RIESGO, MEDIO_RIESGO, ALTO_RIESGO

    @Column(name = "amount_category")
    private String amountCategory; // BAJO, MEDIO, ALTO, MUY_ALTO

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "region")
    private String region;

    @Column(name = "deviation_from_mean")
    private Double deviationFromMean;

    @Column(name = "is_outlier")
    private Boolean isOutlier;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed")
    private Boolean reviewed = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reviewed == null) {
            reviewed = false;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getAmountCategory() { return amountCategory; }
    public void setAmountCategory(String amountCategory) { this.amountCategory = amountCategory; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getDeviationFromMean() { return deviationFromMean; }
    public void setDeviationFromMean(Double deviationFromMean) { this.deviationFromMean = deviationFromMean; }

    public Boolean getIsOutlier() { return isOutlier; }
    public void setIsOutlier(Boolean isOutlier) { this.isOutlier = isOutlier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getReviewed() { return reviewed; }
    public void setReviewed(Boolean reviewed) { this.reviewed = reviewed; }
}
