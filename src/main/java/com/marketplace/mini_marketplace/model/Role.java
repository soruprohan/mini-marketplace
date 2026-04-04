package com.marketplace.mini_marketplace.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    // ── Enum for type-safe role names ─────────────────────────────────────────

    public enum ERole {
        ROLE_ADMIN,
        ROLE_SELLER,
        ROLE_BUYER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, unique = true)
    private ERole name;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Role() {}

    public Role(ERole name) {
        this.name = name;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long  getId()         { return id; }
    public void  setId(Long id)  { this.id = id; }

    public ERole getName()            { return name; }
    public void  setName(ERole name)  { this.name = name; }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name=" + name + "}";
    }
}