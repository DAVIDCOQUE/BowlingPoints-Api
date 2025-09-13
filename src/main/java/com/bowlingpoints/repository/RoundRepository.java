package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Round;
import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Integer> {
    Optional<Round> findByTournamentAndRoundNumber(Tournament tournament, int roundNumber);

}
