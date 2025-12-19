package com.ecommerce.processor.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Table(name = "order_audit_log", indexes = {
    @Index(name = "idx_order_audit_order_id", columnList = "order_id"),
    @Index(name = "idx_order_audit_customer_id", columnList = "customer_id"),
    @Index(name = "idx_order_audit_status", columnList = "order_status"),
    @Index(name = "idx_order_audit_processed_at", columnList = "processed_at")
})
public class OrderAuditLog extends PanacheEntity {

    @Column(name = "order_id", nullable = false, length = 255)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 255)
    private String customerId;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "voucher_discount", nullable = false, precision = 15, scale = 2)
    private BigDecimal voucherDiscount;

    @Column(name = "grand_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId;

    @Column(name = "is_payment_paid", nullable = false)
    private Boolean isPaymentPaid;

    @Column(name = "shipping_city", length = 255)
    private String shippingCity;

    @Column(name = "shipping_province", length = 255)
    private String shippingProvince;

    @Column(name = "shipping_country", length = 255)
    private String shippingCountry;

    @Column(name = "fraud_score")
    private Double fraudScore;

    @Column(name = "is_suspicious")
    private Boolean isSuspicious;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "order_data", columnDefinition = "jsonb")
    private String orderData;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }

    public static long countByCustomerId(String customerId) {
        return count("customerId", customerId);
    }

    public static long countSuspiciousOrders() {
        return count("isSuspicious", true);
    }
}
