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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ProductService productService;
    private final ProductRepository productRepo;
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

        model.addAttribute("carouselImages", carouselService.getAllImages());
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
    public String register(String name, String email, String password, String address) {
        if (userService.registerNewUser(name, email, password, address)) {
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

    // --- Cart Logic ---

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

        // If HTMX request, return partial fragment to update UI without reload
        if ("true".equals(htmxRequest)) {
            if (isError) model.addAttribute("error", message);
            else model.addAttribute("success", message);

            // Add cartCount to model manually since we are returning a fragment directly
            // (GlobalControllerAdvice might not trigger for fragments depending on config, safer to add)
            model.addAttribute("cartCount", cart.getSize());

            return "fragments :: cart-update";
        }

        // Fallback for non-JS
        if (isError) redirectAttributes.addFlashAttribute("error", message);
        else redirectAttributes.addFlashAttribute("success", message);

        if (categoryId != null) return "redirect:/?categoryId=" + categoryId;
        return "redirect:/";
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

    @Data
    @AllArgsConstructor
    public static class CartItemDto {
        private Product product;
        private int quantity;
    }
}