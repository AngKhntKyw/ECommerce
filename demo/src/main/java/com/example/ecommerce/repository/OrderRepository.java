package com.example.ecommerce.repository;

import com.example.ecommerce.model.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<UserOrder, Long> {
    List<UserOrder> findByUserId(Long userId);
}