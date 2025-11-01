package com.bowlingpoints.service;

import com.bowlingpoints.dto.*;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para DashboardService.
 */
class DashboardServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ClubRepository clubRepository; // no usado directamente, pero lo requiere el constructor

    @Mock
    private AmbitRepository ambitRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Tournament tournament;
    private Ambit ambit;
    private Category category;
    private Modality modality;
    private TournamentCategory tournamentCategory;
    private TournamentModality tournamentModality;
    private DashboardPlayerDTO playerDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ambit = Ambit.builder()
                .ambitId(1)
                .name("Nacional")
                .build();

        category = Category.builder()
                .categoryId(10)
                .name("Elite")
                .description("Jugadores expertos")
                .status(true)
                .build();

        modality = Modality.builder()
                .modalityId(20)
                .name("Individual")
                .description("Competencia 1 vs 1")
                .status(true)
                .build();

        tournamentCategory = TournamentCategory.builder()
                .category(category)
                .build();

        tournamentModality = TournamentModality.builder()
                .modality(modality)
                .build();

        tournament = Tournament.builder()
                .tournamentId(1)
                .name("Open Nacional")
                .organizer("Federación Colombiana")
                .ambit(ambit)
                .imageUrl("open.jpg")
                .location("Cali")
                .stage("Programado")
                .status(true)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 5))
                .categories(List.of(tournamentCategory))
                .modalities(List.of(tournamentModality))
                .build();

        playerDTO = DashboardPlayerDTO.builder()
                .fullName("John Doe")
                .averageScore(225.5)
                .build();
    }

    // ----------------------------------------------------------------------
    // getDashboardData
    // ----------------------------------------------------------------------
    @Test
    void getDashboardData_ShouldReturnDashboardDTO_WithAllSectionsPopulated() {
        // Mock repositorios
        when(tournamentRepository.findActiveScheduledOrPostponed())
                .thenReturn(List.of(tournament));

        when(tournamentRepository.findActiveInProgress())
                .thenReturn(List.of(tournament));

        when(resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 10)))
                .thenReturn(List.of(playerDTO));

        when(ambitRepository.findDistinctWithTournaments())
                .thenReturn(List.of(AmbitDTO.builder()
                        .ambitId(1)
                        .name("Nacional")
                        .description("Ámbito nacional")
                        .status(true)
                        .build()));

        // Ejecutar método
        DashboardDTO dashboard = dashboardService.getDashboardData();

        // Validar
        assertNotNull(dashboard);
        assertEquals(1, dashboard.getScheduledOrPostponedTournaments().size());
        assertEquals(1, dashboard.getInProgressTournaments().size());
        assertEquals(1, dashboard.getTopPlayers().size());
        assertEquals(1, dashboard.getAmbits().size());

        // Validar mapeo de torneo
        TournamentDTO mapped = dashboard.getScheduledOrPostponedTournaments().get(0);
        assertEquals("Open Nacional", mapped.getName());
        assertEquals("Federación Colombiana", mapped.getOrganizer());
        assertEquals("Nacional", mapped.getAmbitName());
        assertEquals(List.of("Elite"), mapped.getCategoryNames());
        assertEquals(List.of("Individual"), mapped.getModalityNames());

        verify(tournamentRepository, times(1)).findActiveScheduledOrPostponed();
        verify(tournamentRepository, times(1)).findActiveInProgress();
        verify(resultRepository, times(1)).findTopPlayersByAvgScore(PageRequest.of(0, 10));
        verify(ambitRepository, times(1)).findDistinctWithTournaments();
    }

    @Test
    void getDashboardData_ShouldHandleEmptyResultsGracefully() {
        when(tournamentRepository.findActiveScheduledOrPostponed()).thenReturn(List.of());
        when(tournamentRepository.findActiveInProgress()).thenReturn(List.of());
        when(resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 10))).thenReturn(List.of());
        when(ambitRepository.findDistinctWithTournaments()).thenReturn(List.of());

        DashboardDTO dashboard = dashboardService.getDashboardData();

        assertNotNull(dashboard);
        assertTrue(dashboard.getScheduledOrPostponedTournaments().isEmpty());
        assertTrue(dashboard.getInProgressTournaments().isEmpty());
        assertTrue(dashboard.getTopPlayers().isEmpty());
        assertTrue(dashboard.getAmbits().isEmpty());
    }

    // ----------------------------------------------------------------------
    // toDTO (implícitamente probado dentro del dashboard principal)
    // ----------------------------------------------------------------------
    @Test
    void toDTO_ShouldMapTournamentEntityToDTO_Completely() {
        // Llamar indirectamente a toDTO (mediante getDashboardData)
        when(tournamentRepository.findActiveScheduledOrPostponed()).thenReturn(List.of(tournament));
        when(tournamentRepository.findActiveInProgress()).thenReturn(List.of());
        when(resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 10))).thenReturn(List.of());
        when(ambitRepository.findDistinctWithTournaments()).thenReturn(List.of());

        DashboardDTO dashboard = dashboardService.getDashboardData();

        TournamentDTO dto = dashboard.getScheduledOrPostponedTournaments().get(0);

        assertEquals(tournament.getTournamentId(), dto.getTournamentId());
        assertEquals(tournament.getName(), dto.getName());
        assertEquals(tournament.getAmbit().getAmbitId(), dto.getAmbitId());
        assertEquals(tournament.getAmbit().getName(), dto.getAmbitName());
        assertEquals(tournament.getCategories().get(0).getCategory().getCategoryId(), dto.getCategoryIds().get(0));
        assertEquals(tournament.getModalities().get(0).getModality().getModalityId(), dto.getModalityIds().get(0));
    }
}
