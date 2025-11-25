package com.example.ecommerce.config;

import com.example.ecommerce.model.CarouselImage;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.PaymentMethod;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Role;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.CarouselImageRepository;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.PaymentMethodRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner init(UserRepository userRepo,
                           ProductRepository productRepo,
                           CategoryRepository catRepo,
                           PaymentMethodRepository payRepo,
                           CarouselImageRepository carouselRepo,
                           PasswordEncoder encoder) {
        return args -> {
            // Users
            if (userRepo.findByEmail("admin@store.com").isEmpty()) {
                userRepo.save(new User(null, "Admin", "admin@store.com", encoder.encode("admin"), "Yangon, HQ", Role.ADMIN));
            }
            if (userRepo.findByEmail("user@store.com").isEmpty()) {
                userRepo.save(new User(null, "User", "user@store.com", encoder.encode("password"), "Mandalay, 78th Street", Role.USER));
            }

            // Carousel Images
            if (carouselRepo.count() == 0) {
                carouselRepo.save(new CarouselImage(null, "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1200&q=80", "Mega Sale"));
                carouselRepo.save(new CarouselImage(null, "https://images.unsplash.com/photo-1472851294608-41531b6574d4?w=1200&q=80", "New Arrivals"));
            }

            // Categories & Products
            if (catRepo.count() == 0) {
                Category ele = catRepo.save(new Category(null, "Electronics"));
                Category fash = catRepo.save(new Category(null, "Fashion"));
                Category home = catRepo.save(new Category(null, "Home & Living"));

                // Note: Added soldQuantity (0) to constructor
                productRepo.save(new Product(null, "Gaming Laptop", 2500000, 2300000, 2000000, 5, 0, true, "High-performance gaming laptop.", "https://images.unsplash.com/photo-1603302576837-637886400d40?w=500&q=80", ele));
                productRepo.save(new Product(null, "Wireless Earbuds", 85000, null, 50000, 20, 10, true, "Noise cancelling wireless earbuds.", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80", ele)); // Popular
                productRepo.save(new Product(null, "Cotton Hoodie", 35000, null, 20000, 50, 8, true, "Comfortable black cotton hoodie.", "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&q=80", fash)); // Popular
                productRepo.save(new Product(null, "Modern Desk Lamp", 45000, null, 25000, 2, 0, true, "Minimalist LED desk lamp.", "https://images.unsplash.com/photo-1507473883581-56751711a3fe?w=500&q=80", home));
            }

            // Payment Methods
            if (payRepo.count() == 0) {
                payRepo.save(new PaymentMethod(null, "KBZ Pay", "09123456789", "https://play-lh.googleusercontent.com/cnMJ8g8FfYYKyG_X2GSY8P8k8-xJ9QyJ7t0X5q7X5X9X5X9X5X9X5X9X5X9X=w240-h480-rw", true));
                payRepo.save(new PaymentMethod(null, "Wave Money", "09987654321", "https://play-lh.googleusercontent.com/W-8a-w7V7v9zB4j1z0-z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z7z=w240-h480-rw", true));
                payRepo.save(new PaymentMethod(null, "Cash on Delivery", "-", "https://cdn-icons-png.flaticon.com/512/2331/2331941.png", true));
            }
        };
    }
}