package com.bowlingpoints.controller;

import com.bowlingpoints.dto.RoleDTO;
import com.bowlingpoints.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {RoleController.class}) // ✅ Carga solo este controlador
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @Test
    void getAllRoles_ShouldReturnListOfRoles() throws Exception {
        RoleDTO role1 = new RoleDTO(1, "Admin");
        RoleDTO role2 = new RoleDTO(2, "User");

        when(roleService.getAllRoles()).thenReturn(List.of(role1, role2));

        mockMvc.perform(get("/roles").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles list retrieved"))
                .andExpect(jsonPath("$.data[0].name").value("Admin"))
                .andExpect(jsonPath("$.data[1].name").value("User"));

        verify(roleService, times(1)).getAllRoles();
    }
}
