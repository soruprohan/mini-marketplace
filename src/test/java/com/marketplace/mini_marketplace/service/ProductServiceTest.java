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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ProductService productService;

    private User seller;
    private Category category;
    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        Role sellerRole = new Role(Role.ERole.ROLE_SELLER);
        sellerRole.setId(2L);

        seller = new User("sellerUser", "seller@test.com", "encodedPass");
        seller.setId(1L);
        seller.setRoles(Set.of(sellerRole));

        category = new Category();
        category.setId(10L);
        category.setName("Electronics");

        product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setStock(50);
        product.setSeller(seller);
        product.setCategory(category);

        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("A test product");
        productDTO.setPrice(BigDecimal.valueOf(29.99));
        productDTO.setStock(50);
        productDTO.setCategoryId(10L);
    }

    @Test
    void createProduct_shouldSaveAndReturnProduct() {
        when(userRepository.findByUsername("sellerUser")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(productDTO, "sellerUser");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowWhenSellerNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productDTO, "unknown"));

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_shouldThrowWhenCategoryNotFound() {
        when(userRepository.findByUsername("sellerUser")).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(productDTO, "sellerUser"));
    }

    @Test
    void getProductById_shouldReturnProductWhenFound() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getProductById_shouldThrowWhenNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(999L));
    }

    @Test
    void deleteProduct_shouldDeleteWhenCalledByOwner() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findByUsername("sellerUser")).thenReturn(Optional.of(seller));

        productService.deleteProduct(100L, "sellerUser");

        verify(productRepository, times(1)).delete(product);
    }
}