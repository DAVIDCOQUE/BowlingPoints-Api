package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubPersonDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.ClubPersonRepository;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubPersonServiceTest {

    @Mock
    private ClubPersonRepository clubPersonRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ClubPersonService clubPersonService;

    private Clubs testClub;
    private Person testPerson;
    private ClubPerson testClubPerson;
    private ClubMemberRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        testClub = Clubs.builder()
                .clubId(1)
                .name("Test Club")
                .build();

        testPerson = Person.builder()
                .personId(1)
                .fullName("John")
                .fullSurname("Doe")
                .email("john.doe@test.com")
                .photoUrl("test-photo-url")
                .document("123456")
                .status(true)
                .build();

        testClubPerson = ClubPerson.builder()
                .club(testClub)
                .person(testPerson)
                .roleInClub("MEMBER")
                .status(true)
                .joinedAt(LocalDateTime.now())
                .createdBy(1)
                .build();

        testRequest = new ClubMemberRequestDTO();
        testRequest.setClubId(1);
        testRequest.setPersonId(1);
        testRequest.setRoleInClub("MEMBER");
    }

    @Test
    void getMembersByClubId_WhenClubExists_AndHasMembers_ShouldReturnMembers() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(testClub));
        when(clubPersonRepository.findByClubAndStatusIsTrue(testClub))
                .thenReturn(Arrays.asList(testClubPerson));

        // Act
        ResponseGenericDTO<List<ClubPersonDTO>> response = clubPersonService.getMembersByClubId(1);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Miembros cargados correctamente", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        
        ClubPersonDTO dto = response.getData().get(0);
        assertEquals(testPerson.getFullName() + " " + testPerson.getFullSurname(), dto.getFullName().trim());
        assertEquals(testPerson.getEmail(), dto.getEmail());
        assertEquals(testClubPerson.getRoleInClub(), dto.getRoleInClub());
    }

    @Test
    void getMembersByClubId_WhenClubExists_ButHasNoMembers_ShouldReturnEmptyList() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(testClub));
        when(clubPersonRepository.findByClubAndStatusIsTrue(testClub))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseGenericDTO<List<ClubPersonDTO>> response = clubPersonService.getMembersByClubId(1);

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("El club no tiene miembros activos", response.getMessage());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void getMembersByClubId_WhenClubDoesNotExist_ShouldThrowException() {
        // Arrange
        when(clubRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            clubPersonService.getMembersByClubId(999)
        );
    }

    @Test
    void addMemberToClub_WhenValidRequest_ShouldAddMember() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(testClub));
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        when(clubPersonRepository.findByClubAndPerson(testClub, testPerson))
                .thenReturn(Optional.empty());
        when(clubPersonRepository.save(any(ClubPerson.class))).thenReturn(testClubPerson);

        // Act
        ClubPerson result = clubPersonService.addMemberToClub(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testClub, result.getClub());
        assertEquals(testPerson, result.getPerson());
        assertEquals("MEMBER", result.getRoleInClub());
        assertTrue(result.getStatus());
        assertNotNull(result.getJoinedAt());
        assertEquals(1, result.getCreatedBy());
        
        verify(clubRepository).findById(1);
        verify(personRepository).findById(1);
        verify(clubPersonRepository).findByClubAndPerson(testClub, testPerson);
        verify(clubPersonRepository).save(argThat(member -> 
            member.getClub().equals(testClub) &&
            member.getPerson().equals(testPerson) &&
            member.getRoleInClub().equals("MEMBER") &&
            member.getStatus() &&
            member.getJoinedAt() != null &&
            member.getCreatedBy() == 1
        ));
    }

    @Test
    void addMemberToClub_WhenClubNotFound_ShouldThrowException() {
        // Arrange
        when(clubRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ClubMemberRequestDTO request = new ClubMemberRequestDTO();
            request.setClubId(999);
            request.setPersonId(1);
            request.setRoleInClub("MEMBER");
            clubPersonService.addMemberToClub(request);
        });
        assertEquals("Club no encontrado con ID: 999" , exception.getMessage());
        verify(clubPersonRepository, never()).save(any());
    }

    @Test
    void addMemberToClub_WhenPersonNotFound_ShouldThrowException() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(testClub));
        when(personRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ClubMemberRequestDTO request = new ClubMemberRequestDTO();
            request.setClubId(1);
            request.setPersonId(999);
            request.setRoleInClub("MEMBER");
            clubPersonService.addMemberToClub(request);
        });
        assertEquals("Persona no encontrada", exception.getMessage());
        verify(clubPersonRepository, never()).save(any());
    }

    @Test
    void addMemberToClub_WhenMembershipAlreadyExists_ShouldThrowException() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(testClub));
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        when(clubPersonRepository.findByClubAndPerson(testClub, testPerson))
                .thenReturn(Optional.of(testClubPerson));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            clubPersonService.addMemberToClub(testRequest)
        );
        assertEquals("El miembro ya está asignado a este club.", exception.getMessage());
        verify(clubPersonRepository, never()).save(any());
    }
}