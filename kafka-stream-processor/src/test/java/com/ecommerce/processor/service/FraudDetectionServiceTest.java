package com.ecommerce.processor.service;

import com.ecommerce.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
@DisplayName("Fraud Detection Service Tests")
class FraudDetectionServiceTest {

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Order normalOrder;
    private Order suspiciousOrder;

    @BeforeEach
    void setUp() {
        normalOrder = createOrder(
            "ORD-001",
            new BigDecimal("500000"), 
            PaymentMethod.CREDIT_CARD,
            1,
            true,
            LocalDateTime.of(2024, 1, 15, 14, 30)
        );

        suspiciousOrder = createOrder(
            "ORD-002",
            new BigDecimal("15000000"), 
            PaymentMethod.COD,
            0, 
            false, 
            LocalDateTime.of(2024, 1, 15, 2, 30) 
        );
    }

    @Test
    @DisplayName("Should return low risk for normal order")
    void testNormalOrderLowRisk() {
        FraudCheckResult result = fraudDetectionService.analyzeOrder(normalOrder);
        assertThat(result).isNotNull();
        assertThat(result.getFraudScore()).isLessThan(30.0);
        assertThat(result.getRiskLevel()).isEqualTo(FraudCheckResult.RiskLevel.LOW);
        assertThat(result.getIsSuspicious()).isFalse();
        assertThat(result.getRecommendation()).isEqualTo("APPROVE");
    }

    @Test
    @DisplayName("Should detect high value order")
    void testHighValueOrderDetection() {
        Order highValueOrder = createOrder(
            "ORD-003",
            new BigDecimal("12000000"), 
            PaymentMethod.CREDIT_CARD,
            5,
            true,
            LocalDateTime.now()
        );

        FraudCheckResult result = fraudDetectionService.analyzeOrder(highValueOrder);
        assertThat(result.getFlags()).contains("HIGH_VALUE_ORDER");
        assertThat(result.getFraudScore()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should detect new customer with high value")
    void testNewCustomerHighValue() {
        Order order = createOrder(
            "ORD-004",
            new BigDecimal("6000000"), 
            PaymentMethod.E_WALLET,
            0, 
            true,
            LocalDateTime.now()
        );

        FraudCheckResult result = fraudDetectionService.analyzeOrder(order);
        assertThat(result.getFlags()).contains("NEW_CUSTOMER_HIGH_VALUE");
    }

    @Test
    @DisplayName("Should detect unverified customer")
    void testUnverifiedCustomer() {
        Order order = createOrder(
            "ORD-005",
            new BigDecimal("1000000"),
            PaymentMethod.CREDIT_CARD,
            3,
            false, 
            LocalDateTime.now()
        );

        FraudCheckResult result = fraudDetectionService.analyzeOrder(order);
        assertThat(result.getFlags()).contains("UNVERIFIED_CUSTOMER");
    }

    @Test
    @DisplayName("Should detect bulk order")
    void testBulkOrderDetection() {
        OrderItem bulkItem = OrderItem.builder()
                .productId("P001")
                .productName("Electronics Item")
                .quantity(15) 
                .unitPrice(new BigDecimal("100000"))
                .category("ELECTRONICS")
                .build();

        Order order = normalOrder;
        order.setItems(List.of(bulkItem));

        FraudCheckResult result = fraudDetectionService.analyzeOrder(order);
        assertThat(result.getFlags()).contains("BULK_ORDER");
    }

    @Test
    @DisplayName("Should detect late night order")
    void testLateNightOrder() {
        Order lateNightOrder = createOrder(
            "ORD-006",
            new BigDecimal("800000"),
            PaymentMethod.CREDIT_CARD,
            2,
            true,
            LocalDateTime.of(2024, 1, 15, 3, 0) 
        );
        
        FraudCheckResult result = fraudDetectionService.analyzeOrder(lateNightOrder);
        assertThat(result.getFlags()).contains("LATE_NIGHT_ORDER");
    }

    @Test
    @DisplayName("Should detect high value COD")
    void testHighValueCOD() {
        Order codOrder = createOrder(
            "ORD-007",
            new BigDecimal("4000000"), 
            PaymentMethod.COD,
            1,
            true,
            LocalDateTime.now()
        );
        
        FraudCheckResult result = fraudDetectionService.analyzeOrder(codOrder);
        assertThat(result.getFlags()).contains("HIGH_VALUE_COD");
    }

    @Test
    @DisplayName("Should flag suspicious order with multiple indicators")
    void testSuspiciousOrderMultipleFlags() {
        FraudCheckResult result = fraudDetectionService.analyzeOrder(suspiciousOrder);
        assertThat(result.getFraudScore()).isGreaterThan(50.0);
        assertThat(result.getIsSuspicious()).isTrue();
        assertThat(result.getRiskLevel()).isIn(
            FraudCheckResult.RiskLevel.MEDIUM,
            FraudCheckResult.RiskLevel.HIGH
        );
        assertThat(result.getFlags()).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("Should recommend review for high risk orders")
    void testHighRiskRecommendation() {
        FraudCheckResult result = fraudDetectionService.analyzeOrder(suspiciousOrder);
        if (result.getFraudScore() >= 70.0) {
            assertThat(result.getRecommendation()).isEqualTo("REVIEW");
        }
    }

    @Test
    @DisplayName("Should cap fraud score at 100")
    void testFraudScoreCap() {
        FraudCheckResult result = fraudDetectionService.analyzeOrder(suspiciousOrder);
        assertThat(result.getFraudScore()).isLessThanOrEqualTo(100.0);
    }

    private Order createOrder(String orderId, BigDecimal amount, PaymentMethod paymentMethod,
                             int totalOrders, boolean isVerified, LocalDateTime createdAt) {
        Customer customer = Customer.builder()
                .customerId("CUST-001")
                .name("Test Customer")
                .email("test@example.com")
                .phone("+6281234567890")
                .isVerified(isVerified)
                .totalOrders(totalOrders)
                .build();

        Address address = Address.builder()
                .street("Jl. Test No. 123")
                .city("Jakarta")
                .province("DKI Jakarta")
                .postalCode("12345")
                .country("Indonesia")
                .build();

        OrderItem item = OrderItem.builder()
                .productId("P001")
                .productName("Test Product")
                .quantity(2)
                .unitPrice(amount.divide(new BigDecimal("2")))
                .category("GENERAL")
                .build();

        Payment payment = Payment.builder()
                .method(paymentMethod)
                .amount(amount)
                .currency("IDR")
                .isPaid(false)
                .build();

        return Order.builder()
                .orderId(orderId)
                .customer(customer)
                .items(List.of(item))
                .shippingAddress(address)
                .payment(payment)
                .status(OrderStatus.PENDING)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
