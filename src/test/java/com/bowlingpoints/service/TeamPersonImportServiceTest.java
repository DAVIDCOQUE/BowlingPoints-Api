package com.bowlingpoints.service;

import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.Team;
import com.bowlingpoints.entity.TeamPerson;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.TeamPersonRepository;
import com.bowlingpoints.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamPersonImportServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamPersonRepository teamPersonRepository;

    @InjectMocks
    private TeamPersonImportService teamPersonImportService;

    @Test
    void importCsv_CreatesAssociations_WhenDataValid() throws Exception {
        String csv = "documentNumber,teamName\n123,Team A\n456,Team B\n";
        MockMultipartFile file = new MockMultipartFile("file", "team-person.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        Person p1 = new Person(); p1.setPersonId(1);
        Person p2 = new Person(); p2.setPersonId(2);
        Team t1 = new Team(); t1.setTeamId(11);
        Team t2 = new Team(); t2.setTeamId(12);

        when(personRepository.findByDocument("123")).thenReturn(Optional.of(p1));
        when(personRepository.findByDocument("456")).thenReturn(Optional.of(p2));
        when(teamRepository.findByNameTeam("Team A")).thenReturn(Optional.of(t1));
        when(teamRepository.findByNameTeam("Team B")).thenReturn(Optional.of(t2));
        when(teamPersonRepository.existsByPerson_PersonIdAndTeam_TeamId(eq(1), eq(11))).thenReturn(false);
        when(teamPersonRepository.existsByPerson_PersonIdAndTeam_TeamId(eq(2), eq(12))).thenReturn(false);

        var result = teamPersonImportService.importCsv(file, 99, true);

        assertEquals(2, result.created());
        assertEquals(0, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(teamPersonRepository, times(2)).save(any(TeamPerson.class));
    }

    @Test
    void importCsv_Skips_WhenAssociationExists() throws Exception {
        String csv = "documentNumber,teamName\n123,Team A\n";
        MockMultipartFile file = new MockMultipartFile("file", "team-person.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        Person p = new Person(); p.setPersonId(1);
        Team t = new Team(); t.setTeamId(11);

        when(personRepository.findByDocument("123")).thenReturn(Optional.of(p));
        when(teamRepository.findByNameTeam("Team A")).thenReturn(Optional.of(t));
        when(teamPersonRepository.existsByPerson_PersonIdAndTeam_TeamId(eq(1), eq(11))).thenReturn(true);

        var result = teamPersonImportService.importCsv(file, 5, true);

        assertEquals(0, result.created());
        assertEquals(1, result.skipped());
        assertTrue(result.errors().isEmpty());

        verify(teamPersonRepository, never()).save(any(TeamPerson.class));
    }

    @Test
    void importCsv_ReportsErrors_WhenMissingPersonOrTeamOrInvalidLine() throws Exception {
        String csv = "documentNumber,teamName\n,Team A\n999,UnknownTeam\nonlyonecolumn\n";
        MockMultipartFile file = new MockMultipartFile("file", "team-person.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        // First line after header has empty document -> error
        // Second line: person not found -> error
        when(personRepository.findByDocument("999")).thenReturn(Optional.empty());

        var result = teamPersonImportService.importCsv(file, 1, true);

        assertEquals(0, result.created());
        assertEquals(0, result.skipped());
        assertFalse(result.errors().isEmpty());
        // should contain at least 3 errors for the three problematic lines
        assertTrue(result.errors().size() >= 3);
    }
}
