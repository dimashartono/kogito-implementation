package com.ecommerce.models;


public enum OrderStatus {
    PENDING,           // Order created, awaiting payment
    PAYMENT_PROCESSING, // Payment in progress
    PAYMENT_CONFIRMED, // Payment successful
    PAYMENT_FAILED,    // Payment failed
    VALIDATED,         // Order validated by stream processor
    PROCESSING,        // Order being prepared
    SHIPPED,           // Order shipped
    DELIVERED,         // Order delivered
    CANCELLED,         // Order cancelled
    REFUNDED,          // Order refunded
    FRAUD_SUSPECTED    // Potential fraud detected
}
