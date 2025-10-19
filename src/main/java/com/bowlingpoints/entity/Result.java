package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa el resultado de una persona o equipo en un torneo,
 * incluyendo modalidad, categoría, ronda y puntaje.
 */
@Entity
@Table(name = "result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    /**
     * Participante del resultado.
     */
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    /**
     * Equipo asociado (opcional).
     */
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    /**
     * Torneo al que pertenece el resultado.
     */
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    /**
     * Ronda del torneo.
     */
    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    /**
     * Categoría en la que participa.
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Modalidad en la que compite.
     */
    @ManyToOne
    @JoinColumn(name = "modality_id")
    private Modality modality;

    /**
     * Rama (Masculina, Femenina, Mixta, etc.).
     */
    @Column(name = "rama", nullable = false)
    private String rama;

    /**
     * Número de carril.
     */
    @Column(name = "lane_number")
    private Integer laneNumber;

    /**
     * Número de línea.
     */
    @Column(name = "line_number")
    private Integer lineNumber;

    /**
     * Puntuación obtenida.
     */
    @Column(name = "score", nullable = false)
    private Integer score;

    // ==== Auditoría ====

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
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
