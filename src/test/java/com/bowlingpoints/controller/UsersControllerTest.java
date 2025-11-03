package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.service.UserFullService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {UsersController.class})
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserFullService userFullService;

    @MockBean
    private JwtService jwtService;

    private UserFullDTO mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserFullDTO();
        mockUser.setUserId(1);
        mockUser.setFullName("John Doe");
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        when(userFullService.getAllUsersWithDetails()).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data[0].fullName", is("John Doe")));
    }

    @Test
    void shouldReturnActiveUsers() throws Exception {
        when(userFullService.getAllActiveUsers()).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/users/actives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void shouldReturnCurrentUserFromToken() throws Exception {
        when(jwtService.getUsernameFromToken(anyString())).thenReturn("john.doe");
        when(userFullService.getByUsername("john.doe")).thenReturn(mockUser);

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("John Doe")));
    }

    @Test
    void shouldReturnNotFoundWhenCurrentUserNotExists() throws Exception {
        when(jwtService.getUsernameFromToken(anyString())).thenReturn("unknown");
        when(userFullService.getByUsername("unknown")).thenReturn(null);

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer token123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldReturnActivePlayers() throws Exception {
        when(userFullService.getAllActivePlayers()).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/users/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fullName", is("John Doe")));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        when(userFullService.getUserById(1)).thenReturn(mockUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("John Doe")));
    }

    @Test
    void shouldReturnNotFoundWhenUserByIdDoesNotExist() throws Exception {
        when(userFullService.getUserById(99)).thenReturn(null);

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        doNothing().when(userFullService).createUser(any(UserFullDTO.class));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"John Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("creado")));
    }

    @Test
    void shouldHandleExceptionOnCreateUser() throws Exception {
        doThrow(new RuntimeException("DB Error")).when(userFullService).createUser(any());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"John Doe\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        when(userFullService.updateUser(eq(1), any())).thenReturn(true);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("actualizado")));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateFails() throws Exception {
        when(userFullService.updateUser(eq(1), any())).thenReturn(false);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated Name\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        when(userFullService.deleteUser(1)).thenReturn(true);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("eliminado")));
    }

    @Test
    void shouldReturnNotFoundWhenDeleteFails() throws Exception {
        when(userFullService.deleteUser(1)).thenReturn(false);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
