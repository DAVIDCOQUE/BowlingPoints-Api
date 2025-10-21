package com.bowlingpoints.repository;

import com.bowlingpoints.entity.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Integer> {

    // Verificar si ya existe una inscripción de la misma persona en el torneo
    boolean existsByTournament_TournamentIdAndPerson_PersonId(Integer tournamentId, Integer personId);

    // Obtener todas las inscripciones activas por torneo
    List<TournamentRegistration> findByTournament_TournamentIdAndStatusTrue(Integer tournamentId);

    // Obtener todas las inscripciones activas por persona
    List<TournamentRegistration> findByPerson_PersonIdAndStatusTrue(Integer personId);

    // Obtener todas las inscripciones activas
    List<TournamentRegistration> findByStatusTrue();

    // Obtener todas las inscripciones por torneo
    List<TournamentRegistration> findByTournament_TournamentId(Integer tournamentId);
}
