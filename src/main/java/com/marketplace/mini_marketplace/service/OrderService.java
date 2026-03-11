package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.OrderDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Order;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.OrderRepository;
import com.marketplace.mini_marketplace.repository.ProductRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository  = orderRepository;
        this.productRepository = productRepository;
        this.userRepository   = userRepository;
    }

    @Transactional
    public Order placeOrder(OrderDTO dto, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + buyerUsername));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + dto.getProductId()));

        if (product.getStock() < dto.getQuantity()) {
            throw new IllegalStateException(
                    "Insufficient stock. Available: " + product.getStock() +
                            ", requested: " + dto.getQuantity());
        }

        // Reduce stock
        product.setStock(product.getStock() - dto.getQuantity());
        productRepository.save(product);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantity(dto.getQuantity());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + buyerUsername));
        return orderRepository.findByBuyerId(buyer.getId());
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void cancelOrder(Long orderId, String buyerUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        User currentUser = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + buyerUsername));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            verifyOrderOwner(order, buyerUsername);
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Order is already cancelled");
        }

        // Restore stock
        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productRepository.save(product);

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    @Transactional
    public void markAsReceived(Long orderId, String buyerUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        verifyOrderOwner(order, buyerUsername);

        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only pending orders can be marked as received");
        }

        order.setStatus("RECEIVED");
        orderRepository.save(order);
    }

    private void verifyOrderOwner(Order order, String username) {
        if (!order.getBuyer().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to modify this order");
        }
    }
}