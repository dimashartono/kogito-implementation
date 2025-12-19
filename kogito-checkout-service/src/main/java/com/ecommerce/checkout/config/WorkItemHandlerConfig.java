package com.ecommerce.checkout.config;

import com.ecommerce.checkout.service.*;
import io.quarkus.arc.Unremovable;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;


@Slf4j
@ApplicationScoped
public class WorkItemHandlerConfig {

    @Inject
    ValidateCart validateCart;

    @Inject
    CalculateTotal calculateTotal;

    @Inject
    ProcessPayment processPayment;

    @Inject
    ReserveStock reserveStock;

    @Inject
    CreateOrder createOrder;

    @Inject
    SendNotification sendNotification;

    @Inject
    PublishOrderToKafka publishOrderToKafka;

    @Produces
    @Unremovable
    @Named("ValidateCart")
    public KogitoWorkItemHandler validateCartHandler() {
        log.info("Registering ValidateCart work item handler");
        return validateCart;
    }

    @Produces
    @Unremovable
    @Named("CalculateTotal")
    public KogitoWorkItemHandler calculateTotalHandler() {
        log.info("Registering CalculateTotal work item handler");
        return calculateTotal;
    }

    @Produces
    @Unremovable
    @Named("ProcessPayment")
    public KogitoWorkItemHandler processPaymentHandler() {
        log.info("Registering ProcessPayment work item handler");
        return processPayment;
    }

    @Produces
    @Unremovable
    @Named("ReserveStock")
    public KogitoWorkItemHandler reserveStockHandler() {
        log.info("Registering ReserveStock work item handler");
        return reserveStock;
    }

    @Produces
    @Unremovable
    @Named("CreateOrder")
    public KogitoWorkItemHandler createOrderHandler() {
        log.info("Registering CreateOrder work item handler");
        return createOrder;
    }

    @Produces
    @Unremovable
    @Named("SendNotification")
    public KogitoWorkItemHandler sendNotificationHandler() {
        log.info("Registering SendNotification work item handler");
        return sendNotification;
    }

    @Produces
    @Unremovable
    @Named("PublishOrderToKafka")
    public KogitoWorkItemHandler publishKafkaHandler() {
        log.info("Registering PublishOrderToKafka work item handler");
        return publishOrderToKafka;
    }
}
