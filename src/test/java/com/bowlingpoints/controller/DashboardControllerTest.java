package com.bowlingpoints.controller;

import com.bowlingpoints.dto.DashboardDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.DashboardService;
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

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {DashboardController.class})  // ✅ Fuerza un contexto mínimo
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private DashboardDTO dashboardDTO;

    @BeforeEach
    void setUp() {
        dashboardDTO = new DashboardDTO();
        // Puedes inicializar valores si tu DashboardDTO tiene campos
        // p. ej. dashboardDTO.setTotalPlayers(10);
    }

    @Test
    void shouldReturnDashboardSuccessfully() throws Exception {
        // Arrange
        when(dashboardService.getDashboardData()).thenReturn(dashboardDTO);

        // Act & Assert
        mockMvc.perform(get("/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Dashboard cargado correctamente")));
    }
}
