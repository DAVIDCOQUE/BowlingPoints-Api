package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    List<Tournament> findAllByDeletedAtIsNull(); // <- solo activos (no eliminados)
}