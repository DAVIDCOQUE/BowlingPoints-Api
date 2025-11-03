package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentCategoryRepository extends JpaRepository<TournamentCategory, Integer> {

    // Buscar todas las categorías asociadas a un torneo específico por su ID
    List<TournamentCategory> findByTournament_TournamentId(Integer tournamentId);
}
