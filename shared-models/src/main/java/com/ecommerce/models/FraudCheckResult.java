package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckResult {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("fraud_score")
    private Double fraudScore; 

    @JsonProperty("is_suspicious")
    private Boolean isSuspicious;

    @JsonProperty("risk_level")
    private RiskLevel riskLevel;

    @JsonProperty("flags")
    @Builder.Default
    private List<String> flags = new ArrayList<>(); 

    @JsonProperty("recommendation")
    private String recommendation; 

    public enum RiskLevel {
        LOW,     // 0-30
        MEDIUM,  // 31-70
        HIGH     // 71-100
    }

    public static RiskLevel calculateRiskLevel(double score) {
        if (score <= 30) {
            return RiskLevel.LOW;
        } else if (score <= 70) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.HIGH;
        }
    }
}
