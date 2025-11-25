package com.example.ecommerce.repository;

import com.example.ecommerce.model.CarouselImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarouselImageRepository extends JpaRepository<CarouselImage, Long> {
}