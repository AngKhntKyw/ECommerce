package com.example.ecommerce.service;

import com.example.ecommerce.model.CarouselImage;
import com.example.ecommerce.repository.CarouselImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarouselService {
    private final CarouselImageRepository repo;

    public List<CarouselImage> getAllImages() {
        return repo.findAll();
    }

    public Optional<CarouselImage> getImageById(Long id) {
        return repo.findById(id);
    }

    public void saveImage(CarouselImage image) {
        // Validation: Max 5 images
        if (image.getId() == null && repo.count() >= 5) {
            throw new IllegalStateException("Cannot add more than 5 carousel images.");
        }
        repo.save(image);
    }

    public void deleteImage(Long id) {
        repo.deleteById(id);
    }
}