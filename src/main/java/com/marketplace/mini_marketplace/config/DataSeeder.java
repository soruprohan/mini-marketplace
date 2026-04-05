package com.marketplace.mini_marketplace.config;

import com.marketplace.mini_marketplace.model.Role;
import com.marketplace.mini_marketplace.model.Role.ERole;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.RoleRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Runs once at startup (after Hibernate has created/updated the schema).
 * Seeds the three roles and a default admin account if they don't already exist.
 * Fully idempotent — safe on every restart and redeploy.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository  = roleRepository;
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdminUser();
    }

    // ── Roles ──────────────────────────────────────────────────────────────────

    private void seedRoles() {
        for (ERole roleEnum : ERole.values()) {
            if (roleRepository.findByName(roleEnum).isEmpty()) {
                roleRepository.save(new Role(roleEnum));
                log.info("Seeded role: {}", roleEnum);
            }
        }
    }

    // ── Admin user ─────────────────────────────────────────────────────────────

    private void seedAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists — skipping seed.");
            return;
        }

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found after seeding"));

        User admin = new User(
                "admin",
                "admin@marketplace.com",
                passwordEncoder.encode("admin1234")   // hashed at runtime — no stale hash
        );
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
        log.info("Seeded default admin user.");
    }
}
