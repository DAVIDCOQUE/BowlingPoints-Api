package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentModality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentModalityRepository extends JpaRepository<TournamentModality, Integer> {

    // Buscar todas las modalidades asociadas a un torneo específico
    List<TournamentModality> findByTournament_TournamentId(Integer tournamentId);

}
