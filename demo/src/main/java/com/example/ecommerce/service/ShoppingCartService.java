package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

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

    public void clear() {
        items.clear();
    }

    public Integer getTotal() {
        return items.stream()
                .mapToInt(p -> (p.getSalePrice() != null) ? p.getSalePrice() : p.getPrice())
                .sum();
    }

    // NEW: Count how many of a specific product are in the cart
    public int getCount(Long productId) {
        return (int) items.stream()
                .filter(p -> p.getId().equals(productId))
                .count();
    }

    // NEW: Get total number of items for Badge
    public int getSize() {
        return items.size();
    }
}