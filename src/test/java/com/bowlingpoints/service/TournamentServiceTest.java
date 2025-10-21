package com.bowlingpoints.service;

import com.bowlingpoints.dto.TournamentDTO;
import com.bowlingpoints.entity.*;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private AmbitRepository ambitRepository;
    @Mock
    private ModalityRepository modalityRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TournamentCategoryRepository tournamentCategoryRepository;
    @Mock
    private TournamentModalityRepository tournamentModalityRepository;
    @Mock
    private ResultRepository resultRepository;

    @InjectMocks
    private TournamentService tournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAll_ShouldReturnAllTournaments() {
        // Arrange
        Tournament tournament1 = Tournament.builder()
                .tournamentId(1)
                .name("Tournament 1")
                .organizer("Organizer 1")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("Location 1")
                .stage("Stage 1")
                .status(true)
                .build();

        Tournament tournament2 = Tournament.builder()
                .tournamentId(2)
                .name("Tournament 2")
                .organizer("Organizer 2")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("Location 2")
                .stage("Stage 2")
                .status(true)
                .build();

        when(tournamentRepository.findAllByDeletedAtIsNullOrderByStartDateDesc())
                .thenReturn(Arrays.asList(tournament1, tournament2));

        // Act
        List<TournamentDTO> result = tournamentService.getAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Tournament 1", result.get(0).getName());
        assertEquals("Tournament 2", result.get(1).getName());
        verify(tournamentRepository).findAllByDeletedAtIsNullOrderByStartDateDesc();
    }

    @Test
    void getById_WhenTournamentExists_ShouldReturnTournament() {
        // Arrange
        Integer tournamentId = 1;
        Tournament tournament = Tournament.builder()
                .tournamentId(tournamentId)
                .name("Test Tournament")
                .organizer("Test Organizer")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("Test Location")
                .stage("Test Stage")
                .status(true)
                .build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));

        // Act
        TournamentDTO result = tournamentService.getById(tournamentId);

        // Assert
        assertNotNull(result);
        assertEquals(tournamentId, result.getTournamentId());
        assertEquals("Test Tournament", result.getName());
        verify(tournamentRepository).findById(tournamentId);
    }

    @Test
    void getById_WhenTournamentDoesNotExist_ShouldReturnNull() {
        // Arrange
        Integer tournamentId = 1;
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act
        TournamentDTO result = tournamentService.getById(tournamentId);

        // Assert
        assertNull(result);
        verify(tournamentRepository).findById(tournamentId);
    }

    @Test
    void create_ShouldCreateNewTournament() {
        // Arrange
        Integer ambitId = 1;
        Ambit ambit = Ambit.builder()
                .ambitId(ambitId)
                .name("Test Ambit")
                .build();

        TournamentDTO dto = TournamentDTO.builder()
                .name("New Tournament")
                .organizer("New Organizer")
                .ambitId(ambitId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("New Location")
                .stage("New Stage")
                .status(true)
                .build();

        when(ambitRepository.findById(ambitId)).thenReturn(Optional.of(ambit));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
            Tournament savedTournament = invocation.getArgument(0);
            savedTournament.setTournamentId(1);
            return savedTournament;
        });

        // Act
        TournamentDTO result = tournamentService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("New Tournament", result.getName());
        assertEquals(ambitId, result.getAmbitId());
        verify(tournamentRepository).save(any(Tournament.class));
        verify(ambitRepository).findById(ambitId);
    }

    @Test
    void update_WhenTournamentExists_ShouldUpdateTournament() {
        // Arrange
        Integer tournamentId = 1;
        Integer ambitId = 1;
        
        Tournament existingTournament = Tournament.builder()
                .tournamentId(tournamentId)
                .name("Old Name")
                .organizer("Old Organizer")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("Old Location")
                .stage("Old Stage")
                .status(true)
                .build();

        Ambit ambit = Ambit.builder()
                .ambitId(ambitId)
                .name("Test Ambit")
                .build();

        TournamentDTO updateDto = TournamentDTO.builder()
                .name("Updated Name")
                .organizer("Updated Organizer")
                .ambitId(ambitId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .location("Updated Location")
                .stage("Updated Stage")
                .status(true)
                .build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(existingTournament));
        when(ambitRepository.findById(ambitId)).thenReturn(Optional.of(ambit));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(existingTournament);

        // Act
        boolean result = tournamentService.update(tournamentId, updateDto);

        // Assert
        assertTrue(result);
        verify(tournamentRepository).findById(tournamentId);
        verify(ambitRepository).findById(ambitId);
        verify(tournamentRepository).save(any(Tournament.class));
        verify(tournamentCategoryRepository).deleteAll(any());
        verify(tournamentModalityRepository).deleteAll(any());

        ArgumentCaptor<Tournament> tournamentCaptor = ArgumentCaptor.forClass(Tournament.class);
        verify(tournamentRepository).save(tournamentCaptor.capture());
        Tournament updatedTournament = tournamentCaptor.getValue();
        assertEquals("Updated Name", updatedTournament.getName());
        assertEquals("Updated Organizer", updatedTournament.getOrganizer());
        assertEquals("Updated Location", updatedTournament.getLocation());
    }

    @Test
    void update_WhenTournamentDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Integer tournamentId = 1;
        TournamentDTO updateDto = new TournamentDTO();
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act
        boolean result = tournamentService.update(tournamentId, updateDto);

        // Assert
        assertFalse(result);
        verify(tournamentRepository).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void delete_WhenTournamentExists_ShouldMarkAsDeleted() {
        // Arrange
        Integer tournamentId = 1;
        Tournament tournament = Tournament.builder()
                .tournamentId(tournamentId)
                .name("Test Tournament")
                .status(true)
                .build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        // Act
        boolean result = tournamentService.delete(tournamentId);

        // Assert
        assertTrue(result);
        verify(tournamentRepository).findById(tournamentId);
        
        ArgumentCaptor<Tournament> tournamentCaptor = ArgumentCaptor.forClass(Tournament.class);
        verify(tournamentRepository).save(tournamentCaptor.capture());
        Tournament deletedTournament = tournamentCaptor.getValue();
        assertFalse(deletedTournament.getStatus());
        assertNotNull(deletedTournament.getDeletedAt());
    }

    @Test
    void delete_WhenTournamentDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Integer tournamentId = 1;
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        // Act
        boolean result = tournamentService.delete(tournamentId);

        // Assert
        assertFalse(result);
        verify(tournamentRepository).findById(tournamentId);
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void getTournamentSummary_ShouldReturnCorrectSummary() {
        // Arrange
        Integer tournamentId = 1;
        Tournament tournament = Tournament.builder()
                .tournamentId(tournamentId)
                .name("Test Tournament")
                .organizer("Test Organizer")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .location("Test Location")
                .build();

        TournamentModality tournamentModality = new TournamentModality();
        Modality modality = new Modality();
        modality.setName("Test Modality");
        tournamentModality.setModality(modality);

        TournamentCategory tournamentCategory = new TournamentCategory();
        Category category = new Category();
        category.setName("Test Category");
        tournamentCategory.setCategory(category);

        tournament.setModalities(Collections.singletonList(tournamentModality));
        tournament.setCategories(Collections.singletonList(tournamentCategory));

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(resultRepository.countPlayersByGenderInTournament(tournamentId))
                .thenReturn(new Object[]{5, 3}); // 5 masculine, 3 feminine

        // Act
        TournamentSummaryDTO result = tournamentService.getTournamentSummary(tournamentId);

        // Assert
        assertNotNull(result);
        assertEquals(tournamentId, result.getTournamentId());
        assertEquals("Test Tournament", result.getTournamentName());
        assertEquals("Test Organizer", result.getOrganizer());
        assertEquals(1, result.getModalities().size());
        assertEquals("Test Modality", result.getModalities().get(0));
        assertEquals(1, result.getCategories().size());
        assertEquals("Test Category", result.getCategories().get(0));
        assertEquals(5, result.getTotalMasculino());
        assertEquals(3, result.getTotalFemenino());
    }
}