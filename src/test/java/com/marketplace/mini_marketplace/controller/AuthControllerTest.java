package com.marketplace.mini_marketplace.controller;

import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(Role.ERole.ROLE_BUYER).isEmpty()) {
            roleRepository.save(new Role(Role.ERole.ROLE_BUYER));
        }
    }

    @Test
    void getLoginPage_shouldReturn200() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void getRegisterPage_shouldReturn200WithUserDTO() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("userDTO"));
    }

    @Test
    void register_withValidData_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "integrationUser")
                        .param("email", "integration@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void register_withPasswordMismatch_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "badUser")
                        .param("email", "bad@test.com")
                        .param("password", "pass1")
                        .param("confirmPassword", "pass2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void accessProtectedPage_withoutLogin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/orders/my"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }
}