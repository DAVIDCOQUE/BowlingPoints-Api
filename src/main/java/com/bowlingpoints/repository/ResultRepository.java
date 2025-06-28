package com.bowlingpoints.repository;

import com.bowlingpoints.dto.PlayerRankingDTO;
import com.bowlingpoints.dto.TopTournamentDTO;
import com.bowlingpoints.dto.UserStatsProjection;
import com.bowlingpoints.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    // 1. Ranking de jugadores
    @Query("""
                SELECT new com.bowlingpoints.dto.PlayerRankingDTO(
                    r.person.personId,
                    CONCAT(p.fullName, ' ', p.fullSurname),
                    AVG(r.score),
                    p.photoUrl
                )
                FROM Result r
                JOIN r.person p
                WHERE r.deletedAt IS NULL
                GROUP BY r.person.personId, p.fullName, p.fullSurname, p.photoUrl
                ORDER BY AVG(r.score) DESC
            """)
    List<PlayerRankingDTO> findTop10PlayersByAvgScore();

    // 2. Ranking de clubes (Nativo)
    @Query(value = """
                SELECT
                    c.club_id AS clubId,
                    c.name AS name,
                    SUM(r.score) AS totalScore
                FROM bowlingpoints.result r
                JOIN bowlingpoints.person p ON r.person_id = p.person_id
                JOIN bowlingpoints.club_person cp ON cp.person_id = p.person_id
                JOIN bowlingpoints.clubs c ON c.club_id = cp.club_id
                WHERE r.deleted_at IS NULL
                GROUP BY c.club_id, c.name
                ORDER BY totalScore DESC
                LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTopClubsRaw();

    // 3. Torneos jugados por usuario (se puede hacer DTO si quieres)
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

    // 4. Detalle de resultados de usuario en torneo
    @Query("""
                SELECT r FROM Result r
                WHERE r.person.personId = :userId AND r.tournament.tournamentId = :tournamentId
                ORDER BY r.round.roundNumber, r.lineNumber
            """)
    List<Result> findResultsByPersonAndTournament(@Param("userId") Integer userId, @Param("tournamentId") Integer tournamentId);

    // 5. Estadísticas agregadas del usuario (proyección)
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

    // 6. Top 3 mejores torneos jugados (por puntaje más alto en cada torneo)
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
}
