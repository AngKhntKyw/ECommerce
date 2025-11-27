package com.example.ecommerce.repository;

import com.example.ecommerce.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByIsActiveTrue();

    // Added to look up payment method during checkout
    Optional<PaymentMethod> findByName(String name);
}