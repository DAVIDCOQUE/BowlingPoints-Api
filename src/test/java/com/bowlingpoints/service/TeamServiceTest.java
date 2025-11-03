package com.bowlingpoints.service;

import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Team;
import com.bowlingpoints.entity.TeamPerson;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.TeamPersonRepository;
import com.bowlingpoints.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private TeamPersonRepository teamPersonRepository;

    @InjectMocks
    private TeamService teamService;

    private Team testTeam;
    private Person testPerson;
    private TeamPerson testTeamPerson;
    private TeamDTO testTeamDTO;

    @BeforeEach
    void setUp() {
        // Configurar Person
        testPerson = Person.builder()
                .personId(1)
                .fullName("John")
                .fullSurname("Doe")
                .build();

        // Configurar Team
        testTeam = Team.builder()
                .teamId(1)
                .nameTeam("Test Team")
                .phone("1234567890")
                .status(true)
                .build();

        // Configurar TeamPerson
        testTeamPerson = TeamPerson.builder()
                .team(testTeam)
                .person(testPerson)
                .build();

        testTeam.setTeamPersons(Collections.singletonList(testTeamPerson));

        // Configurar TeamDTO
        testTeamDTO = TeamDTO.builder()
                .teamId(1)
                .nameTeam("Test Team")
                .phone("1234567890")
                .status(true)
                .personIds(Collections.singletonList(testPerson.getPersonId()))
                .build();
    }

    @Test
    void getAll_WhenTeamsExist_ShouldReturnAllTeams() {
        // Arrange
        when(teamRepository.findAll())
                .thenReturn(Collections.singletonList(testTeam));

        // Act
        List<TeamDTO> results = teamService.getAll();

        // Assert
        assertEquals(1, results.size());
        TeamDTO resultTeam = results.get(0);
        assertEquals(testTeam.getTeamId(), resultTeam.getTeamId());
        assertEquals(testTeam.getNameTeam(), resultTeam.getNameTeam());
        assertEquals(testTeam.getPhone(), resultTeam.getPhone());
        assertEquals(testTeam.getStatus(), resultTeam.getStatus());
        assertEquals(1, resultTeam.getPersonIds().size());
        assertEquals(testPerson.getPersonId(), resultTeam.getPersonIds().get(0));
    }

    @Test
    void getAll_WhenNoTeams_ShouldReturnEmptyList() {
        // Arrange
        when(teamRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act
        List<TeamDTO> results = teamService.getAll();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void getById_WhenTeamExists_ShouldReturnTeamDTO() {
        // Arrange
        when(teamRepository.findById(1))
                .thenReturn(Optional.of(testTeam));

        // Act
        TeamDTO result = teamService.getById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testTeam.getTeamId(), result.getTeamId());
        assertEquals(testTeam.getNameTeam(), result.getNameTeam());
        assertEquals(testTeam.getPhone(), result.getPhone());
        assertEquals(testTeam.getStatus(), result.getStatus());
        assertEquals(1, result.getPersonIds().size());
        assertEquals(testPerson.getPersonId(), result.getPersonIds().get(0));
    }

    @Test
    void getById_WhenTeamDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(teamRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act
        TeamDTO result = teamService.getById(99);

        // Assert
        assertNull(result);
    }

    @Test
    void create_WhenValidTeamDTO_ShouldReturnCreatedTeam() {
        // Arrange
        TeamDTO newTeamDTO = TeamDTO.builder()
                .nameTeam("New Team")
                .phone("0987654321")
                .personIds(Collections.singletonList(testPerson.getPersonId()))
                .build();

        Team savedTeam = Team.builder()
                .teamId(1)
                .nameTeam(newTeamDTO.getNameTeam())
                .phone(newTeamDTO.getPhone())
                .status(true)
                .build();

        when(teamRepository.save(any(Team.class)))
                .thenReturn(savedTeam);
        when(personRepository.findById(testPerson.getPersonId()))
                .thenReturn(Optional.of(testPerson));
        when(teamPersonRepository.saveAll(anyList()))
                .thenReturn(Collections.singletonList(testTeamPerson));

        // Act - LLAMAR AL SERVICIO
        TeamDTO result = teamService.create(newTeamDTO);  // ← ESTO FALTABA

        // Assert
        assertNotNull(result);
        assertEquals("New Team", result.getNameTeam());
        assertEquals("0987654321", result.getPhone());

        verify(teamRepository).save(argThat(team ->
                team.getNameTeam().equals(newTeamDTO.getNameTeam()) &&
                        team.getPhone().equals(newTeamDTO.getPhone()) &&
                        team.getStatus()
        ));
        verify(personRepository).findById(testPerson.getPersonId());
        verify(teamPersonRepository).saveAll(anyList());
    }

    @Test
    void create_WhenPersonNotFound_ShouldThrowException() {
        // Arrange
        when(teamRepository.save(any(Team.class)))
                .thenReturn(testTeam);
        when(personRepository.findById(99))
                .thenReturn(Optional.empty());

        TeamDTO newTeamDTO = TeamDTO.builder()
                .nameTeam("New Team")
                .phone("0987654321")
                .personIds(Collections.singletonList(99))
                .build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> teamService.create(newTeamDTO));
    }

    @Test
    void update_WhenTeamExists_ShouldReturnTrue() {
        // Arrange
        when(teamRepository.findById(1))
                .thenReturn(Optional.of(testTeam));
        when(personRepository.findById(testPerson.getPersonId()))
                .thenReturn(Optional.of(testPerson));

        // Act
        boolean result = teamService.update(1, testTeamDTO);

        // Assert
        assertTrue(result);
        verify(teamRepository).save(argThat(team ->
                team.getNameTeam().equals(testTeamDTO.getNameTeam()) &&
                team.getPhone().equals(testTeamDTO.getPhone()) &&
                team.getStatus().equals(testTeamDTO.getStatus())
        ));
        verify(teamPersonRepository).deleteAllByTeam_TeamId(1);
        verify(teamPersonRepository).saveAll(anyList());
    }

    @Test
    void update_WhenTeamDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(teamRepository.findById(99))
                .thenReturn(Optional.empty());

        // Act
        boolean result = teamService.update(99, testTeamDTO);

        // Assert
        assertFalse(result);
        verify(teamRepository, never()).save(any());
        verify(teamPersonRepository, never()).deleteAllByTeam_TeamId(any());
        verify(teamPersonRepository, never()).saveAll(any());
    }

    @Test
    void update_WhenPersonNotFound_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(1))
                .thenReturn(Optional.of(testTeam));
        when(personRepository.findById(99))
                .thenReturn(Optional.empty());

        testTeamDTO.setPersonIds(Collections.singletonList(99));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> teamService.update(1, testTeamDTO));
    }

    @Test
    void delete_WhenTeamExists_ShouldReturnTrue() {
        // Arrange
        when(teamRepository.existsById(1))
                .thenReturn(true);

        // Act
        boolean result = teamService.delete(1);

        // Assert
        assertTrue(result);
        verify(teamPersonRepository).deleteAllByTeam_TeamId(1);
        verify(teamRepository).deleteById(1);
    }

    @Test
    void delete_WhenTeamDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(teamRepository.existsById(99))
                .thenReturn(false);

        // Act
        boolean result = teamService.delete(99);

        // Assert
        assertFalse(result);
        verify(teamPersonRepository, never()).deleteAllByTeam_TeamId(any());
        verify(teamRepository, never()).deleteById(any());
    }

    @Test
    void getAll_WhenTeamHasNoMembers_ShouldHandleGracefully() {
        // Arrange: Team sin TeamPersons
        Team teamWithoutMembers = Team.builder()
                .teamId(10)
                .nameTeam("Solo Team")
                .phone("000")
                .status(true)
                .teamPersons(null)
                .build();

        when(teamRepository.findAll()).thenReturn(List.of(teamWithoutMembers));

        // Act
        List<TeamDTO> result = teamService.getAll();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getPersonIds().isEmpty());
    }

    @Test
    void getById_WhenTeamHasNoMembers_ShouldHandleGracefully() {
        // Arrange: Team sin TeamPersons
        Team teamWithoutMembers = Team.builder()
                .teamId(11)
                .nameTeam("Empty Team")
                .phone("111")
                .status(true)
                .teamPersons(null)
                .build();

        when(teamRepository.findById(11)).thenReturn(Optional.of(teamWithoutMembers));

        // Act
        TeamDTO result = teamService.getById(11);

        // Assert
        assertNotNull(result);
        assertEquals("Empty Team", result.getNameTeam());
        assertTrue(result.getPersonIds().isEmpty());
    }

    @Test
    void create_WhenStatusIsNull_ShouldDefaultToTrue() {
        // Arrange
        TeamDTO newTeamDTO = TeamDTO.builder()
                .nameTeam("NullStatus Team")
                .phone("555")
                .status(null)
                .personIds(List.of(testPerson.getPersonId()))
                .build();

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation -> {
                    Team team = invocation.getArgument(0);
                    team.setTeamId(99);
                    return team;
                });
        when(personRepository.findById(anyInt())).thenReturn(Optional.of(testPerson));

        // Act
        TeamDTO result = teamService.create(newTeamDTO);

        // Assert
        assertNotNull(result);
        verify(teamRepository).save(argThat(team -> Boolean.TRUE.equals(team.getStatus())));
        verify(teamPersonRepository).saveAll(anyList());
    }

    @Test
    void create_WhenPersonIdsEmpty_ShouldStillSaveTeamAndNotFail() {
        // Arrange
        TeamDTO dto = TeamDTO.builder()
                .nameTeam("Empty Members")
                .phone("0000")
                .status(true)
                .personIds(Collections.emptyList())
                .build();

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TeamDTO result = teamService.create(dto);

        // Assert
        assertNotNull(result);
        verify(teamRepository, times(1)).save(any(Team.class));
        // ✅ El servicio sí invoca saveAll, pero con lista vacía
        verify(teamPersonRepository, times(1)).saveAll(eq(Collections.emptyList()));
    }

    @Test
    void update_WhenPersonIdsEmpty_ShouldStillUpdateTeam() {
        // Arrange
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
        testTeamDTO.setPersonIds(Collections.emptyList());

        // Act
        boolean result = teamService.update(1, testTeamDTO);

        // Assert
        assertTrue(result);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamPersonRepository).deleteAllByTeam_TeamId(1);
        // ✅ También se llama con lista vacía
        verify(teamPersonRepository, times(1)).saveAll(eq(Collections.emptyList()));
    }

    @Test
    void delete_WhenRepositoriesDoNotThrow_ShouldStillReturnTrue() {
        // Arrange
        when(teamRepository.existsById(1)).thenReturn(true);

        // Act
        boolean result = teamService.delete(1);

        // Assert
        assertTrue(result);
        verify(teamPersonRepository).deleteAllByTeam_TeamId(1);
        verify(teamRepository).deleteById(1);
    }

}