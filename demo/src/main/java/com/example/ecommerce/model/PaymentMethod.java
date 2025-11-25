package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
    @Column(name = "phone_number")
    private String phoneNumber; // Changed from description
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "is_active")
    private Boolean isActive = true; // Default to true
}