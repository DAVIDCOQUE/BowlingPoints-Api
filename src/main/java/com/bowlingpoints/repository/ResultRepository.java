package com.bowlingpoints.repository;

import com.bowlingpoints.dto.DashboardPlayerDTO;
import com.bowlingpoints.dto.TopTournamentDTO;
import com.bowlingpoints.dto.TournamentBranchPlayerCountDTO;
import com.bowlingpoints.dto.UserStatsProjection;
import com.bowlingpoints.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * Repositorio para consultas complejas y personalizadas de la entidad Result.
 * Incluye rankings, resúmenes, estadísticas y datos para tablas comparativas.
 */
public interface ResultRepository extends JpaRepository<Result, Integer> {

    // -------------------------------------------------------------------------
    // 1. RANKING DE JUGADORES POR PROMEDIO
    // -------------------------------------------------------------------------

    /**
     * Devuelve los jugadores con mejor promedio de score.
     * Si usas Pageable, puedes limitar a top 10, top 50, etc.
     * Ejemplo: findTopPlayersByAvgScore(PageRequest.of(0, 10))
     */
    @Query("""
                SELECT new com.bowlingpoints.dto.DashboardPlayerDTO(
                    r.person.personId,
                    CONCAT(p.fullName, ' ', p.fullSurname),
                    AVG(r.score),
                    MAX(r.score),
                    COUNT(DISTINCT t.tournamentId),
                    p.photoUrl
                )
                FROM Result r
                JOIN r.person p
                JOIN r.tournament t
                WHERE r.deletedAt IS NULL
                GROUP BY r.person.personId, p.fullName, p.fullSurname, p.photoUrl
                ORDER BY AVG(r.score) DESC
            """)
    List<DashboardPlayerDTO> findTopPlayersByAvgScore(Pageable pageable);

    /**
     * Devuelve todos los jugadores ordenados por promedio de score (¡ojo si hay miles!).
     */
    @Query("""
                SELECT new com.bowlingpoints.dto.DashboardPlayerDTO(
                    r.person.personId,
                    CONCAT(p.fullName, ' ', p.fullSurname),
                    AVG(r.score),
                    MAX(r.score),
                    COUNT(DISTINCT t.tournamentId),
                    p.photoUrl
                )
                FROM Result r
                JOIN r.person p
                JOIN r.tournament t
                WHERE r.deletedAt IS NULL
                GROUP BY r.person.personId, p.fullName, p.fullSurname, p.photoUrl
                ORDER BY AVG(r.score) DESC
            """)
    List<DashboardPlayerDTO> findAllPlayersByAvgScore();

    // -------------------------------------------------------------------------
    // 2. CONTAR JUGADORES POR RAMA EN UN TORNEO
    // -------------------------------------------------------------------------

    @Query("""
                SELECT new com.bowlingpoints.dto.TournamentBranchPlayerCountDTO(
                    r.branch.branchId,
                    r.branch.name,
                    COUNT(DISTINCT r.person.personId)
                )
                FROM Result r
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.deletedAt IS NULL
                  AND r.branch IS NOT NULL
                GROUP BY r.branch.branchId, r.branch.name
            """)
    List<TournamentBranchPlayerCountDTO> countPlayersByBranch(@Param("tournamentId") Integer tournamentId);

    // -------------------------------------------------------------------------
    // 3. TORNEOS JUGADOS POR UN USUARIO
    // -------------------------------------------------------------------------

    /**
     * Devuelve todos los torneos jugados por una persona (userId).
     * Útil para la sección de "Mis Torneos".
     * Devuelve datos crudos, puedes armar DTO con esta info.
     */
    @Query("""
                SELECT
                    t.tournamentId,
                    t.name,
                    t.startDate,
                    t.location,
                    m.name,        
                    c.name,      
                    COUNT(r.resultId),
                    t.imageUrl
                FROM Result r
                JOIN r.tournament t
                JOIN r.modality m
                JOIN r.category c
                WHERE r.person.personId = :userId
                GROUP BY t.tournamentId, t.name, t.startDate, t.location, m.name, c.name, t.imageUrl
                ORDER BY t.startDate DESC
            """)
    List<Object[]> findTournamentsByPersonId(@Param("userId") Integer userId);

    // -------------------------------------------------------------------------
    // 4. DETALLE DE RESULTADOS DE UN USUARIO EN UN TORNEO
    // -------------------------------------------------------------------------

    /**
     * Devuelve la lista de Result para un usuario y torneo concreto.
     * Útil para el detalle/desglose de un torneo jugado.
     */
    @Query("""
                SELECT r FROM Result r
                WHERE r.person.personId = :userId AND r.tournament.tournamentId = :tournamentId
                ORDER BY r.roundNumber, r.lineNumber
            """)
    List<Result> findResultsByPersonAndTournament(@Param("userId") Integer userId, @Param("tournamentId") Integer tournamentId);

    // -------------------------------------------------------------------------
    // 5. ESTADÍSTICAS GLOBALES DEL USUARIO (proyección, no DTO)
    // -------------------------------------------------------------------------

    /**
     * Devuelve estadísticas globales para un usuario:
     * - Total torneos
     * - Total chuzas/strikes
     * - Promedio
     * - Mejor partida
     * - Torneos ganados (ejemplo: puntaje perfecto)
     */
    @Query("""
                SELECT
                    COUNT(DISTINCT r.tournament.tournamentId) as totalTournaments,
                    SUM(CASE WHEN r.score = 300 THEN 1 ELSE 0 END) as totalStrikes,
                    AVG(r.score) as avgScore,
                    MAX(r.score) as bestGame,
                    SUM(CASE WHEN r.score = 300 THEN 1 ELSE 0 END) as tournamentsWon
                FROM Result r
                WHERE r.person.personId = :userId
            """)
    UserStatsProjection findStatsByUserId(@Param("userId") Integer userId);

    // -------------------------------------------------------------------------
    // 6. TOP TORNEOS JUGADOS (Mejor puntaje obtenido en cada torneo por el usuario)
    // -------------------------------------------------------------------------

    /**
     * Devuelve el top 3 de torneos jugados por un usuario, según mejor puntaje alcanzado.
     * Útil para resumen/estadísticas.
     */
    @Query("""
                SELECT new com.bowlingpoints.dto.TopTournamentDTO(
                    t.tournamentId,
                    t.name,
                    t.imageUrl,
                    t.startDate,
                    MAX(r.score)
                )
                FROM Result r
                JOIN r.tournament t
                WHERE r.person.personId = :userId
                GROUP BY t.tournamentId, t.name, t.imageUrl, t.startDate
                ORDER BY MAX(r.score) DESC
            """)
    List<TopTournamentDTO> findTopTournamentsByUser(@Param("userId") Integer userId);

    // -------------------------------------------------------------------------
    // 7. RESÚMENES POR JUGADOR/MODALIDAD EN TORNEO (para tablas comparativas)
    // -------------------------------------------------------------------------

    /**
     * Devuelve resúmenes agregados por jugador y modalidad en un torneo.
     * Útil para tablas estadísticas de un torneo.
     */
    @Query("""
                SELECT 
                    p.personId,
                    CONCAT(p.fullName, ' ', p.fullSurname),
                    p.gender,
                    m.modalityId,
                    m.name,
                    SUM(r.score),
                    AVG(r.score),
                    COUNT(r.resultId)
                FROM Result r
                JOIN r.person p
                JOIN r.modality m
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.deletedAt IS NULL
                GROUP BY p.personId, p.fullName, p.fullSurname, p.gender, m.modalityId, m.name
            """)
    List<Object[]> findPlayerModalitySummariesByTournament(@Param("tournamentId") Integer tournamentId);


    // -------------------------------------------------------------------------
    // 8. DETALLE PARA TABLA DE RESULTADOS DE UN TORNEO Y MODALIDAD
    // -------------------------------------------------------------------------

    /**
     * Devuelve datos crudos para armar tabla: persona, club, ronda, score.
     */
    @Query("""
                SELECT 
                    COALESCE(p.personId, 0),                                
                    CASE 
                        WHEN p.personId IS NOT NULL THEN CONCAT(p.fullName, ' ', p.fullSurname)
                        ELSE NULL
                    END,                                                    
                    CASE 
                        WHEN p.personId IS NOT NULL THEN cp.club.name 
                        ELSE NULL
                    END,                                                    
                    r.roundNumber,
                    r.score,
                    t.teamId,                                              
                    t.nameTeam                                              
                FROM Result r
                LEFT JOIN r.person p
                LEFT JOIN p.clubPersons cp
                LEFT JOIN r.team t
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.modality.modalityId = :modalityId
                  AND r.deletedAt IS NULL
                  AND (cp.status = true OR cp IS NULL)
                  AND (cp.deletedAt IS NULL OR cp IS NULL)
                ORDER BY COALESCE(p.personId, t.teamId), r.roundNumber
            """)
    List<Object[]> findRawPlayerResultsForTable(
            @Param("tournamentId") Integer tournamentId,
            @Param("modalityId") Integer modalityId
    );


    // -------------------------------------------------------------------------
    // 9. OBTENER RONDAS DISTINTAS EN UN TORNEO
    // -------------------------------------------------------------------------
    @Query("""
                SELECT DISTINCT r.roundNumber
                FROM Result r
                WHERE r.tournament.tournamentId = :tournamentId AND r.modality.modalityId = :modalityId AND r.deletedAt IS NULL
                ORDER BY r.roundNumber
            """)
    List<Integer> findDistinctRoundsByTournamentAndModality(
            @Param("tournamentId") Integer tournamentId,
            @Param("modalityId") Integer modalityId
    );

    // Versión sin modalidad

    @Query("""
                SELECT DISTINCT r.roundNumber
                FROM Result r
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.deletedAt IS NULL
                ORDER BY r.roundNumber
            """)
    List<Integer> findDistinctRoundsByTournament(@Param("tournamentId") Integer tournamentId);

    // -------------------------------------------------------------------------
    // 11. OBTENER PROMEDIO GENERAL EN UN TORNEO Y MODALIDAD
    // -------------------------------------------------------------------------

    @Query("""
                SELECT AVG(r.score)
                FROM Result r
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.modality.modalityId = :modalityId
                  AND (:roundNumber IS NULL OR r.roundNumber = :roundNumber)
                  AND r.deletedAt IS NULL
            """)
    Double findAvgByRound(
            @Param("tournamentId") Integer tournamentId,
            @Param("modalityId") Integer modalityId,
            @Param("roundNumber") Integer roundNumber
    );

    // -------------------------------------------------------------------------
    // 12. OBTENER LA MEJOR PARTIDA EN UN TORNEO Y MODALIDAD
    // -------------------------------------------------------------------------

    @Query("""
                SELECT 'L' || r.lineNumber, AVG(r.score)
                FROM Result r
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.modality.modalityId = :modalityId
                  AND (:roundNumber IS NULL OR r.roundNumber = :roundNumber)
                  AND r.deletedAt IS NULL
                GROUP BY r.lineNumber
            """)
    List<Object[]> findAvgByLineRaw(
            @Param("tournamentId") Integer tournamentId,
            @Param("modalityId") Integer modalityId,
            @Param("roundNumber") Integer roundNumber
    );

    // -------------------------------------------------------------------------
    // 13. OBTENER LA MEJOR PARTIDA EN UN TORNEO Y MODALIDAD
    // -------------------------------------------------------------------------

    @Query("""
                SELECT r.score, CONCAT(p.fullName, ' ', p.fullSurname), r.lineNumber
                FROM Result r
                JOIN r.person p
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.modality.modalityId = :modalityId
                  AND (:roundNumber IS NULL OR r.roundNumber = :roundNumber)
                  AND r.deletedAt IS NULL
                ORDER BY r.score DESC
            """)
    List<Object[]> findHighestLine(
            @Param("tournamentId") Integer tournamentId,
            @Param("modalityId") Integer modalityId,
            @Param("roundNumber") Integer roundNumber
    );

    // -------------------------------------------------------------------------
// 15. OBTENER TOTALES POR JUGADOR, MODALIDAD Y RAMA EN UN TORNEO
    // -------------------------------------------------------------------------
    @Query("""
                SELECT 
                    r.person.personId,
                    CONCAT(p.fullName, ' ', p.fullSurname),
                    cp.club.name,
                    r.modality.name,
                    SUM(r.score),
                    COUNT(r.lineNumber)
                FROM Result r
                JOIN r.person p
                LEFT JOIN p.clubPersons cp
                WHERE r.tournament.tournamentId = :tournamentId
                  AND (:roundNumber IS NULL OR r.roundNumber = :roundNumber)
                  AND (:branchId IS NULL OR r.branch.branchId = :branchId)
                  AND r.deletedAt IS NULL
                  AND (cp.status = true OR cp IS NULL)
                  AND (cp.deletedAt IS NULL OR cp IS NULL)
                GROUP BY r.person.personId, p.fullName, p.fullSurname, cp.club.name, r.modality.name
                ORDER BY p.fullName
            """)
    List<Object[]> findPlayerTotalsByModalityAndBranch(
            @Param("tournamentId") Integer tournamentId,
            @Param("roundNumber") Integer roundNumber,
            @Param("branchId") Integer branchId
    );


    //------------------------------------------------------------------------
    // 16. OBTENER TODOS LOS RESULTADOS DE UN USUARIO
    //------------------------------------------------------------------------
    @Query("""
                SELECT r
                FROM Result r
                WHERE r.person.personId = :userId
                  AND r.deletedAt IS NULL
            """)
    List<Result> findByPersonId(@Param("userId") Integer userId);

    //------------------------------------------------------------------------
    // 17. DETECTAR DUPLICADOS EN IMPORTACIÓN MASIVA
    //------------------------------------------------------------------------
    /**
     * Verifica si ya existe un resultado para la combinación:
     * persona + torneo + número de ronda + número de línea
     * Usado para evitar duplicados en la importación masiva.
     */
    boolean existsByPerson_PersonIdAndTournament_TournamentIdAndRoundNumberAndLineNumber(
            Integer personId, Integer tournamentId, Integer roundNumber, Integer lineNumber
    );
}
