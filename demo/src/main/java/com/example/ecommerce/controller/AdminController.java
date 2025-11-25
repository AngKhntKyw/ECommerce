package com.example.ecommerce.controller;

import com.example.ecommerce.model.CarouselImage;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.PaymentMethod;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.UserOrder;
import com.example.ecommerce.service.CarouselService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CarouselService carouselService; // New Service

    @GetMapping
    public String adminHome() { return "redirect:/admin/dashboard"; }

    // ... (Keep Dashboard, Products, Categories, Orders, Payment methods as they are) ...

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("totalProfit", orderService.getTotalProfit());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().sorted((a,b) -> b.getId().compareTo(a.getId())).limit(5).toList());
        model.addAttribute("activePage", "dashboard");
        return "admin-dashboard";
    }

    // ... [Previous methods for Product, Category, Payment, Orders remain unchanged] ...
    // Paste your existing methods here to keep file complete...

    // --- PRODUCTS ---
    @GetMapping("/products")
    public String productManagement(Model model, @RequestParam(required = false) Long categoryId) {
        List<Product> products = (categoryId != null) ? productService.getProductsByCategory(categoryId) : productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("activeCategory", categoryId);
        model.addAttribute("activePage", "products");
        return "admin-products";
    }
    @GetMapping("/product/add")
    public String showAddProductForm(Model model, RedirectAttributes redirectAttributes) {
        if (productService.getAllCategories().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "You must create a Category before adding Products!");
            return "redirect:/admin/categories";
        }
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("activePage", "products");
        return "admin-product-form";
    }
    @GetMapping("/product/edit/{id}")
    public String showUpdateProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id).orElseThrow());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("activePage", "products");
        return "admin-product-form";
    }
    @PostMapping("/product")
    public String saveProduct(Product product, RedirectAttributes redirectAttributes) {
        try { productService.saveProduct(product); redirectAttributes.addFlashAttribute("success", "Product saved!"); }
        catch (Exception e) { redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage()); }
        return "redirect:/admin/products";
    }
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted!");
        return "redirect:/admin/products";
    }

    // --- CATEGORIES ---
    @GetMapping("/categories")
    public String categoryManagement(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("activePage", "categories");
        return "admin-categories";
    }
    @GetMapping("/category/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("activePage", "categories");
        return "admin-category-form";
    }
    @GetMapping("/category/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", productService.getAllCategories().stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow());
        model.addAttribute("activePage", "categories");
        return "admin-category-form";
    }
    @PostMapping("/category")
    public String saveCategory(Category category, RedirectAttributes redirectAttributes) {
        productService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Category saved!");
        return "redirect:/admin/categories";
    }
    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productService.deleteCategory(id)) redirectAttributes.addFlashAttribute("success", "Category deleted!");
        else redirectAttributes.addFlashAttribute("error", "Cannot delete category used by products.");
        return "redirect:/admin/categories";
    }

    // --- ORDERS ---
    @GetMapping("/orders")
    public String orderManagement(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("activePage", "orders");
        return "admin-orders";
    }
    @GetMapping("/order/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrderById(id));
        model.addAttribute("activePage", "orders");
        return "admin-order-detail";
    }
    @PostMapping("/order/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Order status updated.");
        return "redirect:/admin/order/" + id;
    }

    // --- PAYMENTS ---
    @GetMapping("/payments")
    public String paymentManagement(Model model) {
        model.addAttribute("payments", paymentService.getAllPaymentMethods());
        model.addAttribute("activePage", "payments");
        return "admin-payments";
    }
    @GetMapping("/payment/add")
    public String showAddPaymentForm(Model model) {
        model.addAttribute("payment", new PaymentMethod());
        model.addAttribute("activePage", "payments");
        return "admin-payment-form";
    }
    @GetMapping("/payment/edit/{id}")
    public String showEditPaymentForm(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.getPaymentMethodById(id).orElseThrow());
        model.addAttribute("activePage", "payments");
        return "admin-payment-form";
    }
    @PostMapping("/payment")
    public String savePayment(PaymentMethod paymentMethod, RedirectAttributes redirectAttributes) {
        paymentService.savePaymentMethod(paymentMethod);
        redirectAttributes.addFlashAttribute("success", "Payment Method saved!");
        return "redirect:/admin/payments";
    }
    @GetMapping("/payment/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentService.deletePaymentMethod(id);
        redirectAttributes.addFlashAttribute("success", "Payment Method deleted!");
        return "redirect:/admin/payments";
    }

    // --- CAROUSEL (NEW) ---
    @GetMapping("/carousel")
    public String carouselManagement(Model model) {
        model.addAttribute("images", carouselService.getAllImages());
        model.addAttribute("activePage", "carousel");
        return "admin-carousel";
    }

    @GetMapping("/carousel/add")
    public String showAddCarouselForm(Model model) {
        model.addAttribute("carouselImage", new CarouselImage());
        model.addAttribute("activePage", "carousel");
        return "admin-carousel-form";
    }

    @GetMapping("/carousel/edit/{id}")
    public String showEditCarouselForm(@PathVariable Long id, Model model) {
        model.addAttribute("carouselImage", carouselService.getImageById(id).orElseThrow());
        model.addAttribute("activePage", "carousel");
        return "admin-carousel-form";
    }

    @PostMapping("/carousel")
    public String saveCarousel(CarouselImage carouselImage, RedirectAttributes redirectAttributes) {
        try {
            carouselService.saveImage(carouselImage);
            redirectAttributes.addFlashAttribute("success", "Image saved successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save image.");
        }
        return "redirect:/admin/carousel";
    }

    @GetMapping("/carousel/delete/{id}")
    public String deleteCarousel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        carouselService.deleteImage(id);
        redirectAttributes.addFlashAttribute("success", "Image deleted successfully!");
        return "redirect:/admin/carousel";
    }
}