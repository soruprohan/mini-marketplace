-- src/main/resources/data.sql
-- Seeds the three roles so they exist before any user tries to register.
-- Spring Boot runs this automatically when spring.sql.init.mode=always (dev profile).

-- Seeds the three roles
INSERT INTO roles (name)
VALUES ('ROLE_ADMIN'), ('ROLE_SELLER'), ('ROLE_BUYER')
    ON CONFLICT (name) DO NOTHING;

-- Seeds a default admin user (password: password123)
-- BCrypt hash of 'password123' — safe to commit, it's just a hash
INSERT INTO users (username, email, password, enabled)
VALUES ('admin', 'admin@marketplace.com', '$2a$12$Pxl647zWgCrwA5rw4N9EkerQwZqrSn5NmBb8DxvSyGaticCSFwKG6', true)
    ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_ADMIN to the seeded admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
    ON CONFLICT DO NOTHING;