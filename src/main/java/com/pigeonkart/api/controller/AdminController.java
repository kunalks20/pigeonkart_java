package com.pigeonkart.api.controller;

import com.pigeonkart.api.dto.*;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderStatus;
import com.pigeonkart.api.model.Product;
import com.pigeonkart.api.model.ProductCategory;
import com.pigeonkart.api.repository.OrderRepository;
import com.pigeonkart.api.repository.ProductRepository;
import com.pigeonkart.api.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderRepository orderRepository;
    private final AdminAuthService adminAuthService;
    private final ProductRepository productRepository;

    @GetMapping("/orders")
    public List<AdminOrderResponse> getListOfOrders() {
        return orderRepository.findAll().stream()
                .map(AdminOrderResponse::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/orders/{id}")
    public AdminOrderResponse updateStatus(@PathVariable String id, @Valid @RequestBody AdminUpdate request) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
        order.setStatus(OrderStatus.valueOf(request.getStatus()));
        if (request.getRemarks() != null) {
            order.setRemarks(request.getRemarks());
        }
        return new AdminOrderResponse(orderRepository.save(order));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody AdminLoginRequest request) {
        String token = adminAuthService.login(request.getUsername(), request.getPassword());
        return Map.of("token", token);
    }

    @GetMapping("/products")
    public List<Product> getListOfProducts() {
        return productRepository.findAll();
    }

    // Creates a brand-new product. If the id already exists, use PUT instead.
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody AdminProductRequest request) {
        if (productRepository.existsById(request.getId())) {
            throw new IllegalStateException("Product id already exists: " + request.getId());
        }
        Product product = new Product(
                request.getId(),
                request.getName(),
                ProductCategory.valueOf(request.getCategory().toUpperCase()),
                request.getPrice(),
                request.getStock(),
                request.getUnit(),
                request.getDescription()
        );
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/products/bulk")
    @Transactional
    public List<Product> bulkUpdate(@Valid @RequestBody BulkProductUpdateRequest request) {
        List<Product> updated = new ArrayList<>();
        for (AdminProductRequest item : request.getProducts()) {
            Product product = productRepository.findById(item.getId())
                    .orElseThrow(() -> new NoSuchElementException("Product not found: " + item.getId()));
            applyUpdate(product, item);
            updated.add(product);
        }
        return productRepository.saveAll(updated);
    }

    // Updates an existing product — typically just price/stock, but any field
    // can be changed here (e.g. correcting a description or unit size).
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        applyUpdate(product, request);
        return productRepository.save(product);
    }

    @DeleteMapping("products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/session")
    public Map<String, Boolean> session() {
        log.debug("Inside Session api to get session");
        return Map.of("valid", true);
    }

    private void applyUpdate(Product product, AdminProductRequest request) {
        product.setName(request.getName());
        product.setCategory(ProductCategory.valueOf(request.getCategory().toUpperCase()));
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setUnit(request.getUnit());
        product.setDescription(request.getDescription());
    }
}
