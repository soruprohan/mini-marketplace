package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.ProductDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Category;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.CategoryRepository;
import com.marketplace.mini_marketplace.repository.ProductRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.productRepository  = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository     = userRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public List<Product> getProductsBySeller(String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + sellerUsername));
        return productRepository.findBySellerId(seller.getId());
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product createProduct(ProductDTO dto, String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerUsername));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        product.setSeller(seller);
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO dto, String currentUsername) {
        Product product = getProductById(id);
        verifyOwnerOrAdmin(product, currentUsername);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id, String currentUsername) {
        Product product = getProductById(id);
        verifyOwnerOrAdmin(product, currentUsername);
        productRepository.delete(product);
    }

    private void verifyOwnerOrAdmin(Product product, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));
        boolean isOwner = product.getSeller().getUsername().equals(currentUsername);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to modify this product");
        }
    }
}