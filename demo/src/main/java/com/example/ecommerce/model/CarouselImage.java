package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;

@Entity
@Table(name = "carouse_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarouselImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "image_url")
    private String imageUrl;
    @Column
    private String title; // Optional title/alt text
}