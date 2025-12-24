package com.ecommerce.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order Model Tests")
class OrderTest {

    private Order order;
    private List<OrderItem> items;

    @BeforeEach
    void setUp() {
        OrderItem item1 = OrderItem.builder()
                .productId("P001")
                .productName("Product 1")
                .quantity(2)
                .unitPrice(new BigDecimal("100000"))
                .discountPercent(BigDecimal.TEN)
                .taxPercent(new BigDecimal("11"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .productId("P002")
                .productName("Product 2")
                .quantity(1)
                .unitPrice(new BigDecimal("200000"))
                .discountPercent(BigDecimal.ZERO)
                .taxPercent(new BigDecimal("11"))
                .build();

        items = Arrays.asList(item1, item2);

        Customer customer = Customer.builder()
                .customerId("CUST-001")
                .name("Test Customer")
                .email("test@example.com")
                .phone("+6281234567890")
                .isVerified(true)
                .totalOrders(0)
                .build();

        Address address = Address.builder()
                .street("Jl. Test No. 123")
                .city("Jakarta")
                .province("DKI Jakarta")
                .postalCode("12345")
                .country("Indonesia")
                .build();

        Payment payment = Payment.builder()
                .method(PaymentMethod.CREDIT_CARD)
                .amount(new BigDecimal("500000"))
                .currency("IDR")
                .isPaid(false)
                .build();

        order = Order.builder()
                .orderId("ORD-001")
                .customer(customer)
                .items(items)
                .shippingAddress(address)
                .payment(payment)
                .status(OrderStatus.PENDING)
                .shippingCost(new BigDecimal("25000"))
                .voucherDiscount(new BigDecimal("50000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should calculate total items correctly")
    void testGetTotalItems() {
        int totalItems = order.getTotalItems();

        assertThat(totalItems).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Should calculate subtotal correctly")
    void testGetSubtotal() {
        BigDecimal subtotal = order.getSubtotal();

        // Item 1: (100000 * 2) - 10% discount + 11% tax = 200000 - 20000 + 19800 = 199800
        // Item 2: (200000 * 1) - 0% discount + 11% tax = 200000 + 22000 = 222000
        // Total: 421800
        assertThat(subtotal).isEqualByComparingTo(new BigDecimal("421800"));
    }

    @Test
    @DisplayName("Should calculate grand total correctly")
    void testGetGrandTotal() {
        BigDecimal grandTotal = order.getGrandTotal();

        // Subtotal: 421800
        // + Shipping: 25000
        // - Voucher: 50000
        // = 396800
        assertThat(grandTotal).isEqualByComparingTo(new BigDecimal("396800"));
    }

    @Test
    @DisplayName("Should identify final state orders")
    void testIsFinalState() {
        order.setStatus(OrderStatus.PENDING);
        assertThat(order.isFinalState()).isFalse();

        order.setStatus(OrderStatus.PROCESSING);
        assertThat(order.isFinalState()).isFalse();

        order.setStatus(OrderStatus.DELIVERED);
        assertThat(order.isFinalState()).isTrue();

        order.setStatus(OrderStatus.CANCELLED);
        assertThat(order.isFinalState()).isTrue();

        order.setStatus(OrderStatus.REFUNDED);
        assertThat(order.isFinalState()).isTrue();
    }

    @Test
    @DisplayName("Should identify suspicious orders")
    void testIsSuspicious() {
        // Not suspicious
        order.setFraudScore(50.0);
        assertThat(order.isSuspicious()).isFalse();

        // Suspicious
        order.setFraudScore(75.0);
        assertThat(order.isSuspicious()).isTrue();

        // Null fraud score
        order.setFraudScore(null);
        assertThat(order.isSuspicious()).isFalse();
    }

    @Test
    @DisplayName("Should handle orders with no voucher discount")
    void testNoVoucherDiscount() {
        order.setVoucherDiscount(BigDecimal.ZERO);

        BigDecimal grandTotal = order.getGrandTotal();

        // Subtotal: 421800 + Shipping: 25000 - Voucher: 0 = 446800
        assertThat(grandTotal).isEqualByComparingTo(new BigDecimal("446800"));
    }

    @Test
    @DisplayName("Should handle orders with no shipping cost")
    void testNoShippingCost() {
        order.setShippingCost(BigDecimal.ZERO);

        BigDecimal grandTotal = order.getGrandTotal();

        // Subtotal: 421800 + Shipping: 0 - Voucher: 50000 = 371800
        assertThat(grandTotal).isEqualByComparingTo(new BigDecimal("371800"));
    }
}
