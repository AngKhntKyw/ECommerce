package com.example.ecommerce.repository;

import com.example.ecommerce.model.CarouselImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarouselImageRepository extends JpaRepository<CarouselImage, Long> {
}