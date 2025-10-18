package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.AmbitRepository;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.repository.ResultRepository;
import com.bowlingpoints.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private AmbitRepository ambitRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Tournament testTournament;
    private Ambit testAmbit;
    private Category testCategory;
    private Modality testModality;

    @BeforeEach
    void setUp() {
        // Configurar Ambit
        testAmbit = Ambit.builder()
                .ambitId(1)
                .name("Test Ambit")
                .build();

        // Configurar Category
        testCategory = Category.builder()
                .categoryId(1)
                .name("Test Category")
                .build();

        // Configurar Modality
        testModality = Modality.builder()
                .modalityId(1)
                .name("Test Modality")
                .build();

        // Configurar Tournament
        testTournament = Tournament.builder()
                .tournamentId(1)
                .name("Test Tournament")
                .ambit(testAmbit)
                .imageUrl("/uploads/tournaments/test.png")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("Test Location")
                .stage("Test Stage")
                .status(true)
                .build();

        // Configurar TournamentCategory
        TournamentCategory tournamentCategory = new TournamentCategory();
        tournamentCategory.setTournament(testTournament);
        tournamentCategory.setCategory(testCategory);

        // Configurar TournamentModality
        TournamentModality tournamentModality = new TournamentModality();
        tournamentModality.setTournament(testTournament);
        tournamentModality.setModality(testModality);

        testTournament.setCategories(Collections.singletonList(tournamentCategory));
        testTournament.setModalities(Collections.singletonList(tournamentModality));
    }

    @Test
    void getDashboardData_WhenDataExists_ShouldReturnCompleteDTO() {
        // Arrange
        when(tournamentRepository.findAllByStatusTrueAndDeletedAtIsNull())
                .thenReturn(Collections.singletonList(testTournament));

        PlayerRankingDTO playerRanking =
                PlayerRankingDTO.builder()
                        .personId(1)
                        .fullName("Test player")
                        .averageScore(180.0)
                        .photoUrl("/uploads/users/test.jpg")
                        .build();
        when(resultRepository.findTopPlayersByAvgScore(any(PageRequest.class)))
                .thenReturn(Collections.singletonList(playerRanking));

        Object[] clubData = new Object[]{1, "Test Club", 1000};
        when(resultRepository.findTopClubsRaw())
                .thenReturn(Collections.singletonList(clubData));

        AmbitDTO ambitDTO = AmbitDTO.builder()
                .ambitId(1)
                .name("Test Ambit")
                .imageUrl("/uploads/ambits/test.jpg")
                .status(true)
                .build();
        when(ambitRepository.findDistinctWithTournaments())
                .thenReturn(Collections.singletonList(ambitDTO));

        // Act
        DashboardDTO result = dashboardService.getDashboardData();

        // Assert
        assertNotNull(result);
        
        // Verify tournaments
        assertEquals(1, result.getActiveTournaments().size());
        TournamentDTO tournamentDTO = result.getActiveTournaments().get(0);
        assertEquals(testTournament.getTournamentId(), tournamentDTO.getTournamentId());
        assertEquals(testTournament.getName(), tournamentDTO.getName());
        assertEquals(testAmbit.getAmbitId(), tournamentDTO.getAmbitId());
        assertEquals(testAmbit.getName(), tournamentDTO.getAmbitName());
        
        // Verify categories and modalities
        assertEquals(1, tournamentDTO.getCategories().size());
        assertEquals(1, tournamentDTO.getModalities().size());
        assertEquals(testCategory.getName(), tournamentDTO.getCategories().get(0).getName());
        assertEquals(testModality.getName(), tournamentDTO.getModalities().get(0).getName());

        // Verify top players
        assertEquals(1, result.getTopPlayers().size());
        assertEquals(playerRanking.getPersonId(), result.getTopPlayers().get(0).getPersonId());
        assertEquals(playerRanking.getFullName(), result.getTopPlayers().get(0).getFullName());
        assertEquals(playerRanking.getAverageScore(), result.getTopPlayers().get(0).getAverageScore());

        // Verify top clubs
        assertEquals(1, result.getTopClubs().size());
        assertEquals(1, result.getTopClubs().get(0).getClubId());
        assertEquals("Test Club", result.getTopClubs().get(0).getName());
        assertEquals(1000, result.getTopClubs().get(0).getTotalScore());

        // Verify ambits
        assertEquals(1, result.getAmbits().size());
        assertEquals(ambitDTO.getAmbitId(), result.getAmbits().get(0).getAmbitId());
        assertEquals(ambitDTO.getName(), result.getAmbits().get(0).getName());
    }

    @Test
    void getDashboardData_WhenNoData_ShouldReturnEmptyLists() {
        // Arrange
        when(tournamentRepository.findAllByStatusTrueAndDeletedAtIsNull())
                .thenReturn(Collections.emptyList());
        when(resultRepository.findTopPlayersByAvgScore(any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(resultRepository.findTopClubsRaw())
                .thenReturn(Collections.emptyList());
        when(ambitRepository.findDistinctWithTournaments())
                .thenReturn(Collections.emptyList());

        // Act
        DashboardDTO result = dashboardService.getDashboardData();

        // Assert
        assertNotNull(result);
        assertTrue(result.getActiveTournaments().isEmpty());
        assertTrue(result.getTopPlayers().isEmpty());
        assertTrue(result.getTopClubs().isEmpty());
        assertTrue(result.getAmbits().isEmpty());
    }

    @Test
    void getDashboardData_WhenTournamentWithNullFields_ShouldHandleGracefully() {
        // Arrange
        Tournament tournamentWithNulls = Tournament.builder()
                .tournamentId(1)
                .name("Test Tournament")
                .status(true)
                .build();

        when(tournamentRepository.findAllByStatusTrueAndDeletedAtIsNull())
                .thenReturn(Collections.singletonList(tournamentWithNulls));
        when(resultRepository.findTopPlayersByAvgScore(any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(resultRepository.findTopClubsRaw())
                .thenReturn(Collections.emptyList());
        when(ambitRepository.findDistinctWithTournaments())
                .thenReturn(Collections.emptyList());

        // Act
        DashboardDTO result = dashboardService.getDashboardData();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getActiveTournaments().size());
        TournamentDTO tournamentDTO = result.getActiveTournaments().get(0);
        assertNull(tournamentDTO.getAmbitId());
        assertNull(tournamentDTO.getAmbitName());
        assertTrue(tournamentDTO.getCategories().isEmpty());
        assertTrue(tournamentDTO.getModalities().isEmpty());
    }

    @Test
    void getDashboardData_WhenNullValuesInClubData_ShouldHandleGracefully() {
        // Arrange
        when(tournamentRepository.findAllByStatusTrueAndDeletedAtIsNull())
                .thenReturn(Collections.emptyList());
        when(resultRepository.findTopPlayersByAvgScore(any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Club data with null score
        Object[] clubData = new Object[]{1, "Test Club", null};
        when(resultRepository.findTopClubsRaw())
                .thenReturn(Collections.singletonList(clubData));
        when(ambitRepository.findDistinctWithTournaments())
                .thenReturn(Collections.emptyList());

        // Act
        DashboardDTO result = dashboardService.getDashboardData();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTopClubs().size());
        ClubDashboardDTO clubDTO = result.getTopClubs().get(0);
        assertEquals(1, clubDTO.getClubId());
        assertEquals("Test Club", clubDTO.getName());
        assertEquals(0, clubDTO.getTotalScore()); // Should default to 0
    }
}