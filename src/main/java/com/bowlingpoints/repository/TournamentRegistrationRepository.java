package com.bowlingpoints.repository;

import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.entity.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, Integer> {

    boolean existsByTeam_TeamId(Integer teamId);


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


    // Obtener torneos en los que ha participado una persona específica

    @Query("SELECT DISTINCT tr.tournament FROM TournamentRegistration tr WHERE tr.person.personId = :personId")
    List<Tournament> findTournamentsByPersonId(@Param("personId") Integer personId);



    // Verificar si ya existe una inscripción de la misma persona en la misma modalidad y torneo
    boolean existsByTournament_TournamentIdAndModality_ModalityIdAndPerson_PersonId(Integer tournamentId, Integer modalityId, Integer personId);

}
