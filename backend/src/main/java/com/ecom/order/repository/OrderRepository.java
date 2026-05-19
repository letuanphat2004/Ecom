package com.ecom.order.repository;

import com.ecom.order.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
}
