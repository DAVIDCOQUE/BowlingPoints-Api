package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubDetailsDTO;
import com.bowlingpoints.dto.ClubMemberDTO;
import com.bowlingpoints.dto.ClubMemberRequestDTO;
import com.bowlingpoints.dto.ClubsDTO;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.ClubPersonRepository;
import com.bowlingpoints.repository.ClubsRepository;
import com.bowlingpoints.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubsServiceTest {

    @Mock
    private ClubsRepository clubsRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ClubPersonRepository clubPersonRepository;

    @InjectMocks
    private ClubsService clubsService;

    private Clubs testClub;
    private Person testPerson;
    private ClubPerson testClubPerson;
    private ClubsDTO testClubsDTO;
    private ClubMemberRequestDTO testMemberRequest;

    @BeforeEach
    void setUp() {
        testPerson = Person.builder()
                .personId(1)
                .fullName("John")
                .fullSurname("Doe")
                .email("john.doe@test.com")
                .photoUrl("test-photo-url")
                .document("123456")
                .status(true)
                .build();

        testClub = Clubs.builder()
                .clubId(1)
                .name("Test Club")
                .city("Test City")
                .description("Test Description")
                .foundationDate(LocalDate.now())
                .imageUrl("/uploads/clubs/test.png")
                .status(true)
                .build();

        testClubPerson = ClubPerson.builder()
                .club(testClub)
                .person(testPerson)
                .roleInClub("ADMIN")
                .status(true)
                .joinedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        testMemberRequest = new ClubMemberRequestDTO();
        testMemberRequest.setPersonId(1);
        testMemberRequest.setRoleInClub("ADMIN");

        testClubsDTO = new ClubsDTO();
        testClubsDTO.setName("Test Club");
        testClubsDTO.setCity("Test City");
        testClubsDTO.setDescription("Test Description");
        testClubsDTO.setFoundationDate(LocalDate.now());
        testClubsDTO.setImageUrl("/uploads/clubs/test.png");
        testClubsDTO.setMembers(Collections.singletonList(testMemberRequest));
    }

    @Test
    void createClubWithMembers_WhenValidData_ShouldCreateClubAndMembers() {
        // Arrange
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        when(clubsRepository.save(any(Clubs.class))).thenReturn(testClub);

        // Act
        Clubs result = clubsService.createClubWithMembers(testClubsDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testClubsDTO.getName(), result.getName());
        assertEquals(testClubsDTO.getCity(), result.getCity());
        assertEquals(testClubsDTO.getDescription(), result.getDescription());
        assertEquals(testClubsDTO.getFoundationDate(), result.getFoundationDate());
        assertEquals(testClubsDTO.getImageUrl(), result.getImageUrl());
        assertTrue(result.getStatus());

        verify(personRepository).findById(1);
        verify(clubsRepository).save(any(Clubs.class));
    }

    @Test
    void createClubWithMembers_WhenNoImageUrl_ShouldUseDefaultImage() {
        // Arrange
        testClubsDTO.setImageUrl(null);
        when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));
        when(clubsRepository.save(any(Clubs.class))).thenAnswer(invocation -> {
            Clubs savedClub = invocation.getArgument(0);
            savedClub.setClubId(1);
            return savedClub;
        });

        // Act
        Clubs result = clubsService.createClubWithMembers(testClubsDTO);

        // Assert
        assertEquals("/uploads/clubs/default.png", result.getImageUrl());
    }

    @Test
    void createClubWithMembers_WhenPersonNotFound_ShouldThrowException() {
        // Arrange
        when(personRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> clubsService.createClubWithMembers(testClubsDTO)
        );
        assertEquals("Persona no encontrada: 1", exception.getMessage());
        verify(clubsRepository, never()).save(any());
    }

    @Test
    void getClubDetails_WhenClubExists_ShouldReturnDetails() {
        // Arrange
        testClub.setMembers(Collections.singletonList(testClubPerson));
        when(clubsRepository.findById(1)).thenReturn(Optional.of(testClub));

        // Act
        ClubDetailsDTO result = clubsService.getClubDetails(1);

        // Assert
        assertNotNull(result);
        assertEquals(testClub.getClubId(), result.getClubId());
        assertEquals(testClub.getName(), result.getName());
        assertEquals(testClub.getCity(), result.getCity());
        assertEquals(testClub.getDescription(), result.getDescription());
        assertEquals(testClub.getFoundationDate(), result.getFoundationDate());
        assertEquals(testClub.getImageUrl(), result.getImageUrl());
        assertEquals(testClub.getStatus(), result.getStatus());
        
        assertEquals(1, result.getMembers().size());
        ClubMemberDTO memberDTO = result.getMembers().get(0);
        assertEquals(testPerson.getFullName() + " " + testPerson.getFullSurname(), memberDTO.getFullName().trim());
        assertEquals(testPerson.getEmail(), memberDTO.getEmail());
        assertEquals("ADMIN", memberDTO.getRoleInClub());
    }

    @Test
    void getClubDetails_WhenClubNotFound_ShouldThrowException() {
        // Arrange
        when(clubsRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> clubsService.getClubDetails(999)
        );
    }

    @Test
    void getAllClubsWithMembers_ShouldReturnOnlyActiveClubs() {
        // Arrange
        Clubs inactiveClub = Clubs.builder()
                .clubId(2)
                .name("Inactive Club")
                .status(false)
                .build();

        testClub.setMembers(Collections.singletonList(testClubPerson));
        when(clubsRepository.findAll()).thenReturn(Arrays.asList(testClub, inactiveClub));

        // Act
        List<ClubDetailsDTO> results = clubsService.getAllClubsWithMembers();

        // Assert
        assertEquals(1, results.size());
        ClubDetailsDTO activeClub = results.get(0);
        assertEquals(testClub.getClubId(), activeClub.getClubId());
        assertEquals(testClub.getName(), activeClub.getName());
        assertTrue(activeClub.getStatus());
        assertEquals(1, activeClub.getMembers().size());
    }

    @Test
    void updateClubWithMembers_WhenValidUpdate_ShouldUpdateClubAndMembers() {
        // Arrange
        testClub.setMembers(Collections.singletonList(testClubPerson));
        when(clubsRepository.findById(1)).thenReturn(Optional.of(testClub));

        ClubsDTO updateDTO = new ClubsDTO();
        updateDTO.setName("Updated Club");
        updateDTO.setCity("Updated City");
        updateDTO.setDescription("Updated Description");
        updateDTO.setFoundationDate(LocalDate.now());
        updateDTO.setImageUrl("/uploads/clubs/updated.png");
        updateDTO.setStatus(true);
        
        ClubMemberRequestDTO updatedMember = new ClubMemberRequestDTO();
        updatedMember.setPersonId(1);
        updatedMember.setRoleInClub("MEMBER");
        updateDTO.setMembers(Collections.singletonList(updatedMember));

        // Act
        clubsService.updateClubWithMembers(1, updateDTO);

        // Assert
        verify(clubsRepository).findById(1);
        verify(clubsRepository).save(argThat(club -> 
            club.getName().equals("Updated Club") &&
            club.getCity().equals("Updated City") &&
            club.getDescription().equals("Updated Description") &&
            club.getImageUrl().equals("/uploads/clubs/updated.png") &&
            club.getStatus() &&
            club.getUpdatedAt() != null
        ));
        
        verify(clubPersonRepository).save(argThat(member ->
            member.getRoleInClub().equals("MEMBER")
        ));
    }

    @Test
    void updateClubWithMembers_WhenAddingNewMember_ShouldCreateNewClubPerson() {
        // Arrange
        testClub.setMembers(Collections.emptyList());
        when(clubsRepository.findById(1)).thenReturn(Optional.of(testClub));

        Person newPerson = Person.builder()
                .personId(2)
                .fullName("Jane")
                .fullSurname("Doe")
                .document("123456789")
                .email("jane.doe@example.com")
                .status(true)
                .build();
        when(personRepository.findById(2)).thenReturn(Optional.of(newPerson));

        ClubsDTO updateDTO = new ClubsDTO();
        updateDTO.setName(testClub.getName());
        updateDTO.setCity(testClub.getCity());
        updateDTO.setDescription(testClub.getDescription());
        updateDTO.setFoundationDate(testClub.getFoundationDate());
        updateDTO.setImageUrl(testClub.getImageUrl());
        updateDTO.setStatus(true);

        ClubMemberRequestDTO newMember = new ClubMemberRequestDTO();
        newMember.setPersonId(2);
        newMember.setRoleInClub("MEMBER");
        updateDTO.setMembers(Collections.singletonList(newMember));

        // Act
        clubsService.updateClubWithMembers(1, updateDTO);

        // Assert
        verify(personRepository).findById(2);
        
        ArgumentCaptor<ClubPerson> clubPersonCaptor = ArgumentCaptor.forClass(ClubPerson.class);
        verify(clubPersonRepository).save(clubPersonCaptor.capture());
        
        ClubPerson savedClubPerson = clubPersonCaptor.getValue();
        assertEquals(newPerson, savedClubPerson.getPerson());
        assertEquals("MEMBER", savedClubPerson.getRoleInClub());
        assertEquals(testClub, savedClubPerson.getClub());
        assertTrue(savedClubPerson.getStatus());
        assertNotNull(savedClubPerson.getJoinedAt());
        assertNotNull(savedClubPerson.getCreatedAt());
    }

    @Test
    void updateClubWithMembers_WhenClubNotFound_ShouldThrowException() {
        // Arrange
        when(clubsRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> clubsService.updateClubWithMembers(999, testClubsDTO)
        );
        assertEquals("❌ Club no encontrado con ID 999", exception.getMessage());
        verify(clubsRepository, never()).save(any());
    }

    @Test
    void deleteClub_WhenClubExists_ShouldSoftDelete() {
        // Arrange
        when(clubsRepository.findById(1)).thenReturn(Optional.of(testClub));
        when(clubsRepository.save(any(Clubs.class))).thenReturn(testClub);

        // Act
        boolean result = clubsService.deleteClub(1);

        // Assert
        assertTrue(result);
        verify(clubsRepository).save(argThat(club -> 
            !club.getStatus() &&
            club.getDeletedAt() != null
        ));
    }

    @Test
    void deleteClub_WhenClubNotFound_ShouldReturnFalse() {
        // Arrange
        when(clubsRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        boolean result = clubsService.deleteClub(999);

        // Assert
        assertFalse(result);
        verify(clubsRepository, never()).save(any());
    }
}