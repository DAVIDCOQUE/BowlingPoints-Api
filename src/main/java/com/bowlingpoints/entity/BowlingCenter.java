package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bowling_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BowlingCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bowling_center_id")
    private Integer bowlingCenterId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(name = "open_days", length = 100)
    private String openDays;

    @Column(name = "open_hours", length = 100)
    private String openHours;

    @Column(name = "social_links", columnDefinition = "TEXT")
    private String socialLinks;

    @Column(nullable = false)
    private Boolean status = true;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = this.status == null ? true : this.status;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
