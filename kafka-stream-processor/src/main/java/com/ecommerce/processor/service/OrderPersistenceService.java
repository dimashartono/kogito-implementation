package com.ecommerce.processor.service;

import com.ecommerce.models.FraudCheckResult;
import com.ecommerce.models.Order;
import com.ecommerce.processor.entity.FraudAlert;
import com.ecommerce.processor.entity.OrderAuditLog;
import com.ecommerce.processor.exception.OrderPersistenceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
public class OrderPersistenceService {

    private final ObjectMapper objectMapper;

    public OrderPersistenceService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public void saveOrderAuditLog(Order order) {
        try {
            OrderAuditLog auditLog = OrderAuditLog.builder()
                    .orderId(order.getOrderId())
                    .customerId(order.getCustomer().getCustomerId())
                    .customerName(order.getCustomer().getName())
                    .customerEmail(order.getCustomer().getEmail())
                    .orderStatus(order.getStatus().name())
                    .totalItems(order.getTotalItems())
                    .subtotal(order.getSubtotal())
                    .shippingCost(order.getShippingCost())
                    .voucherDiscount(order.getVoucherDiscount())
                    .grandTotal(order.getGrandTotal())
                    .paymentMethod(order.getPayment().getMethod().name())
                    .paymentTransactionId(order.getPayment().getTransactionId())
                    .isPaymentPaid(order.getPayment().getIsPaid())
                    .shippingCity(order.getShippingAddress().getCity())
                    .shippingProvince(order.getShippingAddress().getProvince())
                    .shippingCountry(order.getShippingAddress().getCountry())
                    .fraudScore(order.getFraudScore())
                    .isSuspicious(order.isSuspicious())
                    .source(order.getSource())
                    .orderData(objectMapper.writeValueAsString(order))
                    .processedAt(order.getUpdatedAt())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build();

            auditLog.persist();
            log.info("Order {} saved to audit log with ID: {}", order.getOrderId(), auditLog.id);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order {} to JSON", order.getOrderId(), e);
            throw new OrderPersistenceException("Failed to save order audit log", e);
        }
    }

    @Transactional
    public void saveFraudAlert(FraudCheckResult fraudResult) {
        FraudAlert alert = FraudAlert.builder()
                .orderId(fraudResult.getOrderId())
                .fraudScore(fraudResult.getFraudScore())
                .riskLevel(fraudResult.getRiskLevel().name())
                .flags(fraudResult.getFlags().toArray(new String[0]))
                .recommendation(fraudResult.getRecommendation())
                .reviewed(false)
                .build();

        alert.persist();

        log.warn("Fraud alert created for order {} with score: {} (Risk: {})",
                fraudResult.getOrderId(),
                fraudResult.getFraudScore(),
                fraudResult.getRiskLevel());
    }
}
