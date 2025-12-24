package com.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @NotBlank(message = "Customer ID is required")
    @JsonProperty("customer_id")
    private String customerId;

    @NotBlank(message = "Customer name is required")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @JsonProperty("phone")
    private String phone;

    @JsonProperty("is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @JsonProperty("total_orders")
    @Builder.Default
    private Integer totalOrders = 0;
}
