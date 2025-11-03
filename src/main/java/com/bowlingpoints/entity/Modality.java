package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "modality")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "modality_id")
    private Integer modalityId;

    private String name;

    private String description;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
