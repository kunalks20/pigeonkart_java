package com.pigeonkart.api.service;

import com.pigeonkart.api.dto.CouponApplyRequest;
import com.pigeonkart.api.dto.OrderRequest;
import com.pigeonkart.api.model.Coupon;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderItem;
import com.pigeonkart.api.model.Product;
import com.pigeonkart.api.repository.CouponRepo;
import com.pigeonkart.api.repository.OrderRepository;
import com.pigeonkart.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {

    // Same ceiling enforced client-side (see CartContext.jsx) — kept here too
    // since client-side limits are only a UX nicety, not real enforcement.
    private static final int MAX_QTY_PER_ITEM = 20;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepo couponRepo;
    /**
     * Creates an order in PENDING_PAYMENT status. Stock is validated here but only
     * decremented once payment is verified (see PaymentService.verify), so an
     * abandoned checkout doesn't permanently lock stock.
     */
    @Transactional
    public CustomerOrder createOrder(OrderRequest request) {
        CustomerOrder order = new CustomerOrder(
                request.getCustomer().getName(),
                request.getCustomer().getPhone(),
                request.getCustomer().getAddress()
        );

        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = requireActiveCoupon(request.getCouponCode());
        }

        int subtotal = 0;
        int discount = 0;
        boolean hasEligibleCouponItem = false;

        for (OrderRequest.Item item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Unknown product: " + item.getProductId()));

            if (item.getQty() < 1) {
                throw new IllegalStateException("Invalid quantity for " + product.getName());
            }
            if (item.getQty() > MAX_QTY_PER_ITEM) {
                throw new IllegalStateException("Maximum " + MAX_QTY_PER_ITEM + " per item — reduce quantity for " + product.getName());
            }
            if (item.getQty() > product.getStock()) {
                throw new IllegalStateException("Only " + product.getStock() + " left in stock for " + product.getName());
            }

            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), item.getQty()));
            subtotal+= product.getPrice() * item.getQty();

            if (coupon != null) {
                hasEligibleCouponItem |= coupon.matches(product.getCategory(), product.getUnit());
                int effectiveUnitPrice = effectiveUnitPrice(coupon, product);
                discount += (product.getPrice() - effectiveUnitPrice) * item.getQty();
            }
        }

        if (coupon != null && !hasEligibleCouponItem) {
            throw new CouponNotApplicableException(couponEligibilityMessage(coupon));
        }

        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal - discount);
        return orderRepository.save(order);
    }

    public CustomerOrder getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    public CustomerOrder getOrderForUpdate(Long id) {
        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
    }

    @Transactional
    public void applyStockAndSave(CustomerOrder order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Unknown product: " + item.getProductId()));
            product.decreaseStock(item.getQty());
            productRepository.save(product);
        }
        orderRepository.save(order);
    }

    public Coupon requireActiveCoupon(String code) {
        Coupon coupon = couponRepo.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new NoSuchElementException("Unknown coupon code: " + code));
        if (!coupon.isActive()) {
            throw new IllegalStateException("Coupon is no longer active: " + code);
        }
        return coupon;
    }

    public Coupon validateCouponForItems(String code, List<CouponApplyRequest.Item> items) {
        Coupon coupon = requireActiveCoupon(code);
        boolean hasEligibleItem = items.stream().anyMatch(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Unknown product: " + item.getProductId()));
            return coupon.matches(product.getCategory(), product.getUnit());
        });

        if (!hasEligibleItem) {
            throw new CouponNotApplicableException(couponEligibilityMessage(coupon));
        }
        return coupon;
    }

    private String couponEligibilityMessage(Coupon coupon) {
        List<String> requirements = new ArrayList<>();
        if (coupon.getScopeCategory() != null) {
            requirements.add(coupon.getScopeCategory() + " items");
        }
        if (coupon.getScopeUnitContains() != null && !coupon.getScopeUnitContains().isBlank()) {
            requirements.add("products with a unit containing \"" + coupon.getScopeUnitContains() + "\"");
        }

        if (requirements.isEmpty()) {
            return "Sorry, your cart doesn't have any items eligible for this coupon.";
        }
        return "Sorry, your cart doesn't have any eligible items. This coupon is only applicable to "
                + String.join(" and ", requirements) + ".";
    }

    public int effectiveUnitPrice(Coupon coupon, Product product) {
        if (coupon == null || !coupon.matches(product.getCategory(), product.getUnit())) {
            return product.getPrice();
        }
        return coupon.discountedUnitPrice(product.getPrice());
    }
}
