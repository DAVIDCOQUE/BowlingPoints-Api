package com.bowlingpoints.service;

import com.bowlingpoints.dto.ClubDTO;
import com.bowlingpoints.dto.ClubPersonDTO;
import com.bowlingpoints.entity.ClubPerson;
import com.bowlingpoints.entity.Clubs;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.repository.ClubPersonRepository;
import com.bowlingpoints.repository.ClubRepository;
import com.bowlingpoints.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para ClubService.
 */
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubPersonRepository clubPersonRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ClubService clubService;

    private Clubs club;
    private Person person;
    private ClubPerson clubPerson;
    private ClubDTO clubDTO;
    private ClubPersonDTO memberDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        person = Person.builder()
                .personId(1)
                .fullName("John")
                .fullSurname("Doe")
                .document("123")
                .email("john@doe.com")
                .phone("3001234567")
                .gender("M")
                .photoUrl("photo.jpg")
                .build();

        clubPerson = ClubPerson.builder()
                .id(1)
                .person(person)
                .roleInClub("Presidente")
                .status(true)
                .joinedAt(LocalDateTime.now())
                .build();

        club = Clubs.builder()
                .clubId(1)
                .name("Strike Club")
                .city("Cali")
                .description("Club de bowling")
                .imageUrl("image.jpg")
                .foundationDate(LocalDate.of(2020, 1, 1))
                .status(true)
                .members(List.of(clubPerson))
                .build();

        memberDTO = ClubPersonDTO.builder()
                .personId(1)
                .roleInClub("Presidente")
                .build();

        clubDTO = ClubDTO.builder()
                .name("Strike Club")
                .city("Cali")
                .description("Club de bowling")
                .foundationDate(LocalDate.of(2020, 1, 1))
                .members(List.of(memberDTO))
                .build();
    }

    // ----------------------------------------------------------------------
    // createClub
    // ----------------------------------------------------------------------
    @Test
    void createClub_ShouldSaveClubAndMembers_WhenMembersExist() {
        when(personRepository.findById(1)).thenReturn(Optional.of(person));

        clubService.createClub(clubDTO);

        verify(clubRepository, times(1)).save(any(Clubs.class));
        verify(clubPersonRepository, times(1)).save(any(ClubPerson.class));
    }

    @Test
    void createClub_ShouldSaveOnlyClub_WhenNoMembersProvided() {
        ClubDTO input = ClubDTO.builder()
                .name("Solo Club")
                .city("Bogotá")
                .build();

        clubService.createClub(input);

        verify(clubRepository, times(1)).save(any(Clubs.class));
        verify(clubPersonRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------
    // getAllClubsNotDeleted
    // ----------------------------------------------------------------------
    @Test
    void getAllClubsNotDeleted_ShouldReturnOnlyNonDeletedClubs() {
        Clubs deleted = Clubs.builder()
                .clubId(2)
                .name("Eliminado")
                .deletedAt(LocalDateTime.now())
                .build();

        when(clubRepository.findAll()).thenReturn(List.of(club, deleted));

        List<ClubDTO> result = clubService.getAllClubsNotDeleted();

        assertEquals(1, result.size());
        assertEquals("Strike Club", result.get(0).getName());
    }

    // ----------------------------------------------------------------------
    // getAllActiveClubs
    // ----------------------------------------------------------------------
    @Test
    void getAllActiveClubs_ShouldReturnOnlyActiveClubs() {
        Clubs inactive = Clubs.builder()
                .clubId(2)
                .name("Inactivo")
                .status(false)
                .build();

        when(clubRepository.findAll()).thenReturn(List.of(club, inactive));

        List<ClubDTO> result = clubService.getAllActiveClubs();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getStatus());
    }

    // ----------------------------------------------------------------------
    // getClubById
    // ----------------------------------------------------------------------
    @Test
    void getClubById_ShouldReturnClub_WhenExistsAndNotDeleted() {
        when(clubRepository.findById(1)).thenReturn(Optional.of(club));

        ClubDTO result = clubService.getClubById(1);

        assertNotNull(result);
        assertEquals("Strike Club", result.getName());
        assertEquals(1, result.getMembers().size());
    }

    @Test
    void getClubById_ShouldReturnNull_WhenDeleted() {
        club.setDeletedAt(LocalDateTime.now());
        when(clubRepository.findById(1)).thenReturn(Optional.of(club));

        ClubDTO result = clubService.getClubById(1);

        assertNull(result);
    }

    @Test
    void getClubById_ShouldReturnNull_WhenNotFound() {
        when(clubRepository.findById(999)).thenReturn(Optional.empty());

        ClubDTO result = clubService.getClubById(999);

        assertNull(result);
    }

    // ----------------------------------------------------------------------
    // updateClub
    // ----------------------------------------------------------------------
    @Test
    void updateClub_ShouldUpdateAndReplaceMembers_WhenExists() {
        when(clubRepository.findById(1)).thenReturn(Optional.of(club));
        when(personRepository.findById(1)).thenReturn(Optional.of(person));

        boolean result = clubService.updateClub(1, clubDTO);

        assertTrue(result);
        verify(clubRepository, times(1)).save(any(Clubs.class));
        verify(clubPersonRepository, times(1)).deleteAllByClub_ClubId(1);
        verify(clubPersonRepository, times(1)).save(any(ClubPerson.class));
    }

    @Test
    void updateClub_ShouldReturnFalse_WhenNotFound() {
        when(clubRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = clubService.updateClub(99, clubDTO);

        assertFalse(result);
        verify(clubRepository, never()).save(any());
    }

    // ----------------------------------------------------------------------
    // deleteClub
    // ----------------------------------------------------------------------
    @Test
    void deleteClub_ShouldSoftDeleteClubAndMembers_WhenExists() {
        ClubPerson cp1 = ClubPerson.builder().id(1).club(club).status(true).build();
        when(clubRepository.findById(1)).thenReturn(Optional.of(club));
        when(clubPersonRepository.findAllByClub_ClubIdAndDeletedAtIsNull(1)).thenReturn(List.of(cp1));

        boolean result = clubService.deleteClub(1);

        assertTrue(result);
        verify(clubRepository, times(1)).save(any(Clubs.class));
        verify(clubPersonRepository, times(1)).save(any(ClubPerson.class));
    }

    @Test
    void deleteClub_ShouldReturnFalse_WhenNotFound() {
        when(clubRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = clubService.deleteClub(999);

        assertFalse(result);
        verify(clubRepository, never()).save(any());
    }
}
