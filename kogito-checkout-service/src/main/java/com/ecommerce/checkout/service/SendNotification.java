package com.ecommerce.checkout.service;

import com.ecommerce.models.Order;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class SendNotification implements KogitoWorkItemHandler {

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing SendNotification task");
        try {
            Order order = (Order) workItem.getParameter("order");
            log.info("Sending notifications for order: {}", order.getOrderId());

            sendEmailNotification(order);

            if (order.getCustomer().getPhone() != null) {
                sendSmsNotification(order);
            }

            log.info("Notifications sent successfully for order: {}", order.getOrderId());

            Map<String, Object> results = new HashMap<>();
            results.put("notificationSent", true);
            results.put("order", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Notification sending failed", e);
            Map<String, Object> results = new HashMap<>();
            results.put("notificationSent", false);
            manager.completeWorkItem(workItem.getStringId(), results);
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("SendNotification task aborted for workItem: {}", workItem.getStringId());
    }

    private void sendEmailNotification(Order order) {
        log.info("Sending email to {} for order {}",
                order.getCustomer().getEmail(),
                order.getOrderId());

        String emailBody = String.format(
            "Dear %s,%n%nYour order %s has been confirmed!%n%nTotal Amount: %s %s%n%nThank you for shopping with us!",
            order.getCustomer().getName(),
            order.getOrderId(),
            order.getGrandTotal(),
            order.getPayment().getCurrency()
        );

        log.debug("Email content: {}", emailBody);
    }

    private void sendSmsNotification(Order order) {
        log.info("Sending SMS to {} for order {}",
                order.getCustomer().getPhone(),
                order.getOrderId());

        String smsBody = String.format(
            "Order %s confirmed! Total: %s %s. Thank you!",
            order.getOrderId(),
            order.getGrandTotal(),
            order.getPayment().getCurrency()
        );

        log.debug("SMS content: {}", smsBody);
    }
}
