package com.marketplace.mini_marketplace.repository;

import com.marketplace.mini_marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Used by Spring Security and login flow. */
    Optional<User> findByUsername(String username);

    /** Used during registration to prevent duplicate emails. */
    Optional<User> findByEmail(String email);

    /** Quick existence checks — avoids loading the full entity. */
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}