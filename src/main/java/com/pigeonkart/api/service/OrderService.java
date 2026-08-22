package com.pigeonkart.api.service;

import com.pigeonkart.api.dto.OrderRequest;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderItem;
import com.pigeonkart.api.model.Product;
import com.pigeonkart.api.repository.OrderRepository;
import com.pigeonkart.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class OrderService {

    // Same ceiling enforced client-side (see CartContext.jsx) — kept here too
    // since client-side limits are only a UX nicety, not real enforcement.
    private static final int MAX_QTY_PER_ITEM = 10;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

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

        int total = 0;
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
            total += product.getPrice() * item.getQty();
        }
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public CustomerOrder getOrder(String id) {
        return orderRepository.findById(id)
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
}
