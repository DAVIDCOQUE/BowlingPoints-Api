package com.bowlingpoints.repository;

import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    List<Tournament> findAllByDeletedAtIsNull(); // <- solo activos (no eliminados)
    Optional<Tournament> findByName(String name);  // <-- ¡AGREGA ESTA LÍNEA!
    List<Tournament> findAllByStatusTrueAndDeletedAtIsNull();
}