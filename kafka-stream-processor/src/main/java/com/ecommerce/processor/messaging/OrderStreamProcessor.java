package com.ecommerce.processor.messaging;

import com.ecommerce.models.FraudCheckResult;
import com.ecommerce.models.Order;
import com.ecommerce.models.OrderEvent;
import com.ecommerce.models.OrderStatus;
import com.ecommerce.processor.service.FraudDetectionService;
import com.ecommerce.processor.service.OrderEnrichmentService;
import com.ecommerce.processor.service.OrderPersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@ApplicationScoped
public class OrderStreamProcessor {

    @Inject
    OrderEnrichmentService enrichmentService;

    @Inject
    FraudDetectionService fraudDetectionService;

    @Inject
    OrderPersistenceService persistenceService;

    @Inject
    @Channel("fraud-alerts")
    Emitter<String> fraudAlertsEmitter;

    private final ObjectMapper objectMapper;

    public OrderStreamProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Incoming("raw-orders")
    @Outgoing("validated-orders")
    public String processOrder(String orderJson) {
        try {
            log.info("Received order message from Kafka");
            Order order = objectMapper.readValue(orderJson, Order.class);
            log.info("Processing order: {}", order.getOrderId());

            if (!enrichmentService.isValid(order)) {
                log.error("Invalid order received: {}", order.getOrderId());
                return null;
            }

            order = enrichmentService.enrichOrder(order);
            FraudCheckResult fraudResult = fraudDetectionService.analyzeOrder(order);
            order.setFraudScore(fraudResult.getFraudScore());
            
            if (Boolean.TRUE.equals(fraudResult.getIsSuspicious())) {
                order.setStatus(OrderStatus.FRAUD_SUSPECTED);
                log.warn("Suspicious order detected: {} (Score: {})",
                        order.getOrderId(), fraudResult.getFraudScore());

                persistenceService.saveFraudAlert(fraudResult);
                publishFraudAlert(fraudResult);
            } else {
                order.setStatus(OrderStatus.VALIDATED);
            }

            persistenceService.saveOrderAuditLog(order);
            OrderEvent validatedEvent = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("ORDER_VALIDATED")
                    .timestamp(LocalDateTime.now())
                    .sourceService("kafka-stream-processor")
                    .order(order)
                    .build();

            String validatedJson = objectMapper.writeValueAsString(validatedEvent);
            log.info("Order {} processed successfully. Status: {}, Fraud Score: {}",
                    order.getOrderId(),
                    order.getStatus(),
                    order.getFraudScore());

            return validatedJson;

        } catch (Exception e) {
            log.error("Error processing order", e);
            return null;
        }
    }

    private void publishFraudAlert(FraudCheckResult fraudResult) {
        try {
            String fraudJson = objectMapper.writeValueAsString(fraudResult);
            log.info("Publishing fraud alert for order: {}", fraudResult.getOrderId());
            fraudAlertsEmitter.send(fraudJson);
        } catch (Exception e) {
            log.error("Error publishing fraud alert for order: {}", fraudResult.getOrderId(), e);
        }
    }
}
