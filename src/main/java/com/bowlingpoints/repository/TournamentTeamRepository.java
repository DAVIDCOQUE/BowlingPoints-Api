package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Integer> {

    Optional<TournamentTeam> findByTournament_TournamentIdAndTeam_TeamId(Integer tournamentId, Integer teamId);

    List<TournamentTeam> findAllByTournament_TournamentIdAndStatusTrue(Integer tournamentId);
}
