package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private Integer price;
    @Column(name = "sale_price")
    private Integer salePrice;
    @Column(name = "cost_price")
    private Integer costPrice;
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    @Column(name = "sold_qunatity")
    private Integer soldQuantity = 0;
    @Column(name = "is_active")
    private Boolean isActive = true;
    @Column
    private String description;
    @Column(name = "image_url")
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}