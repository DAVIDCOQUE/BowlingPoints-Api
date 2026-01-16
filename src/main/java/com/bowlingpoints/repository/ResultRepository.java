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

public interface ResultRepository extends JpaRepository<Result, Integer> {

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

    @Query("""
                SELECT r FROM Result r
                WHERE r.person.personId = :userId AND r.tournament.tournamentId = :tournamentId
                ORDER BY r.roundNumber, r.lineNumber
            """)
    List<Result> findResultsByPersonAndTournament(@Param("userId") Integer userId, @Param("tournamentId") Integer tournamentId);

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
                LEFT JOIN p.clubPersons cp ON cp.status = true AND cp.deletedAt IS NULL
                LEFT JOIN cp.club c
                LEFT JOIN r.team t
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.modality.modalityId = :modalityId
                  AND r.deletedAt IS NULL
                ORDER BY COALESCE(p.personId, t.teamId), r.roundNumber
            """)
    List<Object[]> findRawPlayerResultsForTable(
            @Param("tournamentId") Integer tournamentId,
            @Param("modalityId") Integer modalityId
    );

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

    @Query("""
                SELECT DISTINCT r.roundNumber
                FROM Result r
                WHERE r.tournament.tournamentId = :tournamentId
                  AND r.deletedAt IS NULL
                ORDER BY r.roundNumber
            """)
    List<Integer> findDistinctRoundsByTournament(@Param("tournamentId") Integer tournamentId);

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

    @Query("""
            SELECT 
                r.person.personId,
                CONCAT(p.fullName, ' ', p.fullSurname),
                COALESCE(MAX(c.name), 'Sin club'),
                r.modality.name,
                SUM(r.score),
                COUNT(r.lineNumber)
            FROM Result r
            JOIN r.person p
            LEFT JOIN p.clubPersons cp ON cp.status = true AND cp.deletedAt IS NULL
            LEFT JOIN cp.club c
            WHERE r.tournament.tournamentId = :tournamentId
              AND (:roundNumber IS NULL OR r.roundNumber = :roundNumber)
              AND (:branchId IS NULL OR r.branch.branchId = :branchId)
              AND r.deletedAt IS NULL
            GROUP BY
                r.person.personId,
                p.fullName,
                p.fullSurname,
                r.modality.name
            ORDER BY p.fullName
            """)
    List<Object[]> findPlayerTotalsByModalityAndBranch(
            @Param("tournamentId") Integer tournamentId,
            @Param("roundNumber") Integer roundNumber,
            @Param("branchId") Integer branchId
    );

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
