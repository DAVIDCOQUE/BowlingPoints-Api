package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentBranchRepository extends JpaRepository<TournamentBranch, Integer> {

    /**
     * Obtiene todas las relaciones de rama asociadas a un torneo.
     */
    List<TournamentBranch> findByTournament_TournamentId(Integer tournamentId);

}
