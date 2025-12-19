package com.ecommerce.checkout.service;

import com.ecommerce.models.Order;
import com.ecommerce.models.PaymentMethod;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class ProcessPayment implements KogitoWorkItemHandler {

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing ProcessPayment task");
        try {
            Order order = (Order) workItem.getParameter("order");
            if (order == null) {
                throw new IllegalArgumentException("Order is null");
            }

            log.info("Processing payment for order: {} (Amount: {} {})",
                    order.getOrderId(),
                    order.getPayment().getAmount(),
                    order.getPayment().getCurrency());

            boolean paymentSuccess = processPaymentGateway(order);

            if (paymentSuccess) {
                String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                order.getPayment().setTransactionId(transactionId);
                order.getPayment().setIsPaid(true);

                log.info("Payment successful for order: {} (Transaction: {})",
                        order.getOrderId(), transactionId);
            } else {
                log.warn("Payment failed for order: {}", order.getOrderId());
            }

            Map<String, Object> results = new HashMap<>();
            results.put("paymentSuccess", paymentSuccess);
            results.put("order", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Payment processing failed", e);
            Map<String, Object> results = new HashMap<>();
            results.put("paymentSuccess", false);
            manager.completeWorkItem(workItem.getStringId(), results);
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("ProcessPayment task aborted for workItem: {}", workItem.getStringId());
    }

    private boolean processPaymentGateway(Order order) {
        PaymentMethod method = order.getPayment().getMethod();
        log.info("Simulating payment gateway for method: {}", method);

        switch (method) {
            case COD:
                log.info("COD payment - auto approved");
                return true;
            case CREDIT_CARD, DEBIT_CARD:
                boolean cardSuccess = Math.random() < 0.95;
                log.info("Card payment result: {}", cardSuccess ? "SUCCESS" : "FAILED");
                return cardSuccess;

            case E_WALLET:
                boolean walletSuccess = Math.random() < 0.98;
                log.info("E-Wallet payment result: {}", walletSuccess ? "SUCCESS" : "FAILED");
                return walletSuccess;

            case BANK_TRANSFER:
                log.info("Bank transfer - pending verification (auto approved for demo)");
                return true;

            default:
                log.warn("Unknown payment method: {}", method);
                return false;
        }
    }
}
