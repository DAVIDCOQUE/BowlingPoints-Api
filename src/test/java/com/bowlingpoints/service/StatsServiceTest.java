package com.bowlingpoints.service;

import com.bowlingpoints.dto.TopTournamentDTO;
import com.bowlingpoints.dto.UserStatisticsDTO;
import com.bowlingpoints.dto.UserStatsProjection;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.ResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private StatsService statsService;

    private Person testPerson;
    private UserStatsProjection testProjection;
    private Clubs testClub;
    private ClubPerson testClubPerson;

    @BeforeEach
    void setUp() {
        // Configurar Club
        testClub = Clubs.builder()
                .clubId(1)
                .name("Test Club")
                .build();

        // Configurar Person
        testPerson = Person.builder()
                .personId(1)
                .fullName("John")
                .fullSurname("Doe")
                .birthDate(LocalDate.now().minusYears(25))
                .photoUrl("/uploads/users/test.jpg")
                .build();

        // Configurar ClubPerson
        testClubPerson = ClubPerson.builder()
                .club(testClub)
                .person(testPerson)
                .status(true)
                .build();
        testPerson.setClubPersons(Collections.singletonList(testClubPerson));

        // Configurar UserStatsProjection mock
        testProjection = new UserStatsProjection() {
            @Override
            public Integer getTotalTournaments() {
                return 10;
            }

            @Override
            public Integer getTotalStrikes() {
                return 50;
            }

            @Override
            public Double getAvgScore() {
                return 180.5;
            }

            @Override
            public Integer getBestGame() {
                return 279;
            }

            @Override
            public Integer getTournamentsWon() {
                return 3;
            }
        };
    }

    @Test
    void calculateUserStats_WhenUserExistsWithCompleteData_ShouldReturnFullStats() {
        // Arrange
        when(resultRepository.findStatsByUserId(1)).thenReturn(testProjection);
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

        // Act
        UserStatisticsDTO result = statsService.calculateUserStats(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProjection.getTotalTournaments(), result.getTotalTournaments());
        assertEquals(testProjection.getTotalStrikes(), result.getTotalStrikes());
        assertEquals(testProjection.getAvgScore(), result.getAvgScore());
        assertEquals(testProjection.getBestGame(), result.getBestGame());
        assertEquals(testProjection.getTournamentsWon(), result.getTournamentsWon());
        assertEquals(testPerson.getPersonId(), result.getPersonId());
        assertEquals(testPerson.getFullName() + " " + testPerson.getFullSurname(), result.getFullName());
        assertEquals(testClub.getName(), result.getClub());
        assertEquals("25", result.getAge());
        assertEquals(testPerson.getPhotoUrl(), result.getPhotoUrl());
    }

    @Test
    void calculateUserStats_WhenUserDoesNotExist_ShouldReturnMinimalStats() {
        // Arrange
        when(resultRepository.findStatsByUserId(99)).thenReturn(null);
        when(personRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        UserStatisticsDTO result = statsService.calculateUserStats(99);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalTournaments());
        assertEquals(0, result.getTotalStrikes());
        assertEquals(0.0, result.getAvgScore());
        assertEquals(0, result.getBestGame());
        assertEquals(0, result.getTournamentsWon());
        assertNull(result.getPersonId());
        assertNull(result.getFullName());
        assertNull(result.getClub());
        assertNull(result.getAge());
        assertNull(result.getPhotoUrl());
    }

    @Test
    void calculateUserStats_WhenUserExistsWithoutBirthDate_ShouldReturnStatsWithoutAge() {
        // Arrange
        testPerson.setBirthDate(null);
        when(resultRepository.findStatsByUserId(1)).thenReturn(testProjection);
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

        // Act
        UserStatisticsDTO result = statsService.calculateUserStats(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProjection.getTotalTournaments(), result.getTotalTournaments());
        assertNull(result.getAge());
    }

    @Test
    void calculateUserStats_WhenUserExistsWithoutClub_ShouldReturnStatsWithoutClub() {
        // Arrange
        testPerson.setClubPersons(Collections.emptyList());
        when(resultRepository.findStatsByUserId(1)).thenReturn(testProjection);
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

        // Act
        UserStatisticsDTO result = statsService.calculateUserStats(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProjection.getTotalTournaments(), result.getTotalTournaments());
        assertNull(result.getClub());
    }

    @Test
    void calculateUserStats_WhenUserHasInactiveClub_ShouldReturnStatsWithoutClub() {
        // Arrange
        testClubPerson.setStatus(false);
        when(resultRepository.findStatsByUserId(1)).thenReturn(testProjection);
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

        // Act
        UserStatisticsDTO result = statsService.calculateUserStats(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProjection.getTotalTournaments(), result.getTotalTournaments());
        assertNull(result.getClub());
    }

    @Test
    void getTopTournaments_WhenTournamentsExist_ShouldReturnList() {
        // Arrange
        List<TopTournamentDTO> expectedTournaments = Arrays.asList(
            TopTournamentDTO.builder()
                .tournamentId(1)
                .name("Tournament 1")
                .startDate(LocalDate.now())
                .bestScore(200)
                .build(),
            TopTournamentDTO.builder()
                .tournamentId(2)
                .name("Tournament 2")
                .startDate(LocalDate.now())
                .bestScore(180)
                .build()
        );
        when(resultRepository.findTopTournamentsByUser(1)).thenReturn(expectedTournaments);

        // Act
        List<TopTournamentDTO> result = statsService.getTopTournaments(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedTournaments.get(0).getName(), result.get(0).getName());
        assertEquals(expectedTournaments.get(0).getBestScore(), result.get(0).getBestScore());
    }

    @Test
    void getTopTournaments_WhenNoTournaments_ShouldReturnEmptyList() {
        // Arrange
        when(resultRepository.findTopTournamentsByUser(1)).thenReturn(Collections.emptyList());

        // Act
        List<TopTournamentDTO> result = statsService.getTopTournaments(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}