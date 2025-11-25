package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class ShoppingCartService {
    private List<Product> items = new ArrayList<>();

    public void addItem(Product product) {
        items.add(product);
    }

    public void removeItem(Long productId) {
        items.removeIf(p -> p.getId().equals(productId));
    }

    // NEW: Remove only one instance of the product
    public void removeOneItem(Long productId) {
        Optional<Product> productToRemove = items.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst();
        productToRemove.ifPresent(items::remove);
    }

    public void clear() {
        items.clear();
    }

    public Integer getTotal() {
        return items.stream()
                .mapToInt(p -> (p.getSalePrice() != null) ? p.getSalePrice() : p.getPrice())
                .sum();
    }

    public int getCount(Long productId) {
        return (int) items.stream()
                .filter(p -> p.getId().equals(productId))
                .count();
    }

    public int getSize() {
        return items.size();
    }
}