package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_modality")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentModality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_modality_id")
    private Integer tournamentModalityId;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "modality_id", nullable = false)
    private Modality modality;
}
