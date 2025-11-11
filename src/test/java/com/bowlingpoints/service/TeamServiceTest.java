package com.bowlingpoints.service;

import com.bowlingpoints.dto.TeamDTO;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Team;
import com.bowlingpoints.entity.TeamPerson;
import com.bowlingpoints.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private TeamPersonRepository teamPersonRepository;

    @Mock
    private TournamentTeamRepository tournamentTeamRepository;

    @Mock
    private TournamentRegistrationRepository tournamentRegistrationRepository;

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
        TeamDTO newTeamDTO = TeamDTO.builder()
                .nameTeam("New Team")
                .phone("0987654321")
                .playerIds(List.of(1, 2))
                .build();

        Team savedTeam = Team.builder()
                .teamId(1)
                .nameTeam(newTeamDTO.getNameTeam())
                .phone(newTeamDTO.getPhone())
                .status(true)
                .build();

        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);
        when(personRepository.findById(anyInt())).thenReturn(Optional.of(testPerson));

        TeamDTO result = teamService.create(newTeamDTO);

        assertNotNull(result);
        assertEquals("New Team", result.getNameTeam());
        verify(teamRepository).save(any(Team.class));
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
                .build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> teamService.create(newTeamDTO));
    }

    @Test
    void update_WhenTeamExists_ShouldReturnTrue() {
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
        when(personRepository.findById(anyInt())).thenReturn(Optional.of(testPerson));

        boolean result = teamService.update(1, testTeamDTO);

        assertTrue(result);
        verify(teamRepository).save(any(Team.class));
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
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));
        when(personRepository.findById(99)).thenReturn(Optional.empty());

        // 👇 Agregar el ID inexistente para que el servicio intente buscarlo
        testTeamDTO.setPlayerIds(List.of(99));

        assertThrows(RuntimeException.class, () -> teamService.update(1, testTeamDTO));
    }


    @Test
    void delete_WhenTeamExists_ShouldReturnTrue() {
        when(teamRepository.existsById(1)).thenReturn(true);

        boolean result = teamService.delete(1);

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
    }

    @Test
    void create_WhenStatusIsNull_ShouldDefaultToTrue() {
        TeamDTO newTeamDTO = TeamDTO.builder()
                .nameTeam("NullStatus Team")
                .phone("555")
                .status(null)
                .playerIds(List.of(1, 2))
                .build();

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team team = inv.getArgument(0);
            team.setTeamId(99);
            return team;
        });
        when(personRepository.findById(anyInt())).thenReturn(Optional.of(testPerson));

        TeamDTO result = teamService.create(newTeamDTO);

        assertNotNull(result);
        assertTrue(result.getStatus());
        verify(teamRepository).save(any(Team.class));
        verify(teamPersonRepository).saveAll(anyList());
    }

    @Test
    void create_WhenPersonIdsEmpty_ShouldStillSaveTeamAndNotFail() {
        // Arrange
        TeamDTO dto = TeamDTO.builder()
                .nameTeam("Empty Members")
                .phone("0000")
                .status(true)
                .playerIds(List.of(1, 2)) // ✅ debe tener al menos 2
                .build();

        Person p1 = new Person();
        p1.setPersonId(1);
        Person p2 = new Person();
        p2.setPersonId(2);

        when(personRepository.findById(1)).thenReturn(Optional.of(p1));
        when(personRepository.findById(2)).thenReturn(Optional.of(p2));
        when(teamRepository.save(any(Team.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        TeamDTO result = teamService.create(dto);

        // Assert
        assertNotNull(result);
        verify(teamRepository).save(any(Team.class));
        verify(teamPersonRepository).saveAll(anyList());
    }


    @Test
    void update_WhenPersonIdsEmpty_ShouldStillUpdateTeam() {
        when(teamRepository.findById(1)).thenReturn(Optional.of(testTeam));

        // PlayerIds vacía
        testTeamDTO.setPlayerIds(Collections.emptyList());

        boolean result = teamService.update(1, testTeamDTO);

        assertTrue(result);
        verify(teamRepository).save(any(Team.class));
        verify(teamPersonRepository).deleteAllByTeam_TeamId(1);
        verify(teamPersonRepository).saveAll(eq(Collections.emptyList()));
    }

    @Test
    void delete_WhenRepositoriesDoNotThrow_ShouldStillReturnTrue() {
        when(teamRepository.existsById(1)).thenReturn(true);
        when(tournamentTeamRepository.findAll()).thenReturn(Collections.emptyList());
        when(tournamentRegistrationRepository.findAll()).thenReturn(Collections.emptyList());

        boolean result = teamService.delete(1);

        assertTrue(result);
        verify(teamPersonRepository).deleteAllByTeam_TeamId(1);
        verify(teamRepository).deleteById(1);
    }

}