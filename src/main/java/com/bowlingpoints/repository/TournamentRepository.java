package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {

    List<Tournament> findAllByDeletedAtIsNullOrderByStartDateDesc();

    Optional<Tournament> findByName(String name);

    List<Tournament> findAllByStatusTrueAndDeletedAtIsNull();

    List<Tournament> findByAmbit_AmbitIdAndDeletedAtIsNull(Integer ambitId);
}
