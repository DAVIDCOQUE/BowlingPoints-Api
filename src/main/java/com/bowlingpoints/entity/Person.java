package com.bowlingpoints.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa a una persona dentro del sistema (jugador, entrenador, etc).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Integer personId;

    @Column(name = "document", unique = true, nullable = false)
    private String document;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "full_surname", nullable = false)
    private String fullSurname;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Relación uno a uno con el usuario.
     */
    @OneToOne(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    /**
     * Relación con los clubes a los que pertenece la persona.
     */
    @OneToMany(mappedBy = "person")
    @JsonBackReference
    private List<ClubPerson> clubPersons;

    /**
     * Relación con las categorías asociadas a la persona.
     */
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonCategory> personCategories;

    public Person(Integer personId) {
        this.personId = personId;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
