package com.ecommerce.processor.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Table(name = "fraud_alerts", indexes = {
    @Index(name = "idx_fraud_alerts_order_id", columnList = "order_id"),
    @Index(name = "idx_fraud_alerts_reviewed", columnList = "reviewed"),
    @Index(name = "idx_fraud_alerts_risk_level", columnList = "risk_level")
})
public class FraudAlert extends PanacheEntity {

    @Column(name = "order_id", nullable = false, length = 255)
    private String orderId;

    @Column(name = "fraud_score", nullable = false)
    private Double fraudScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "flags", columnDefinition = "text[]")
    private String[] flags;

    @Column(name = "recommendation", nullable = false, length = 20)
    private String recommendation;

    @Column(name = "reviewed")
    @Builder.Default
    private Boolean reviewed = false;

    @Column(name = "reviewed_by", length = 255)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void markAsReviewed(String reviewedBy) {
        this.reviewed = true;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
    }

    public static long countUnreviewed() {
        return count("reviewed", false);
    }
}
