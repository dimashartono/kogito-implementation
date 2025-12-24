package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @NotBlank(message = "Product ID is required")
    @JsonProperty("product_id")
    private String productId;

    @NotBlank(message = "Product name is required")
    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("sku")
    private String sku; 

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @JsonProperty("quantity")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("discount_percent")
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @JsonProperty("tax_percent")
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @JsonProperty("weight_grams")
    private Integer weightGrams; 

    @JsonProperty("category")
    private String category;

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getDiscountAmount() {
        return getSubtotal().multiply(discountPercent).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getTaxAmount() {
        BigDecimal afterDiscount = getSubtotal().subtract(getDiscountAmount());
        return afterDiscount.multiply(taxPercent).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getTotalPrice() {
        return getSubtotal().subtract(getDiscountAmount()).add(getTaxAmount());
    }
}
