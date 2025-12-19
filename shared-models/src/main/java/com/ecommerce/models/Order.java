package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @NotBlank(message = "Order ID is required")
    @JsonProperty("order_id")
    private String orderId;

    @NotNull(message = "Customer information is required")
    @Valid
    @JsonProperty("customer")
    private Customer customer;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @JsonProperty("items")
    private List<OrderItem> items;

    @NotNull(message = "Shipping address is required")
    @Valid
    @JsonProperty("shipping_address")
    private Address shippingAddress;

    @NotNull(message = "Payment information is required")
    @Valid
    @JsonProperty("payment")
    private Payment payment;

    @NotNull(message = "Order status is required")
    @JsonProperty("status")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @JsonProperty("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonProperty("updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @JsonProperty("shipping_cost")
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @JsonProperty("voucher_code")
    private String voucherCode;

    @JsonProperty("voucher_discount")
    @Builder.Default
    private BigDecimal voucherDiscount = BigDecimal.ZERO;

    @JsonProperty("notes")
    private String notes; 

    @JsonProperty("fraud_score")
    private Double fraudScore; 

    @JsonProperty("source")
    @Builder.Default
    private String source = "WEB"; 


    public int getTotalItems() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrandTotal() {
        return getSubtotal()
                .add(shippingCost)
                .subtract(voucherDiscount);
    }

    public boolean isFinalState() {
        return status == OrderStatus.DELIVERED ||
                status == OrderStatus.CANCELLED ||
                status == OrderStatus.REFUNDED;
    }

    public boolean isSuspicious() {
        return fraudScore != null && fraudScore > 70.0;
    }
}
