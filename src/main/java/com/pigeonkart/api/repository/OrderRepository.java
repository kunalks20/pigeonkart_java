package com.pigeonkart.api.repository;

import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByRazorpayPaymentId(String razorpayPaymentId);

    List<CustomerOrder> findByStatusNotIn(Collection<OrderStatus> statuses);
}
