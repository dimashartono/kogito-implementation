package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @NotNull(message = "Payment method is required")
    @JsonProperty("method")
    private PaymentMethod method;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    @Builder.Default
    private String currency = "IDR"; 

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("payment_gateway")
    private String paymentGateway; 

    @JsonProperty("card_last_four")
    private String cardLastFour; 

    @JsonProperty("is_paid")
    @Builder.Default
    private Boolean isPaid = false;
}
