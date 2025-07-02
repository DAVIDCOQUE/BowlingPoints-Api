package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentCategoryRepository extends JpaRepository<TournamentCategory, Integer> {
    List<TournamentCategory> findByTournament_TournamentId(Integer tournamentId);
}
