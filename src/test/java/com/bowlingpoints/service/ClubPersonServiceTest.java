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
    }

    @Test
    void addMemberToClub_WhenClubAndPersonExists_ShouldReturnTrue() {
        // Arrange
        ClubPersonDTO dto = ClubPersonDTO.builder()
                .clubId(testClub.getClubId())
                .personId(testPerson.getPersonId())
                .roleInClub("MEMBER")
                .build();
        when(clubRepository.findById(testClub.getClubId())).thenReturn(Optional.of(testClub));
        when(personRepository.findById(testPerson.getPersonId())).thenReturn(Optional.of(testPerson));
        when(clubPersonRepository.save(any())).thenReturn(testClubPerson);

        // Act
        boolean result = clubPersonService.addMemberToClub(dto);

        // Assert
        assertTrue(result);
        verify(clubPersonRepository).save(any(ClubPerson.class));
    }

    @Test
    void addMemberToClub_WhenClubNotExists_ShouldReturnFalse() {
        ClubPersonDTO dto = ClubPersonDTO.builder()
                .clubId(999)
                .personId(testPerson.getPersonId())
                .roleInClub("MEMBER")
                .build();
        when(clubRepository.findById(999)).thenReturn(Optional.empty());
        // Act
        boolean result = clubPersonService.addMemberToClub(dto);
        // Assert
        assertFalse(result);
        verify(clubPersonRepository, never()).save(any());
    }

    @Test
    void addMemberToClub_WhenPersonNotExists_ShouldReturnFalse() {
        ClubPersonDTO dto = ClubPersonDTO.builder()
                .clubId(testClub.getClubId())
                .personId(999)
                .roleInClub("MEMBER")
                .build();
        when(clubRepository.findById(testClub.getClubId())).thenReturn(Optional.of(testClub));
        when(personRepository.findById(999)).thenReturn(Optional.empty());
        // Act
        boolean result = clubPersonService.addMemberToClub(dto);
        // Assert
        assertFalse(result);
        verify(clubPersonRepository, never()).save(any());
    }

    @Test
    void updateMemberRole_WhenClubPersonExists_ShouldReturnTrue() {
        // Arrange
        when(clubPersonRepository.findById(anyInt())).thenReturn(Optional.of(testClubPerson));
        when(clubPersonRepository.save(any())).thenReturn(testClubPerson);

        // Act
        boolean result = clubPersonService.updateMemberRole(1, "CAPTAIN");

        // Assert
        assertTrue(result);
        verify(clubPersonRepository).save(any(ClubPerson.class));
    }

    @Test
    void updateMemberRole_WhenClubPersonExists_ShouldReturnFalse() {
        when(clubPersonRepository.findById(anyInt())).thenReturn(Optional.empty());
        boolean result = clubPersonService.updateMemberRole(1, "CAPTAIN");
        assertFalse(result);
        verify(clubPersonRepository, never()).save(any());
    }

    @Test
    void removeMember_WhenClubNotExists_ShouldReturnTrue() {
        // Arrange
        when(clubPersonRepository.findById(anyInt())).thenReturn(Optional.of(testClubPerson));
        when(clubPersonRepository.save(any())).thenReturn(testClubPerson);

        // Act
        boolean result = clubPersonService.removeMember(1);

        // Assert
        assertTrue(result);
        verify(clubPersonRepository).save(any(ClubPerson.class));
    }

    @Test
    void removeMember_WhenClubPersonNotExists_ShouldReturnFalse() {
        when(clubPersonRepository.findById(anyInt())).thenReturn(Optional.empty());
        boolean result = clubPersonService.removeMember(1);
        assertFalse(result);
        verify(clubPersonRepository, never()).save(any());
    }
}