package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.model.Category;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Category testCategory;
    private User testSeller;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(Role.ERole.ROLE_SELLER).isEmpty()) {
            roleRepository.save(new Role(Role.ERole.ROLE_SELLER));
        }
        if (roleRepository.findByName(Role.ERole.ROLE_BUYER).isEmpty()) {
            roleRepository.save(new Role(Role.ERole.ROLE_BUYER));
        }

        testCategory = new Category();
        testCategory.setName("Test Category " + System.currentTimeMillis());
        testCategory.setDescription("For testing");
        testCategory = categoryRepository.save(testCategory);

        testSeller = new User("sellerForTest", "seller4test@test.com",
                passwordEncoder.encode("pass"));
        testSeller.addRole(roleRepository.findByName(Role.ERole.ROLE_SELLER).get());
        testSeller = userRepository.save(testSeller);
    }

    @Test
    void getProducts_shouldReturn200ForAnonymousUser() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"));
    }

    @Test
    @WithMockUser(username = "sellerForTest", roles = {"SELLER"})
    void createProduct_asSeller_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/products")
                        .param("name", "New Widget")
                        .param("description", "A widget")
                        .param("price", "19.99")
                        .param("stock", "10")
                        .param("categoryId", testCategory.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @WithMockUser(username = "buyerUser", roles = {"BUYER"})
    void newProductForm_asBuyer_shouldReturn403() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProductDetail_shouldReturn200() throws Exception {
        Product p = new Product();
        p.setName("Detail Product");
        p.setPrice(BigDecimal.valueOf(5.00));
        p.setStock(5);
        p.setSeller(testSeller);
        p.setCategory(testCategory);
        p = productRepository.save(p);

        mockMvc.perform(get("/products/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"));
    }
}