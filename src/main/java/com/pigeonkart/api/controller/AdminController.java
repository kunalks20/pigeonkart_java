package com.pigeonkart.api.controller;

import com.pigeonkart.api.dto.*;
import com.pigeonkart.api.model.*;
import com.pigeonkart.api.repository.CouponRepo;
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
    private final CouponRepo couponRepo;

    @GetMapping("/orders")
    public List<AdminOrderResponse> getListOfOrders() {
        return orderRepository.findAll().stream()
                .map(AdminOrderResponse::new)
                .toList();
    }

    @PutMapping("/orders/{id}")
    public AdminOrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody AdminUpdate request) {
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
    @PostMapping("/products/add-product")
    public ResponseEntity<Product> create(@Valid @RequestBody AdminProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setCategory(ProductCategory.valueOf(request.getCategory()));
        product.setUnit(request.getUnit());
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/products/bulk")
    @Transactional
    public List<Product> bulkUpdate(@Valid @RequestBody List<AdminProductRequest> products) {
        List<Product> updated = new ArrayList<>();
        for (AdminProductRequest item : products) {
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


    @GetMapping("/coupons")
    public List<Coupon> list() {
        return couponRepo.findAll();
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> create(@Valid @RequestBody AdminCouponRequest request) {
        if (couponRepo.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new IllegalStateException("Coupon code already exists: " + request.getCode());
        }
        Coupon coupon = new Coupon(
                null,
                request.getCode().toUpperCase(),
                request.getDescription(),
                DiscountType.valueOf(request.getDiscountType().toUpperCase()),
                request.getDiscountValue(),
                parseCategory(request.getScopeCategory()),
                blankToNull(request.getScopeUnitContains()),
                request.isActive()
        );
        return ResponseEntity.ok(couponRepo.save(coupon));
    }

    @PutMapping("/coupons/{code}")
    public Coupon update(@PathVariable String code, @Valid @RequestBody AdminCouponRequest request) {
        Coupon coupon = couponRepo.findByCodeIgnoreCase(request.getCode())
                .orElseThrow(() -> new NoSuchElementException("Coupon not found: " + code));
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setScopeCategory(parseCategory(request.getScopeCategory()));
        coupon.setScopeUnitContains(blankToNull(request.getScopeUnitContains()));
        coupon.setActive(request.isActive());
        return couponRepo.save(coupon);
    }

    @DeleteMapping("/coupons/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        Coupon coupon = couponRepo.findByCodeIgnoreCase(code).orElseThrow(
                () -> new NoSuchElementException("Coupon not found: " + code)
        );
        couponRepo.delete(coupon);
        return ResponseEntity.ok().build();
    }

    private ProductCategory parseCategory(String value) {
        return (value == null || value.isBlank()) ? null : ProductCategory.valueOf(value.toUpperCase());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
