package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {

    List<Tournament> findAllByDeletedAtIsNullOrderByStartDateDesc();

    Optional<Tournament> findByName(String name);

    List<Tournament> findAllByStatusTrueAndDeletedAtIsNull();

    List<Tournament> findByAmbit_AmbitIdAndDeletedAtIsNull(Integer ambitId);


    // Torneos activos con estado PROGRAMADO o APLAZADO
    @Query("""
                SELECT t
                FROM Tournament t
                WHERE t.status = true
                  AND t.deletedAt IS NULL
                  AND LOWER(t.stage) IN ('programado', 'aplazado')
                ORDER BY t.startDate ASC
            """)
    List<Tournament> findActiveScheduledOrPostponed();

    // Torneos activos (status = true) con estado En curso
    @Query("""
                SELECT t
                FROM Tournament t
                WHERE t.status = true
                  AND t.deletedAt IS NULL
                  AND LOWER(t.stage) = 'en curso'
                ORDER BY t.startDate ASC
            """)
    List<Tournament> findActiveInProgress();
}

