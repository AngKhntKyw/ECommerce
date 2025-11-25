package com.example.ecommerce.repository;

import com.example.ecommerce.model.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<UserOrder, Long> {
    List<UserOrder> findByUserId(Long userId);
}