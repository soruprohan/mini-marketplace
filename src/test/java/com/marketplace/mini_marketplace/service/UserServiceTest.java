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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private UserDTO userDTO;
    private Role buyerRole;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setUsername("newUser");
        userDTO.setEmail("new@test.com");
        userDTO.setPassword("password123");
        userDTO.setConfirmPassword("password123");

        buyerRole = new Role(Role.ERole.ROLE_BUYER);
        buyerRole.setId(3L);
    }

    @Test
    void registerUser_shouldCreateUserWithBuyerRole() {
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.ERole.ROLE_BUYER)).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User("newUser", "new@test.com", "encodedPassword");
        savedUser.setId(1L);
        savedUser.addRole(buyerRole);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(userDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newUser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowWhenUsernameTaken() {
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(userDTO));

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByUsername_shouldReturnUserWhenFound() {
        User user = new User("existingUser", "ex@test.com", "pass");
        user.setId(5L);
        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("existingUser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("existingUser");
    }

    @Test
    void findByUsername_shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.findByUsername("ghost"));
    }
}