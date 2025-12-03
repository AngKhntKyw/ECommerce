package com.example.ecommerce.controller;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.CarouselService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ShoppingCartService;
import com.example.ecommerce.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ProductService productService;
    private final ProductRepository productRepo; // Using Repo directly for specialized queries
    private final UserService userService;
    private final OrderService orderService;
    private final ShoppingCartService cart;
    private final PaymentService paymentService;
    private final CarouselService carouselService;

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) Long categoryId) {
        List<Category> categories = productService.getAllCategories();
        List<Product> products;

        if (categoryId != null) {
            products = productService.getActiveProductsByCategory(categoryId);
            model.addAttribute("activeCategory", categoryId);
        } else {
            products = productService.getAllActiveProducts();
        }

        // 1. Carousel
        model.addAttribute("carouselImages", carouselService.getAllImages());

        // 2. New Arrivals
        model.addAttribute("newArrivals", productRepo.findTop10ByIsActiveTrueOrderByIdDesc());

        // 3. Popular Products
        model.addAttribute("popularProducts", productRepo.findBySoldQuantityGreaterThanEqualAndIsActiveTrueOrderBySoldQuantityDesc(6));

        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        return "index";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).orElseThrow();
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", productService.getActiveProductsByCategory(product.getCategory().getId()));
        return "product-detail";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/register")
    public String register(String name, String email, String password, String address,String phone_number) {
        if (userService.registerNewUser(name, email, password, address,phone_number)) {
            return "redirect:/login?registered";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "profile";
    }

    // --- ADDED THIS METHOD TO FIX PROFILE UPDATE ---
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phoneNumber,
                                @RequestParam String address,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        // 1. Find current user
        User user = userService.findByEmail(auth.getName()).orElseThrow();

        // 2. Update fields
        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);

        // 3. Save to DB
        userService.updateUser(user);

        // 4. Add success message and redirect
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    // Standard Add to Cart (Redirets to Shop/Home)
    @GetMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(required = false) Long categoryId,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        Product product = productService.getProductById(id).orElse(null);
        String message = "";
        boolean isError = false;

        if (product != null) {
            int currentInCart = cart.getCount(id);
            if (currentInCart + 1 > product.getStockQuantity()) {
                message = "Cannot add item. Max stock (" + product.getStockQuantity() + ") reached.";
                isError = true;
            } else {
                cart.addItem(product);
                message = "Item added to cart.";
            }
        }

        if ("true".equals(htmxRequest)) {
            if (isError) model.addAttribute("error", message);
            else model.addAttribute("success", message);
            model.addAttribute("cartCount", cart.getSize());
            return "fragments :: cart-update";
        }

        if (isError) redirectAttributes.addFlashAttribute("error", message);
        else redirectAttributes.addFlashAttribute("success", message);

        if (categoryId != null) return "redirect:/?categoryId=" + categoryId;
        return "redirect:/";
    }

    // NEW: Specific method for Cart Page Increase (Redirects to Cart)
    @GetMapping("/cart/increase/{id}")
    public String increaseCartItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(id).orElse(null);
        if (product != null) {
            int currentInCart = cart.getCount(id);
            if (currentInCart < product.getStockQuantity()) {
                cart.addItem(product);
            } else {
                redirectAttributes.addFlashAttribute("error", "Cannot add more. Max stock reached for " + product.getName());
            }
        }
        return "redirect:/cart";
    }

    // NEW: Specific method for Cart Page Decrease (Redirects to Cart)
    @GetMapping("/cart/decrease/{id}")
    public String decreaseCartItem(@PathVariable Long id) {
        cart.removeOneItem(id);
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id) {
        cart.removeItem(id);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        Map<Product, Long> groupedItems = cart.getItems().stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

        List<CartItemDto> cartDisplayItems = new ArrayList<>();
        for (Map.Entry<Product, Long> entry : groupedItems.entrySet()) {
            cartDisplayItems.add(new CartItemDto(entry.getKey(), entry.getValue().intValue()));
        }

        model.addAttribute("cartItems", cartDisplayItems);
        model.addAttribute("total", cart.getTotal());
        model.addAttribute("paymentMethods", paymentService.getActivePaymentMethods());
        return "cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(Authentication auth, @RequestParam String paymentMethod, RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(auth.getName()).orElseThrow();
        try {
            orderService.checkout(user, paymentMethod);
            return "redirect:/orders?success";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/orders")
    public String myOrders(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        return "orders";
    }

    // NEW: Upload Payment Screenshot
    @PostMapping("/order/upload-payment")
    public String uploadPayment(@RequestParam("orderId") Long orderId,
                                @RequestParam("file") MultipartFile file,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/orders";
        }

        try {
            // Check ownership
            User user = userService.findByEmail(auth.getName()).orElseThrow();
            // In a real app, verify order belongs to user here.

            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            // Assuming PNG/JPG, constructing a data URI.
            // A more robust solution would detect the mime type.
            // For simplicity, we assume standard image types.
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String dataUri = "data:" + mimeType + ";base64," + base64Image;

            orderService.updatePaymentScreenshot(orderId, dataUri);
            redirectAttributes.addFlashAttribute("success", "Payment screenshot uploaded successfully!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image.");
        }

        return "redirect:/orders";
    }

    @Data
    @AllArgsConstructor
    public static class CartItemDto {
        private Product product;
        private int quantity;
    }
}