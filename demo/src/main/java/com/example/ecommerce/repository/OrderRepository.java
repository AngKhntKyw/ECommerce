package com.example.ecommerce.repository;

import com.example.ecommerce.model.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<UserOrder, Long> {

    // Existing method (kept for compatibility)
    List<UserOrder> findByUserId(Long userId);

    // NEW: Fetch all orders sorted by ID Descending (Latest first) -> For Admin
    List<UserOrder> findAllByOrderByIdDesc();

    // NEW: Fetch specific user's orders sorted by ID Descending (Latest first) -> For User
    List<UserOrder> findByUserIdOrderByIdDesc(Long userId);
}