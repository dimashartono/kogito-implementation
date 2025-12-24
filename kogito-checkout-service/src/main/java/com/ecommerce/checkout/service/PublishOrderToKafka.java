package com.ecommerce.checkout.service;

import com.ecommerce.models.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@ApplicationScoped
public class PublishOrderToKafka implements KogitoWorkItemHandler {

    @Inject
    @Channel("raw-orders-out")
    Emitter<String> kafkaEmitter;

    private final ObjectMapper objectMapper;

    public PublishOrderToKafka() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing PublishOrderToKafka task");
        try {
            Order order = (Order) workItem.getParameter("order");
            log.info("Publishing order to Kafka: {}", order.getOrderId());
            String orderJson = objectMapper.writeValueAsString(order);

            kafkaEmitter.send(orderJson);

            log.info("Order {} published to Kafka topic: raw-orders", order.getOrderId());

            Map<String, Object> results = new HashMap<>();
            results.put("kafkaPublished", true);
            results.put("order", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Failed to publish order to Kafka", e);
            Map<String, Object> results = new HashMap<>();
            results.put("kafkaPublished", false);
            manager.completeWorkItem(workItem.getStringId(), results);
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("PublishOrderToKafka task aborted for workItem: {}", workItem.getStringId());
    }
}
