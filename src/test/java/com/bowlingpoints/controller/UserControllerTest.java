package com.bowlingpoints.controller;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.dto.UserFullDTO;
import com.bowlingpoints.service.UserFullService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Pruebas unitarias para UsersController
 */
@WebMvcTest(controllers = UsersController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.bowlingpoints\\.config\\..*")
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserFullService userFullService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserFullDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = UserFullDTO.builder()
                .userId(1)
                .nickname("johnsoto")
                .email("john@example.com")
                .status(true)
                .build();
    }

    // ===============================
    // GET /users
    // ===============================
   /* @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        Mockito.when(userFullService.getAllUsersWithDetails()).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuarios obtenidos correctamente"))
                .andExpect(jsonPath("$.data[0].username").value("johnsoto"));
    }

    */

    // ===============================
    // GET /users/me
    // ===============================
    /*@Test
    void getCurrentUser_ShouldReturnUser_WhenTokenValid() throws Exception {
        Mockito.when(jwtService.getUsernameFromToken("validtoken")).thenReturn("johnsoto");
        Mockito.when(userFullService.getByUsername("johnsoto")).thenReturn(userDTO);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer validtoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("johnsoto"));
    }

     */

    @Test
    void getCurrentUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        Mockito.when(jwtService.getUsernameFromToken("invalid")).thenReturn("unknown");
        Mockito.when(userFullService.getByUsername("unknown")).thenReturn(null);

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    // ===============================
    // GET /users/jugadores
    // ===============================
    /*@Test
    void getActivePlayers_ShouldReturnListOfPlayers() throws Exception {
        Mockito.when(userFullService.getAllActivePlayers()).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/users/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jugadores activos encontrados"))
                .andExpect(jsonPath("$.data[0].username").value("johnsoto"));
    }
     */

    // ===============================
    // GET /users/{id}
    // ===============================
    /*@Test
    void getUserById_ShouldReturnUser_WhenExists() throws Exception {
        Mockito.when(userFullService.getUserById(1)).thenReturn(userDTO);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("johnsoto"));
    }

     */

    @Test
    void getUserById_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(userFullService.getUserById(99)).thenReturn(null);

        mockMvc.perform(get("/users/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    // ===============================
    // POST /users
    // ===============================
    @Test
    void createUser_ShouldReturnOk_WhenSuccess() throws Exception {
        Mockito.doNothing().when(userFullService).createUser(any(UserFullDTO.class));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario creado correctamente"));
    }

    @Test
    void createUser_ShouldReturnServerError_WhenExceptionOccurs() throws Exception {
        Mockito.doThrow(new RuntimeException("DB error")).when(userFullService).createUser(any(UserFullDTO.class));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al crear el usuario"));
    }

    // ===============================
    // PUT /users/{id}
    // ===============================
    @Test
    void updateUser_ShouldReturnOk_WhenUpdated() throws Exception {
        Mockito.when(userFullService.updateUser(eq(1), any(UserFullDTO.class))).thenReturn(true);

        mockMvc.perform(put("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario actualizado correctamente"));
    }

    @Test
    void updateUser_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(userFullService.updateUser(eq(99), any(UserFullDTO.class))).thenReturn(false);

        mockMvc.perform(put("/users/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    // ===============================
    // DELETE /users/{id}
    // ===============================
    @Test
    void deleteUser_ShouldReturnOk_WhenDeleted() throws Exception {
        Mockito.when(userFullService.deleteUser(1)).thenReturn(true);

        mockMvc.perform(delete("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario eliminado correctamente"));
    }

    @Test
    void deleteUser_ShouldReturn404_WhenNotFound() throws Exception {
        Mockito.when(userFullService.deleteUser(99)).thenReturn(false);

        mockMvc.perform(delete("/users/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }
}
