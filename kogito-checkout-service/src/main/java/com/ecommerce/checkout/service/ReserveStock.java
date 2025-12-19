package com.ecommerce.checkout.service;

import com.ecommerce.models.Order;
import com.ecommerce.models.OrderItem;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class ReserveStock implements KogitoWorkItemHandler {

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing ReserveStock task");
        try {
            Order order = (Order) workItem.getParameter("order");
            log.info("Reserving stock for order: {}", order.getOrderId());

            for (OrderItem item : order.getItems()) {
                reserveInventory(item);
            }

            log.info("Stock reserved successfully for order: {}", order.getOrderId());

            Map<String, Object> results = new HashMap<>();
            results.put("stockReserved", true);
            results.put("order", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Stock reservation failed", e);
            manager.abortWorkItem(workItem.getStringId());
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("ReserveStock task aborted for workItem: {}", workItem.getStringId());
    }

    private void reserveInventory(OrderItem item) {
        log.debug("Reserving {} units of product: {} (SKU: {})",
                item.getQuantity(), item.getProductName(), item.getSku());
    }
}
