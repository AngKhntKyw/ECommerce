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
@Table(name = "user_orders")
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
    @Column(name = "total_amount")
    private Integer totalAmount; // Changed to Integer
    @Column(name = "total_cost")
    private Integer totalCost;   // Changed to Integer
    @Column(name = "order_date")
    private LocalDateTime orderDate;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();
    @Column
    private String status = "Pending";
    @Column(name = "payment_method")
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