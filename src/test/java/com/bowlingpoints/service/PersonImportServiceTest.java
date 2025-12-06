package com.bowlingpoints.service;

import com.bowlingpoints.dto.PersonImportResponse;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.RoleRepository;
import com.bowlingpoints.repository.UserRepository;
import com.bowlingpoints.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonImportServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private PersonImportService personImportService;

    @Test
    void importPersonFile_SuccessfulImport() throws Exception {
        String csv = "document;names;surnames;email;gender;birthDate;phone\n" +
                "123;John;Doe;john@example.com;M;1/5/1990;555\n" +
                "456;Jane;Roe;jane@example.com;F;2/6/1992;666\n";

        MockMultipartFile file = new MockMultipartFile("file", "persons.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        Role playerRole = new Role();
        playerRole.setName("JUGADOR");

        when(roleRepository.findByName("JUGADOR")).thenReturn(Optional.of(playerRole));

        when(personRepository.existsByDocument("123")).thenReturn(false);
        when(personRepository.existsByDocument("456")).thenReturn(false);
        when(personRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(personRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);

        PersonImportResponse resp = personImportService.importPersonFile(file);

        assertNotNull(resp);
        assertEquals(2, resp.getSuccessCount());
        assertEquals(0, resp.getErrorCount());
        assertTrue(resp.getErrors().isEmpty());

        verify(personRepository, times(1)).saveAll(anyList());
        verify(userRepository, times(1)).saveAll(anyList());
        verify(userRoleRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importPersonFile_ShouldCountDuplicateDocumentAsError() throws Exception {
        String csv = "document;names;surnames;email;gender;birthDate;phone\n" +
                "123;John;Doe;john@example.com;M;1/5/1990;555\n" +
                "456;Jane;Roe;jane@example.com;F;2/6/1992;666\n";

        MockMultipartFile file = new MockMultipartFile("file", "persons.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        Role playerRole = new Role();
        playerRole.setName("JUGADOR");
        when(roleRepository.findByName("JUGADOR")).thenReturn(Optional.of(playerRole));

        // First document does not exist, second does -> should produce one error
        when(personRepository.existsByDocument("123")).thenReturn(false);
        when(personRepository.existsByDocument("456")).thenReturn(true);
        when(personRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);

        PersonImportResponse resp = personImportService.importPersonFile(file);

        assertNotNull(resp);
        assertEquals(1, resp.getSuccessCount());
        assertEquals(1, resp.getErrorCount());
        assertEquals(1, resp.getErrors().size());

        // Should still try to save the successful ones
        verify(personRepository, times(1)).saveAll(anyList());
        verify(userRepository, times(1)).saveAll(anyList());
        verify(userRoleRepository, times(1)).saveAll(anyList());
    }
}
