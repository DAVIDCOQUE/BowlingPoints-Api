package com.bowlingpoints.repository;

import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    List<Tournament> findAllByDeletedAtIsNullOrderByStartDateDesc(); // <- solo activos (no eliminados)

    Optional<Tournament> findByName(String name);  // <-- ¡AGREGA ESTA LÍNEA!

    List<Tournament> findAllByStatusTrueAndDeletedAtIsNull();


    @Query("""
                SELECT new com.bowlingpoints.dto.TournamentDTO(
                    t.tournamentId,
                    t.name,
                    t.organizer,
                    t.ambit.ambitId,
                    t.ambit.name,
                    t.imageUrl,
                    t.startDate,
                    t.endDate,
                    t.location,
                    t.stage,
                    t.status,
                    null,
                    null,
                    null,
                    null
                )
                FROM Tournament t
                WHERE (:ambitId IS NULL OR t.ambit.ambitId = :ambitId)
                  AND (:ambitName IS NULL OR t.ambit.name = :ambitName)
                  AND t.deletedAt IS NULL
                  ORDER BY t.startDate DESC
            """)
    List<TournamentDTO> findTournamentsByAmbit(
            @Param("ambitId") Integer ambitId,
            @Param("ambitName") String ambitName
    );

}