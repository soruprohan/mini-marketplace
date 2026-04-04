package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.UserDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.RoleRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Username of the bootstrapped super-admin that must never be modified or removed. */
    private static final String PROTECTED_ADMIN_USERNAME = "admin";

    @Transactional
    public void setRole(Long userId, Role.ERole targetRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (PROTECTED_ADMIN_USERNAME.equals(user.getUsername())) {
            throw new IllegalStateException("The built-in admin account's role cannot be changed.");
        }
        Role role = roleRepository.findByName(targetRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public String getUsernameById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getUsername();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (PROTECTED_ADMIN_USERNAME.equals(user.getUsername())) {
            throw new IllegalStateException("The built-in admin account cannot be deleted.");
        }
        userRepository.delete(user);
    }

    @Transactional
    public User registerUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        Role buyerRole = roleRepository.findByName(Role.ERole.ROLE_BUYER)
                .orElseThrow(() -> new IllegalStateException("ROLE_BUYER not found — run data.sql first"));

        User user = new User(dto.getUsername(), dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()));
        user.addRole(buyerRole);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}