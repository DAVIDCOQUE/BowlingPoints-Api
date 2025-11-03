package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_category_id")
    private Integer tournamentCategoryId;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
