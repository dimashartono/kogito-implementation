package com.ecommerce.checkout.service;

import com.ecommerce.models.Order;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class CalculateTotal implements KogitoWorkItemHandler {

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.info("Executing CalculateTotal task");
        try {
            Order order = (Order) workItem.getParameter("order");
            if (order == null) {
                throw new IllegalArgumentException("Order is null");
            }

            log.info("Calculating total for order: {}", order.getOrderId());
            BigDecimal subtotal = order.getSubtotal();
            log.debug("Subtotal: {}", subtotal);

            BigDecimal shippingCost = calculateShippingCost(order);
            order.setShippingCost(shippingCost);
            log.debug("Shipping cost: {}", shippingCost);

            if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
                BigDecimal voucherDiscount = applyVoucher(order.getVoucherCode(), subtotal);
                order.setVoucherDiscount(voucherDiscount);
                log.debug("Voucher discount: {}", voucherDiscount);
            }

            BigDecimal grandTotal = order.getGrandTotal();
            log.info("Grand total calculated: {}", grandTotal);
            order.getPayment().setAmount(grandTotal);

            Map<String, Object> results = new HashMap<>();
            results.put("totalCalculated", true);
            results.put("grandTotal", grandTotal);
            results.put("order", order);

            manager.completeWorkItem(workItem.getStringId(), results);

        } catch (Exception e) {
            log.error("Total calculation failed", e);
            manager.abortWorkItem(workItem.getStringId());
        }
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        log.warn("CalculateTotal task aborted for workItem: {}", workItem.getStringId());
    }

    private BigDecimal calculateShippingCost(Order order) {
        int totalWeight = order.getItems().stream()
                .mapToInt(item -> item.getWeightGrams() != null ?
                    item.getWeightGrams() * item.getQuantity() : 500 * item.getQuantity())
                .sum();

        BigDecimal costPerKg = new BigDecimal("10000");
        BigDecimal weightInKg = new BigDecimal(totalWeight).divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

        return costPerKg.multiply(weightInKg).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal applyVoucher(String voucherCode, BigDecimal subtotal) {
        log.info("Applying voucher: {}", voucherCode);

        Map<String, BigDecimal> voucherRates = Map.of(
            "WELCOME10", new BigDecimal("0.10"), 
            "SAVE20", new BigDecimal("0.20"),    
            "NEWYEAR", new BigDecimal("0.15")    
        );

        BigDecimal discountRate = voucherRates.getOrDefault(voucherCode, BigDecimal.ZERO);
        return subtotal.multiply(discountRate).setScale(0, RoundingMode.HALF_UP);
    }
}
