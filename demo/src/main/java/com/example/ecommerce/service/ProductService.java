package com.example.ecommerce.service;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    // Admin: Get All
    public List<Product> getAllProducts() { return productRepo.findAll(); }

    // User: Get Active Only
    public List<Product> getAllActiveProducts() { return productRepo.findByIsActiveTrueOrderByIdDesc(); }
    public List<Product> getActiveProductsByCategory(Long id) { return productRepo.findByCategoryIdAndIsActiveTrue(id); }

    // Helper for filters
    public List<Product> getProductsByCategory(Long id) { return productRepo.findByCategoryId(id); }

    public List<Category> getAllCategories() { return categoryRepo.findAll(); }
    public Optional<Product> getProductById(Long id) { return productRepo.findById(id); }
    public List<Product> getLowStockProducts() { return productRepo.findByStockQuantityLessThan(5); }

    public Product saveProduct(Product product) { return productRepo.save(product); }
    public void deleteProduct(Long id) { productRepo.deleteById(id); }

    public Category saveCategory(Category category) { return categoryRepo.save(category); }

    // FIXED: Changed return type from void to boolean
    public boolean deleteCategory(Long id) {
        try {
            categoryRepo.deleteById(id);
            return true;
        } catch (Exception e) {
            // Returns false if deletion fails (e.g., foreign key constraint if products exist in this category)
            return false;
        }
    }
}