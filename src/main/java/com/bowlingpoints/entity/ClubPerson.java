package com.bowlingpoints.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_person_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "club_id")
    @JsonBackReference
    private Clubs club;

    @ManyToOne
    @JoinColumn(name = "person_id")
    @JsonManagedReference
    private Person person;

    @Column(name = "role_in_club")
    private String roleInClub;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "status")
    private Boolean status = true;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
