package com.bowlingpoints.service;

import com.bowlingpoints.dto.UserDashboardStatsDTO;
import com.bowlingpoints.entity.Modality;
import com.bowlingpoints.entity.Result;
import com.bowlingpoints.entity.Tournament;
import com.bowlingpoints.repository.ResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private StatsService statsService;

    private Tournament tournament1;
    private Tournament tournament2;
    private Modality modalityIndividual;
    private Modality modalityTeam;

    @BeforeEach
    void setUp() {
        tournament1 = new Tournament();
        tournament1.setTournamentId(1);
        tournament1.setName("Torneo 1");
        tournament1.setImageUrl("img1.png");
        tournament1.setStartDate(LocalDate.of(2024, 10, 10));

        tournament2 = new Tournament();
        tournament2.setTournamentId(2);
        tournament2.setName("Torneo 2");
        tournament2.setImageUrl("img2.png");
        tournament2.setStartDate(LocalDate.of(2024, 12, 5));

        modalityIndividual = new Modality();
        modalityIndividual.setName("Individual");

        modalityTeam = new Modality();
        modalityTeam.setName("Parejas");
    }

    @Test
    void getUserDashboardStats_WhenNoResults_ShouldReturnZerosAndEmptyLists() {
        when(resultRepository.findByPersonId(1)).thenReturn(Collections.emptyList());

        UserDashboardStatsDTO stats = statsService.getUserDashboardStats(1);

        assertNotNull(stats);
        assertEquals(0.0, stats.getAvgScoreGeneral());
        assertEquals(0, stats.getBestLine());
        assertEquals(0, stats.getTotalLines());
        assertEquals(0, stats.getTotalTournaments());
        assertTrue(stats.getAvgPerTournament().isEmpty());
        assertTrue(stats.getAvgPerModality().isEmpty());
        assertTrue(stats.getScoreDistribution().isEmpty());
    }

    @Test
    void getUserDashboardStats_WhenResultsExist_ShouldReturnCalculatedStats() {
        Result r1 = new Result();
        r1.setScore(200);
        r1.setTournament(tournament1);
        r1.setModality(modalityIndividual);

        Result r2 = new Result();
        r2.setScore(180);
        r2.setTournament(tournament1);
        r2.setModality(modalityIndividual);

        Result r3 = new Result();
        r3.setScore(220);
        r3.setTournament(tournament2);
        r3.setModality(modalityTeam);

        when(resultRepository.findByPersonId(1)).thenReturn(List.of(r1, r2, r3));

        UserDashboardStatsDTO stats = statsService.getUserDashboardStats(1);

        assertNotNull(stats);
        assertEquals(200.0, stats.getAvgScoreGeneral(), 0.01);
        assertEquals(220, stats.getBestLine());
        assertEquals(3, stats.getTotalLines());
        assertEquals(2, stats.getTotalTournaments());
        assertEquals(2, stats.getAvgPerTournament().size());
        assertEquals(2, stats.getAvgPerModality().size());
        assertEquals(6, stats.getScoreDistribution().size()); // Rango completo 0–129 ... 251–300

        // Validar orden y valores del mejor torneo
        assertNotNull(stats.getBestTournamentAvg());
        assertEquals("Torneo 2", stats.getBestTournamentAvg().getTournamentName());
    }

    @Test
    void getUserDashboardStats_WhenAllResultsLowScores_ShouldReturnCorrectDistribution() {
        Result r1 = new Result();
        r1.setScore(100);
        r1.setTournament(tournament1);
        r1.setModality(modalityIndividual);

        Result r2 = new Result();
        r2.setScore(140);
        r2.setTournament(tournament1);
        r2.setModality(modalityIndividual);

        Result r3 = new Result();
        r3.setScore(160);
        r3.setTournament(tournament1);
        r3.setModality(modalityIndividual);

        when(resultRepository.findByPersonId(5)).thenReturn(List.of(r1, r2, r3));

        UserDashboardStatsDTO stats = statsService.getUserDashboardStats(5);

        assertEquals(133.33, stats.getAvgScoreGeneral(), 0.5);
        assertEquals(160, stats.getBestLine());
        assertEquals(3, stats.getTotalLines());
        assertEquals(1, stats.getTotalTournaments());

        // Validar distribución de puntajes (debe tener conteo en los rangos bajos)
        var ranges = stats.getScoreDistribution();
        long lowCount = ranges.stream()
                .filter(r -> r.getLabel().equals("0–129"))
                .findFirst().get().getCount();
        assertEquals(1L, lowCount);
    }
}
