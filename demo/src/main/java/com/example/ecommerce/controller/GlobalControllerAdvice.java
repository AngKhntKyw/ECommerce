package com.example.ecommerce.controller;

import com.example.ecommerce.service.ShoppingCartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ShoppingCartService cart;

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // NEW: Globally expose cart size for the Navbar Badge
    @ModelAttribute("cartCount")
    public int cartCount() {
        return cart.getSize();
    }
}