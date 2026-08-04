package com.pigeonkart.api.repository;

import com.pigeonkart.api.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, String> {
}
