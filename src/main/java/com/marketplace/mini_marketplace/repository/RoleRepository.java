package com.marketplace.mini_marketplace.repository;

import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.model.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Primary lookup used when assigning roles during registration.
     * Example: roleRepository.findByName(ERole.ROLE_BUYER)
     */
    Optional<Role> findByName(Role.ERole name);
}