package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer totalAmount; // Changed to Integer
    private Integer totalCost;   // Changed to Integer

    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    private String status = "Pending";
    private String paymentMethod;

    public String getProductNames() {
        if (items == null || items.isEmpty()) {
            return "No items";
        }
        return items.stream()
                .map(item -> item.getProduct().getName() + " (x" + item.getQuantity() + ")")
                .collect(Collectors.joining(", "));
    }
}