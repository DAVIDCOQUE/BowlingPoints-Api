package com.bowlingpoints.service;

import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para RoleService.
 */
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role roleAdmin;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roleAdmin = Role.builder()
                .id(1)
                .name("ADMIN")
                .build();

        roleUser = Role.builder()
                .id(2)
                .name("USER")
                .build();
    }

    // ----------------------------------------------------------------------
    // getAllRoles
    // ----------------------------------------------------------------------
    @Test
    void getAllRoles_ShouldReturnMappedRoleDTOs_WhenRolesExist() {
        when(roleRepository.findAll()).thenReturn(List.of(roleAdmin, roleUser));

        List<RoleDTO> result = roleService.getAllRoles();

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void getAllRoles_ShouldReturnEmptyList_WhenNoRolesExist() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<RoleDTO> result = roleService.getAllRoles();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleRepository, times(1)).findAll();
    }
}
