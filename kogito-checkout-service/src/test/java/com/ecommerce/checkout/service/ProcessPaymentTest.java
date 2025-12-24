package com.ecommerce.checkout.service;

import com.ecommerce.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Process Payment Handler Tests")
class ProcessPaymentTest {

    private ProcessPayment processPayment;

    @Mock
    private KogitoWorkItem workItem;

    @Mock
    private KogitoWorkItemManager workItemManager;

    @Captor
    private ArgumentCaptor<Map<String, Object>> resultsCaptor;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        processPayment = new ProcessPayment();
        testOrder = createTestOrder(PaymentMethod.CREDIT_CARD);
    }

    private void stubWorkItemWithOrder() {
        when(workItem.getParameter("order")).thenReturn(testOrder);
        when(workItem.getStringId()).thenReturn("workitem-123");
    }

    @Test
    @DisplayName("Should process COD payment successfully")
    void testCODPaymentSuccess() {
        stubWorkItemWithOrder();
        testOrder.getPayment().setMethod(PaymentMethod.COD);

        processPayment.executeWorkItem(workItem, workItemManager);

        verify(workItemManager).completeWorkItem(eq("workitem-123"), resultsCaptor.capture());

        Map<String, Object> results = resultsCaptor.getValue();
        assertThat(results).containsEntry("paymentSuccess", true);

        Order resultOrder = (Order) results.get("order");
        Payment payment = resultOrder.getPayment();
        assertThat(payment.getIsPaid()).isTrue();
        assertThat(payment.getTransactionId()).isNotNull();
    }

    @Test
    @DisplayName("Should process credit card payment")
    void testCreditCardPayment() {
        stubWorkItemWithOrder();
        testOrder.getPayment().setMethod(PaymentMethod.CREDIT_CARD);

        processPayment.executeWorkItem(workItem, workItemManager);

        verify(workItemManager).completeWorkItem(eq("workitem-123"), resultsCaptor.capture());

        Map<String, Object> results = resultsCaptor.getValue();
        assertThat(results.get("paymentSuccess")).isNotNull();

        if ((Boolean) results.get("paymentSuccess")) {
            Order resultOrder = (Order) results.get("order");
            assertThat(resultOrder.getPayment().getTransactionId()).startsWith("TXN-");
        }
    }

    @Test
    @DisplayName("Should process e-wallet payment")
    void testEWalletPayment() {
        stubWorkItemWithOrder();
        testOrder.getPayment().setMethod(PaymentMethod.E_WALLET);

        processPayment.executeWorkItem(workItem, workItemManager);

        verify(workItemManager).completeWorkItem(eq("workitem-123"), resultsCaptor.capture());

        Map<String, Object> results = resultsCaptor.getValue();
        assertThat(results.get("paymentSuccess")).isNotNull();
    }

    @Test
    @DisplayName("Should handle null order gracefully")
    void testNullOrder() {
        when(workItem.getParameter("order")).thenReturn(null);
        when(workItem.getStringId()).thenReturn("workitem-123");

        processPayment.executeWorkItem(workItem, workItemManager);

        verify(workItemManager).completeWorkItem(eq("workitem-123"), resultsCaptor.capture());

        Map<String, Object> results = resultsCaptor.getValue();
        assertThat(results).containsEntry("paymentSuccess", false);
    }

    @Test
    @DisplayName("Should generate transaction ID on success")
    void testTransactionIdGeneration() {
        stubWorkItemWithOrder();
        testOrder.getPayment().setMethod(PaymentMethod.COD);

        processPayment.executeWorkItem(workItem, workItemManager);

        verify(workItemManager).completeWorkItem(eq("workitem-123"), resultsCaptor.capture());

        Order resultOrder = (Order) resultsCaptor.getValue().get("order");
        String transactionId = resultOrder.getPayment().getTransactionId();

        assertThat(transactionId)
                .isNotNull()
                .startsWith("TXN-")
                .hasSize(12);
    }

    @Test
    @DisplayName("Should handle abort work item")
    void testAbortWorkItem() {
        processPayment.abortWorkItem(workItem, workItemManager);
        verify(workItem, atLeastOnce()).getStringId();
    }

    private Order createTestOrder(PaymentMethod paymentMethod) {
        Customer customer = Customer.builder()
                .customerId("CUST-001")
                .name("Test Customer")
                .email("test@example.com")
                .phone("+6281234567890")
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
                .quantity(1)
                .unitPrice(new BigDecimal("500000"))
                .build();

        Payment payment = Payment.builder()
                .method(paymentMethod)
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
