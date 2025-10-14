package com.bowlingpoints.service;

import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.entity.Role;
import com.bowlingpoints.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .id(1)
                .description("ROLE_ADMIN")
                .build();

        userRole = Role.builder()
                .id(2)
                .description("ROLE_USER")
                .build();
    }

    @Test
    void getAllRoles_WhenRolesExist_ShouldReturnAllRoles() {
        // Arrange
        when(roleRepository.findAll())
                .thenReturn(Arrays.asList(adminRole, userRole));

        // Act
        List<RoleDTO> result = roleService.getAllRoles();

        // Assert
        assertEquals(2, result.size());
        
        RoleDTO firstRole = result.get(0);
        assertEquals(adminRole.getId(), firstRole.getRoleId());
        assertEquals(adminRole.getDescription(), firstRole.getDescription());
        
        RoleDTO secondRole = result.get(1);
        assertEquals(userRole.getId(), secondRole.getRoleId());
        assertEquals(userRole.getDescription(), secondRole.getDescription());
    }

    @Test
    void getAllRoles_WhenNoRolesExist_ShouldReturnEmptyList() {
        // Arrange
        when(roleRepository.findAll())
                .thenReturn(Collections.emptyList());

        // Act
        List<RoleDTO> result = roleService.getAllRoles();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRoles_ShouldMapAllFieldsCorrectly() {
        // Arrange
        Role complexRole = Role.builder()
                .id(3)
                .description("ROLE_SUPER_ADMIN")
                .build();

        when(roleRepository.findAll())
                .thenReturn(Collections.singletonList(complexRole));

        // Act
        List<RoleDTO> result = roleService.getAllRoles();

        // Assert
        assertEquals(1, result.size());
        RoleDTO roleDTO = result.get(0);
        
        assertEquals(complexRole.getId(), roleDTO.getRoleId());
        assertEquals(complexRole.getDescription(), roleDTO.getDescription());
    }
}