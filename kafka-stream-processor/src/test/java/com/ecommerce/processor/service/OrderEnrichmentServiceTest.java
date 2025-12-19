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
@DisplayName("Order Enrichment Service Tests")
class OrderEnrichmentServiceTest {

    @InjectMocks
    private OrderEnrichmentService enrichmentService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = createTestOrder();
    }

    @Test
    @DisplayName("Should enrich order successfully")
    void testEnrichOrder() {
        LocalDateTime beforeEnrichment = LocalDateTime.now();

        Order enriched = enrichmentService.enrichOrder(testOrder);
        assertThat(enriched).isNotNull();
        assertThat(enriched.getUpdatedAt()).isAfterOrEqualTo(beforeEnrichment);
    }

    @Test
    @DisplayName("Should set default source if missing")
    void testSetDefaultSource() {
        testOrder.setSource(null);

        Order enriched = enrichmentService.enrichOrder(testOrder);
        assertThat(enriched.getSource()).isEqualTo("WEB");
    }

    @Test
    @DisplayName("Should update status when payment is confirmed")
    void testUpdateStatusOnPayment() {
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.getPayment().setIsPaid(true);

        Order enriched = enrichmentService.enrichOrder(testOrder);
        assertThat(enriched.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
    }

    @Test
    @DisplayName("Should adjust payment amount if mismatch")
    void testPaymentAmountMismatch() {
        BigDecimal correctTotal = testOrder.getGrandTotal();
        testOrder.getPayment().setAmount(new BigDecimal("999999")); // wrong amount

        Order enriched = enrichmentService.enrichOrder(testOrder);
        assertThat(enriched.getPayment().getAmount()).isEqualTo(correctTotal);
    }

    @Test
    @DisplayName("Should validate order successfully")
    void testValidOrder() {
        boolean isValid = enrichmentService.isValid(testOrder);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject null order")
    void testNullOrder() {
        boolean isValid = enrichmentService.isValid(null);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject order without ID")
    void testOrderWithoutId() {
        testOrder.setOrderId(null);

        boolean isValid = enrichmentService.isValid(testOrder);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject order without items")
    void testOrderWithoutItems() {
        testOrder.setItems(List.of());

        boolean isValid = enrichmentService.isValid(testOrder);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject order with invalid total")
    void testOrderWithInvalidTotal() {
        testOrder.getItems().get(0).setUnitPrice(BigDecimal.ZERO);

        boolean isValid = enrichmentService.isValid(testOrder);
        assertThat(isValid).isFalse();
    }

    private Order createTestOrder() {
        Customer customer = Customer.builder()
                .customerId("CUST-001")
                .name("Test Customer")
                .email("test@example.com")
                .phone("+6281234567890")
                .isVerified(true)
                .totalOrders(5)
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
                .unitPrice(new BigDecimal("250000"))
                .build();

        Payment payment = Payment.builder()
                .method(PaymentMethod.CREDIT_CARD)
                .amount(new BigDecimal("500000"))
                .currency("IDR")
                .isPaid(false)
                .build();

        return Order.builder()
                .orderId("ORD-TEST-001")
                .customer(customer)
                .items(List.of(item))
                .shippingAddress(address)
                .payment(payment)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
