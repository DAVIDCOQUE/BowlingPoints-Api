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
 * Test unitario actualizado para DashboardService.
 * Ajustado para el comportamiento actual del servicio (sin nulls, listas vacías por defecto).
 */
class DashboardServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ResultRepository resultRepository;

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

        // 🔥 El DashboardService usa PageRequest.of(0, 5), no (0, 10)
        when(resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 5)))
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

        verify(tournamentRepository).findActiveScheduledOrPostponed();
        verify(tournamentRepository).findActiveInProgress();
        verify(resultRepository).findTopPlayersByAvgScore(PageRequest.of(0, 5));
        verify(ambitRepository).findDistinctWithTournaments();
    }

    @Test
    void getDashboardData_ShouldHandleEmptyResultsGracefully() {
        when(tournamentRepository.findActiveScheduledOrPostponed()).thenReturn(List.of());
        when(tournamentRepository.findActiveInProgress()).thenReturn(List.of());
        when(resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 5))).thenReturn(List.of());
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
        when(tournamentRepository.findActiveScheduledOrPostponed()).thenReturn(List.of(tournament));
        when(tournamentRepository.findActiveInProgress()).thenReturn(List.of());
        when(resultRepository.findTopPlayersByAvgScore(PageRequest.of(0, 5))).thenReturn(List.of());
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

    @Test
    void toDTO_ShouldHandleNullAmbitCategoriesAndModalitiesGracefully() throws Exception {
        Tournament tournamentWithNulls = Tournament.builder()
                .tournamentId(99)
                .name("Sin detalles")
                .organizer("Desconocido")
                .ambit(null)
                .categories(null)
                .modalities(null)
                .status(true)
                .build();

        var method = DashboardService.class.getDeclaredMethod("toDTO", Tournament.class);
        method.setAccessible(true);

        TournamentDTO dto = (TournamentDTO) method.invoke(dashboardService, tournamentWithNulls);

        // ✅ Ajuste: el servicio ahora devuelve listas vacías, no null
        assertNotNull(dto);
        assertEquals(99, dto.getTournamentId());
        assertNull(dto.getAmbitId());
        assertNull(dto.getAmbitName());
        assertNotNull(dto.getCategoryIds());
        assertTrue(dto.getCategoryIds().isEmpty());
        assertNotNull(dto.getCategoryNames());
        assertTrue(dto.getCategoryNames().isEmpty());
        assertNotNull(dto.getCategories());
        assertTrue(dto.getCategories().isEmpty());
        assertNotNull(dto.getModalityIds());
        assertTrue(dto.getModalityIds().isEmpty());
        assertNotNull(dto.getModalityNames());
        assertTrue(dto.getModalityNames().isEmpty());
        assertNotNull(dto.getModalities());
        assertTrue(dto.getModalities().isEmpty());
    }
}
