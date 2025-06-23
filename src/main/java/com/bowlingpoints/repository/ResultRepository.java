package com.bowlingpoints.repository;

import com.bowlingpoints.dto.PlayerRankingDTO;
import com.bowlingpoints.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    @Query("""
    SELECT new com.bowlingpoints.dto.PlayerRankingDTO(
        r.person.personId,
        CONCAT(p.firstName, ' ', p.lastname),
        AVG(r.score),
        p.photoUrl
    )
    FROM Result r
    JOIN r.person p
    WHERE r.deletedAt IS NULL
    GROUP BY r.person.personId, p.firstName, p.lastname
    ORDER BY AVG(r.score) DESC
    """)
    List<PlayerRankingDTO> findTop10PlayersByAvgScore();

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
}
