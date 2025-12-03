package com.example.ecommerce.service;

import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.PaymentMethod;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.model.UserOrder;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentMethodRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final PaymentMethodRepository paymentMethodRepo; // Injected
    private final ShoppingCartService cart;

    @Transactional
    public UserOrder checkout(User user, String paymentMethodName) {
        if (cart.getItems().isEmpty()) return null;

        // Fetch the actual PaymentMethod entity
        PaymentMethod pm = paymentMethodRepo.findByName(paymentMethodName)
                .orElseThrow(() -> new IllegalStateException("Invalid payment method selected"));

        Map<Long, Long> productQuantities = cart.getItems().stream()
                .collect(Collectors.groupingBy(Product::getId, Collectors.counting()));

        List<OrderItem> orderItems = new ArrayList<>();
        int totalCost = 0;

        UserOrder order = new UserOrder();
        order.setUser(user);
        order.setTotalAmount(cart.getTotal());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("Pending");

        // Set the entity, not the string
        order.setPaymentMethod(pm);

        for (Map.Entry<Long, Long> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue().intValue();

            Product dbProduct = productRepo.findById(productId).orElseThrow();

            if (dbProduct.getStockQuantity() < quantity) {
                throw new IllegalStateException("Not enough stock for " + dbProduct.getName());
            }

            // Deduct Stock
            dbProduct.setStockQuantity(dbProduct.getStockQuantity() - quantity);

            // Increment Sold Quantity
            int currentSold = (dbProduct.getSoldQuantity() == null) ? 0 : dbProduct.getSoldQuantity();
            dbProduct.setSoldQuantity(currentSold + quantity);

            productRepo.save(dbProduct);

            totalCost += (dbProduct.getCostPrice() != null ? dbProduct.getCostPrice() : 0) * quantity;

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(dbProduct);
            item.setQuantity(quantity);
            int finalPrice = (dbProduct.getSalePrice() != null) ? dbProduct.getSalePrice() : dbProduct.getPrice();
            item.setPrice(finalPrice);

            orderItems.add(item);
        }

        order.setTotalCost(totalCost);
        order.setItems(orderItems);

        orderRepo.save(order);
        cart.clear();
        return order;
    }

    public void updateOrderStatus(Long orderId, String status) {
        UserOrder order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus(status);
        orderRepo.save(order);
    }

    //Save payment screenshot
    public void updatePaymentScreenshot(Long orderId, String screenshotBase64) {
        UserOrder order = getOrderById(orderId);
        order.setPaymentScreenshot(screenshotBase64);
        orderRepo.save(order);
    }

    public UserOrder getOrderById(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    // CHANGED: Use the sorted repository method for User Orders
    public List<UserOrder> getOrdersByUser(Long userId) {
        return orderRepo.findByUserIdOrderByIdDesc(userId);
    }

    // CHANGED: Use the sorted repository method for Admin Orders
    public List<UserOrder> getAllOrders() {
        return orderRepo.findAllByOrderByIdDesc();
    }

    public Long getTotalRevenue() {
        return getAllOrders().stream()
                .filter(o -> !o.getStatus().equals("Canceled"))
                .mapToLong(UserOrder::getTotalAmount).sum();
    }

    public Long getTotalProfit() {
        return getAllOrders().stream()
                .filter(o -> !o.getStatus().equals("Canceled"))
                .mapToLong(o -> o.getTotalAmount() - (o.getTotalCost() != null ? o.getTotalCost() : 0))
                .sum();
    }
}