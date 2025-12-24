package com.ecommerce.checkout.service;

import com.ecommerce.models.Order;
import com.ecommerce.models.OrderStatus;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@ApplicationScoped
public class CreateOrder implements KogitoWorkItemHandler {

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing CreateOrder task");
        try {
            Order order = (Order) workItem.getParameter("order");
            log.info("Creating order: {}", order.getOrderId());

            if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
                order.setOrderId(orderId);
                log.info("Generated order ID: {}", orderId);
            }

            order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
            if (order.getCreatedAt() == null) {
                order.setCreatedAt(LocalDateTime.now());
            }
            order.setUpdatedAt(LocalDateTime.now());

            log.info("Order created successfully: {} (Status: {}, Total: {} {})",
                    order.getOrderId(),
                    order.getStatus(),
                    order.getGrandTotal(),
                    order.getPayment().getCurrency());

            Map<String, Object> results = new HashMap<>();
            results.put("orderCreated", true);
            results.put("orderId", order.getOrderId());
            results.put("order", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Order creation failed", e);
            manager.abortWorkItem(workItem.getStringId());
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("CreateOrder task aborted for workItem: {}", workItem.getStringId());
    }
}
