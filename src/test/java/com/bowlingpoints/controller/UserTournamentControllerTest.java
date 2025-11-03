package com.bowlingpoints.controller;

import com.bowlingpoints.dto.UserTournamentDTO;
import com.bowlingpoints.service.UserTournamentService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserTournamentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {UserTournamentController.class}) // ✅ evita ApplicationContext error
class UserTournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserTournamentService userTournamentService;

    private List<UserTournamentDTO> tournaments;

    @BeforeEach
    void setUp() {
        UserTournamentDTO dto = new UserTournamentDTO();
        // Inicializa campos si tu DTO tiene atributos, ej:
        // dto.setTournamentName("Summer Cup");
        tournaments = List.of(dto);
    }

    @Test
    void shouldReturnPlayedTournamentsSuccessfully() throws Exception {
        // Arrange
        Integer userId = 1;
        when(userTournamentService.getTournamentsPlayedByUser(userId)).thenReturn(tournaments);

        // Act & Assert
        mockMvc.perform(get("/user-tournaments/{userId}/played", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Torneos jugados obtenidos correctamente")));
    }
}
