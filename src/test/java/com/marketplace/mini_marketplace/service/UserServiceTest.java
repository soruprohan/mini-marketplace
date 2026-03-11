package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.UserDTO;
import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.RoleRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private UserDTO dto;
    private Role buyerRole;

    @BeforeEach
    void setUp() {
        dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setEmail("newuser@test.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");

        buyerRole = new Role(Role.ERole.ROLE_BUYER);
    }

    @Test
    void registerUser_shouldSaveUser_whenUsernameAndEmailAreNew() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.ERole.ROLE_BUYER)).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerUser(dto);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getRoles()).contains(buyerRole);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrow_whenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(dto));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_shouldThrow_whenEmailAlreadyRegistered() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.findByEmail("newuser@test.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(dto));
    }

    @Test
    void findByUsername_shouldReturnUser_whenFound() {
        User user = new User("newuser", "newuser@test.com", "encoded");
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("newuser");

        assertThat(result.getUsername()).isEqualTo("newuser");
    }

    @Test
    void findByUsername_shouldThrow_whenNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.findByUsername("ghost"));
    }
}