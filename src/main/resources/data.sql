-- src/main/resources/data.sql
-- Seeds the three roles so they exist before any user tries to register.
-- Spring Boot runs this automatically when spring.sql.init.mode=always (dev profile).

INSERT INTO roles (name)
VALUES ('ROLE_ADMIN'), ('ROLE_SELLER'), ('ROLE_BUYER')
    ON CONFLICT (name) DO NOTHING;