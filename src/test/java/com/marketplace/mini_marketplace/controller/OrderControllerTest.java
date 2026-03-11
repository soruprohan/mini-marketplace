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
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(Role.ERole.ROLE_SELLER).isEmpty()) {
            roleRepository.save(new Role(Role.ERole.ROLE_SELLER));
        }
        if (roleRepository.findByName(Role.ERole.ROLE_BUYER).isEmpty()) {
            roleRepository.save(new Role(Role.ERole.ROLE_BUYER));
        }

        Category cat = new Category();
        cat.setName("OrderTestCat" + System.currentTimeMillis());
        cat = categoryRepository.save(cat);

        User seller = new User("orderTestSeller", "orderseller@test.com",
                passwordEncoder.encode("pass"));
        seller.addRole(roleRepository.findByName(Role.ERole.ROLE_SELLER).get());
        seller = userRepository.save(seller);

        User buyer = new User("buyerForOrder", "buyerorder@test.com",
                passwordEncoder.encode("pass"));
        buyer.addRole(roleRepository.findByName(Role.ERole.ROLE_BUYER).get());
        userRepository.save(buyer);

        testProduct = new Product();
        testProduct.setName("Order Product");
        testProduct.setPrice(BigDecimal.valueOf(12.00));
        testProduct.setStock(20);
        testProduct.setSeller(seller);
        testProduct.setCategory(cat);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @WithMockUser(username = "buyerForOrder", roles = {"BUYER"})
    void placeOrder_asBuyer_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/orders")
                        .param("productId", testProduct.getId().toString())
                        .param("quantity", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "buyerForOrder", roles = {"BUYER"})
    void getAllOrders_asBuyer_shouldReturn403() throws Exception {
        mockMvc.perform(get("/orders/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "buyerForOrder", roles = {"BUYER"})
    void getMyOrders_asBuyer_shouldReturn200() throws Exception {
        mockMvc.perform(get("/orders/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/my-orders"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void getAllOrders_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(get("/orders/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/all-orders"));
    }
}