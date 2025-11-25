package com.example.ecommerce.service;

import com.example.ecommerce.model.PaymentMethod;
import com.example.ecommerce.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentMethodRepository repo;

    public List<PaymentMethod> getAllPaymentMethods() {
        return repo.findAll();
    }

    // For Checkout Page
    public List<PaymentMethod> getActivePaymentMethods() {
        return repo.findByIsActiveTrue();
    }

    public Optional<PaymentMethod> getPaymentMethodById(Long id) {
        return repo.findById(id);
    }

    public void savePaymentMethod(PaymentMethod pm) {
        repo.save(pm);
    }

    public void deletePaymentMethod(Long id) {
        repo.deleteById(id);
    }
}