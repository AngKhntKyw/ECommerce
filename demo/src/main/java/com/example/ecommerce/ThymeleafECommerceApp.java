package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// The @EnableWebSecurity is moved to SecurityConfig.java for better separation.
@SpringBootApplication
// Ensure Spring scans the new packages
// Specify where the JpaRepository interfaces are now located
@EnableJpaRepositories(basePackages = "com.example.ecommerce.repository")
public class ThymeleafECommerceApp {

    public static void main(String[] args) {
        SpringApplication.run(ThymeleafECommerceApp.class, args);
    }
}