package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Admin methods
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findAllByOrderByIdDesc();
    List<Product> findByStockQuantityLessThan(Integer quantity);

    // User methods
    List<Product> findByIsActiveTrue();
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
    List<Product> findByIsActiveTrueOrderByIdDesc();

    // NEW: Find Most Selling (Sold > 5)
    List<Product> findBySoldQuantityGreaterThanEqualAndIsActiveTrueOrderBySoldQuantityDesc(Integer quantity);
}