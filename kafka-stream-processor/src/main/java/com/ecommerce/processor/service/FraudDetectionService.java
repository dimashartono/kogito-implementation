package com.ecommerce.processor.service;

import com.ecommerce.models.FraudCheckResult;
import com.ecommerce.models.Order;
import com.ecommerce.models.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@ApplicationScoped
public class FraudDetectionService {

    @ConfigProperty(name = "app.fraud-detection.enabled", defaultValue = "true")
    boolean fraudDetectionEnabled;

    @ConfigProperty(name = "app.fraud-detection.high-risk-threshold", defaultValue = "70.0")
    double highRiskThreshold;

    @ConfigProperty(name = "app.fraud-detection.suspicious-threshold", defaultValue = "50.0")
    double suspiciousThreshold;

    public FraudCheckResult analyzeOrder(Order order) {
        if (!fraudDetectionEnabled) {
            return createLowRiskResult(order.getOrderId());
        }

        List<String> flags = new ArrayList<>();
        double fraudScore = 0.0;

        if (order.getGrandTotal().compareTo(new BigDecimal("10000000")) > 0) {
            fraudScore += 15.0;
            flags.add("HIGH_VALUE_ORDER");
        }

        if (order.getCustomer().getTotalOrders() == 0 &&
            order.getGrandTotal().compareTo(new BigDecimal("5000000")) > 0) {
            fraudScore += 20.0;
            flags.add("NEW_CUSTOMER_HIGH_VALUE");
        }

        if (Boolean.FALSE.equals(order.getCustomer().getIsVerified())) {
            fraudScore += 10.0;
            flags.add("UNVERIFIED_CUSTOMER");
        }

        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() > 10) {
                fraudScore += 15.0;
                flags.add("BULK_ORDER");
                break;
            }
        }

        LocalTime orderTime = order.getCreatedAt().toLocalTime();
        if (orderTime.isAfter(LocalTime.MIDNIGHT) && orderTime.isBefore(LocalTime.of(5, 0))) {
            fraudScore += 10.0;
            flags.add("LATE_NIGHT_ORDER");
        }

        if (order.getPayment().getMethod().name().equals("COD") &&
            order.getGrandTotal().compareTo(new BigDecimal("3000000")) > 0) {
            fraudScore += 25.0;
            flags.add("HIGH_VALUE_COD");
        }

        long electronicsCount = order.getItems().stream()
                .filter(item -> item.getCategory() != null &&
                        item.getCategory().equalsIgnoreCase("ELECTRONICS"))
                .count();
        if (electronicsCount >= 3) {
            fraudScore += 15.0;
            flags.add("MULTIPLE_ELECTRONICS");
        }

        fraudScore = Math.min(fraudScore, 100.0);
        FraudCheckResult.RiskLevel riskLevel = FraudCheckResult.calculateRiskLevel(fraudScore);
        String recommendation = determineRecommendation(fraudScore);

        log.debug("Fraud analysis for order {}: score={}, level={}, flags={}",
                order.getOrderId(), fraudScore, riskLevel, flags);

        return FraudCheckResult.builder()
                .orderId(order.getOrderId())
                .fraudScore(fraudScore)
                .isSuspicious(fraudScore >= suspiciousThreshold)
                .riskLevel(riskLevel)
                .flags(flags)
                .recommendation(recommendation)
                .build();
    }

    private String determineRecommendation(double fraudScore) {
        if (fraudScore >= highRiskThreshold) {
            return "REVIEW";
        } else if (fraudScore >= suspiciousThreshold) {
            return "APPROVE_WITH_MONITORING";
        } else {
            return "APPROVE";
        }
    }

    private FraudCheckResult createLowRiskResult(String orderId) {
        return FraudCheckResult.builder()
                .orderId(orderId)
                .fraudScore(0.0)
                .isSuspicious(false)
                .riskLevel(FraudCheckResult.RiskLevel.LOW)
                .flags(new ArrayList<>())
                .recommendation("APPROVE")
                .build();
    }
}
