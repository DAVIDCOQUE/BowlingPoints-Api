package com.bowlingpoints.controller;

import com.bowlingpoints.dto.UserDashboardStatsDTO;
import com.bowlingpoints.service.StatsService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {UserStatsController.class}) // ✅ Contexto mínimo
class UserStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @MockBean
    private UserTournamentService userTournamentService;

    private UserDashboardStatsDTO userDashboardStatsDTO;

    @BeforeEach
    void setUp() {
        userDashboardStatsDTO = UserDashboardStatsDTO.builder()
                .avgScoreGeneral(200.0)
                .bestLine(280)
                .totalTournaments(5)
                .totalLines(25)
                .build();
    }

    @Test
    void shouldReturnDashboardStatsSuccessfully() throws Exception {
        // Arrange
        Integer userId = 1;
        when(statsService.getUserDashboardStats(userId)).thenReturn(userDashboardStatsDTO);

        // Act & Assert
        mockMvc.perform(get("/api/user-stats/dashboard")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Estadísticas para dashboard cargadas")));
    }
}
