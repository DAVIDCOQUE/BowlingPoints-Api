package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Entidad que representa a los usuarios del sistema.
 * Implementa UserDetails para la integración con Spring Security.
 */
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"nickname"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    @Column(name = "attempts_login")
    private Integer attemptsLogin = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Relación uno a uno con la entidad Person.
     */
    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "person_id", unique = true, nullable = false)
    private Person person;

    /**
     * Relación uno a muchos con los roles asignados al usuario.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===============================
    // Métodos requeridos por UserDetails
    // ===============================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (userRoles == null) return List.of();

        return userRoles.stream()
                .filter(UserRole::isGranted)
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return nickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes personalizarlo si quieres manejar expiración
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puedes personalizarlo con un campo adicional
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes personalizarlo con expiración de contraseña
    }

    @Override
    public boolean isEnabled() {
        return status;
    }

    /**
     * Asigna un único rol al usuario (elimina roles previos).
     */
    public void setSingleRole(Role role) {
        this.userRoles.clear();
        UserRole userRole = UserRole.builder()
                .user(this)
                .role(role)
                .status(true)
                .build();
        this.userRoles.add(userRole);
    }

    /**
     * Agrega un rol adicional al usuario sin eliminar los existentes.
     */
    public void addRole(Role role) {
        this.userRoles.add(
                UserRole.builder()
                        .user(this)
                        .role(role)
                        .status(true)
                        .build()
        );
    }
}
