package com.ecommerce.processor.service;

import com.ecommerce.models.Order;
import com.ecommerce.models.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


@Slf4j
@ApplicationScoped
public class OrderEnrichmentService {

    public Order enrichOrder(Order order) {
        log.debug("Enriching order: {}", order.getOrderId());
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getSource() == null || order.getSource().isEmpty()) {
            order.setSource("WEB");
        }

        if (order.getStatus() == OrderStatus.PENDING && Boolean.TRUE.equals(order.getPayment().getIsPaid())) {
            order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        }

        if (order.getPayment().getAmount().compareTo(order.getGrandTotal()) != 0) {
            log.warn("Payment amount mismatch for order {}: payment={}, total={}",
                    order.getOrderId(),
                    order.getPayment().getAmount(),
                    order.getGrandTotal());
            order.getPayment().setAmount(order.getGrandTotal());
        }

        log.info("Order {} enriched successfully. Status: {}, Total Items: {}, Grand Total: {}",
                order.getOrderId(),
                order.getStatus(),
                order.getTotalItems(),
                order.getGrandTotal());

        return order;
    }

    public boolean isValid(Order order) {
        if (order == null) {
            log.error("Order is null");
            return false;
        }

        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            log.error("Order ID is missing");
            return false;
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            log.error("Order {} has no items", order.getOrderId());
            return false;
        }

        if (order.getGrandTotal().signum() <= 0) {
            log.error("Order {} has invalid grand total: {}", order.getOrderId(), order.getGrandTotal());
            return false;
        }

        return true;
    }
}
