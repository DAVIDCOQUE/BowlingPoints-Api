package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtAuthenticationFilter;
import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva seguridad para test
public class RoleControllerTest {
/*
    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        roleDTO = new RoleDTO();
        roleDTO.setRoleId(1);
        roleDTO.setDescription("Admin");
    }

    @Test
    void getAllRoles_ShouldReturnListOfRoles() throws Exception {
        RoleDTO roleDTO = RoleDTO.builder()
                .roleId(1)
                .description("Admin")
                .build();

        when(roleService.getAllRoles()).thenReturn(List.of(roleDTO));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles list retrieved"))
                .andExpect(jsonPath("$.data[0].roleId").value(1))
                .andExpect(jsonPath("$.data[0].description").value("Admin"));
    }

 */
}
