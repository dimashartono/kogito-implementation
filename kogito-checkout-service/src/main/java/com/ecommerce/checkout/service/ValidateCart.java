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
public class ValidateCart implements KogitoWorkItemHandler {

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing ValidateCart task");
        try {
            Order order = (Order) workItem.getParameter("order");

            if (order == null) {
                throw new IllegalArgumentException("Order is null");
            }

            log.info("Validating cart for order: {}", order.getOrderId());

            if (order.getItems() == null || order.getItems().isEmpty()) {
                throw new IllegalStateException("Cart is empty");
            }

            for (OrderItem item : order.getItems()) {
                boolean stockAvailable = checkStockAvailability(item);
                if (!stockAvailable) {
                    throw new IllegalStateException(
                        String.format("Product %s is out of stock", item.getProductName())
                    );
                }

                if (item.getQuantity() > 100) {
                    throw new IllegalStateException(
                        String.format("Quantity for %s exceeds maximum limit of 100", item.getProductName())
                    );
                }
            }

            log.info("Cart validation successful for order: {}", order.getOrderId());

            Map<String, Object> results = new HashMap<>();
            results.put("cartValid", true);
            results.put("validatedOrder", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Cart validation failed", e);
            manager.abortWorkItem(workItem.getStringId());
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("ValidateCart task aborted for workItem: {}", workItem.getStringId());
    }

    private boolean checkStockAvailability(OrderItem item) {
        log.debug("Checking stock for product: {} (SKU: {})", item.getProductName(), item.getSku());
        return true;
    }
}
