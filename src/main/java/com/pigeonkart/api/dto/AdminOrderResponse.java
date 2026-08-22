package com.pigeonkart.api.dto;

import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderItem;
import com.pigeonkart.api.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class AdminOrderResponse {
    private Long id;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private int totalAmount;
    private OrderStatus status;
    private String remarks;
    private Instant createdAt;
    private List<Item> items;

    public AdminOrderResponse(CustomerOrder order) {
        this.id = order.getId();
        this.customerName = order.getCustomerName();
        this.customerPhone = order.getCustomerPhone();
        this.customerAddress = order.getCustomerAddress();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.remarks = order.getRemarks();
        this.createdAt = order.getCreatedAt();
        this.items = order.getItems().stream()
                .map(Item::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class Item {
        private String productName;
        private int qty;
        private int unitPrice;

        public Item(OrderItem item) {
            this.productName = item.getProductName();
            this.qty = item.getQty();
            this.unitPrice = item.getUnitPrice();
        }
    }
}