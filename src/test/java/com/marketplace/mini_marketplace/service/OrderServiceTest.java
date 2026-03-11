package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.OrderDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Order;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.OrderRepository;
import com.marketplace.mini_marketplace.repository.ProductRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private OrderService orderService;

    private User buyer;
    private Product product;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        buyer = new User("buyerUser", "buyer@test.com", "pass");
        buyer.setId(1L);

        product = new Product();
        product.setId(50L);
        product.setName("Widget");
        product.setPrice(BigDecimal.valueOf(9.99));
        product.setStock(10);

        orderDTO = new OrderDTO();
        orderDTO.setProductId(50L);
        orderDTO.setQuantity(3);
    }

    @Test
    void placeOrder_shouldReduceStockAndSaveOrder() {
        when(userRepository.findByUsername("buyerUser")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setBuyer(buyer);
        savedOrder.setProduct(product);
        savedOrder.setQuantity(3);
        savedOrder.setStatus("PENDING");
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.placeOrder(orderDTO, "buyerUser");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(product.getStock()).isEqualTo(7);
        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_shouldThrowWhenInsufficientStock() {
        orderDTO.setQuantity(20); // more than stock of 10
        when(userRepository.findByUsername("buyerUser")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));

        assertThrows(IllegalStateException.class,
                () -> orderService.placeOrder(orderDTO, "buyerUser"));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getMyOrders_shouldReturnOrdersForBuyer() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        when(userRepository.findByUsername("buyerUser")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerId(1L)).thenReturn(List.of(order1, order2));

        List<Order> result = orderService.getMyOrders("buyerUser");

        assertThat(result).hasSize(2);
    }

    @Test
    void cancelOrder_shouldRestoreStockAndSetCancelled() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantity(3);
        order.setStatus("PENDING");

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("buyerUser")).thenReturn(Optional.of(buyer));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(10L, "buyerUser");

        assertThat(order.getStatus()).isEqualTo("CANCELLED");
        assertThat(product.getStock()).isEqualTo(13); // 10 + 3 restored
        verify(orderRepository).save(order);
    }
}