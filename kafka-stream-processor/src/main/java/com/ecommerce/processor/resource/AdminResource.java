package com.ecommerce.processor.resource;

import com.ecommerce.processor.entity.FraudAlert;
import com.ecommerce.processor.entity.OrderAuditLog;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Admin", description = "Admin operations for monitoring and management")
public class AdminResource {

    @GET
    @Path("/stats")
    @Operation(
        summary = "Get processing statistics", 
        description = "Returns order processing and fraud detection statistics")
    public Response getStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = PanacheEntityBase.count();
        long suspiciousOrders = OrderAuditLog.countSuspiciousOrders();
        long unreviewedAlerts = FraudAlert.countUnreviewed();

        stats.put("total_orders_processed", totalOrders);
        stats.put("suspicious_orders", suspiciousOrders);
        stats.put("unreviewed_fraud_alerts", unreviewedAlerts);
        stats.put("fraud_detection_rate", totalOrders > 0 ?
                (double) suspiciousOrders / totalOrders * 100 : 0.0);

        log.info("Stats retrieved: {}", stats);
        return Response.ok(stats).build();
    }

    @GET
    @Path("/orders/recent")
    @Operation(
        summary = "Get recent orders", 
        description = "Returns the most recent processed orders")
    public Response getRecentOrders(@QueryParam("limit") @DefaultValue("10") int limit) {
        List<OrderAuditLog> recentOrders = PanacheEntityBase.find(
                "ORDER BY processedAt DESC"
        ).page(0, limit).list();
        return Response.ok(recentOrders).build();
    }

    @GET
    @Path("/orders/{orderId}")
    @Operation(summary = "Get order by ID", description = "Returns order details by order ID")
    public Response getOrderById(@PathParam("orderId") String orderId) {
        OrderAuditLog order = PanacheEntityBase.find("orderId", orderId).firstResult();

        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Order not found"))
                    .build();
        }

        return Response.ok(order).build();
    }

    @GET
    @Path("/fraud-alerts")
    @Operation(
        summary = "Get fraud alerts", 
        description = "Returns fraud alerts, optionally filtered by review status")
    public Response getFraudAlerts(
            @QueryParam("reviewed") Boolean reviewed,
            @QueryParam("limit") @DefaultValue("20") int limit) {

        List<FraudAlert> alerts;

        if (reviewed != null) {
            alerts = PanacheEntityBase.find("reviewed = ?1 ORDER BY createdAt DESC", reviewed)
                    .page(0, limit).list();
        } else {
            alerts = PanacheEntityBase.find("ORDER BY createdAt DESC")
                    .page(0, limit).list();
        }

        return Response.ok(alerts).build();
    }

    @GET
    @Path("/health-detailed")
    @Operation(summary = "Detailed health check", description = "Returns detailed health information")
    public Response getDetailedHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            long orderCount = PanacheEntityBase.count();
            health.put("database", "UP");
            health.put("order_count", orderCount);
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("error", e.getMessage());
        }

        health.put("service", "kafka-stream-processor");
        health.put("status", "RUNNING");

        return Response.ok(health).build();
    }
}
