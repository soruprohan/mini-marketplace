package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.ProductDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Category;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.CategoryRepository;
import com.marketplace.mini_marketplace.repository.ProductRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ProductService productService;

    private User seller;
    private Category category;
    private Product product;
    private ProductDTO dto;

    @BeforeEach
    void setUp() {
        seller = new User("seller1", "seller@test.com", "encoded");
        seller.setId(1L);

        Role sellerRole = new Role(Role.ERole.ROLE_SELLER);
        seller.setRoles(Set.of(sellerRole));

        category = new Category();
        category.setId(10L);
        category.setName("Electronics");

        product = new Product();
        product.setId(100L);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setStock(5);
        product.setSeller(seller);
        product.setCategory(category);

        dto = new ProductDTO();
        dto.setName("Laptop");
        dto.setDescription("A laptop");
        dto.setPrice(new BigDecimal("999.99"));
        dto.setStock(5);
        dto.setCategoryId(10L);
    }

    @Test
    void createProduct_shouldSaveAndReturn_whenSellerAndCategoryExist() {
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.createProduct(dto, "seller1");

        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getSeller()).isEqualTo(seller);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrow_whenSellerNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(dto, "ghost"));
    }

    @Test
    void createProduct_shouldThrow_whenCategoryNotFound() {
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(dto, "seller1"));
    }

    @Test
    void getProductById_shouldReturnProduct_whenFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(999L));
    }

    @Test
    void deleteProduct_shouldDelete_whenCalledByOwner() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("seller1")).thenReturn(Optional.of(seller));

        productService.deleteProduct(100L, "seller1");

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrow_whenCalledByNonOwnerNonAdmin() {
        User otherUser = new User("other", "other@test.com", "pass");
        otherUser.setId(2L);
        Role buyerRole = new Role(Role.ERole.ROLE_BUYER);
        otherUser.setRoles(Set.of(buyerRole));

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class,
                () -> productService.deleteProduct(100L, "other"));
    }
}