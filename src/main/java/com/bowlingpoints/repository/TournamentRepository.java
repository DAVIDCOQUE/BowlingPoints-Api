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

    @Query("""
    SELECT new com.bowlingpoints.dto.TournamentDTO(
        t.tournamentId,
        t.name,
        t.location,
        m.name,
        a.name,
        t.startDate
    )
    FROM Tournament t
    LEFT JOIN t.ambit a
    LEFT JOIN t.modalities tm
    LEFT JOIN tm.modality m
    WHERE t.status = true AND t.deletedAt IS NULL
""")
    List<TournamentDTO> findActiveTournaments();
}