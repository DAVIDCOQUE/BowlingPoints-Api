package com.bowlingpoints.controller;

import com.bowlingpoints.dto.PasswordRecoveryRequestDTO;
import com.bowlingpoints.dto.PasswordResetRequestDTO;
import com.bowlingpoints.service.PasswordRecoveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PasswordRecoveryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.bowlingpoints\\.config\\.jwt\\..*"
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class PasswordRecoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordRecoveryService passwordRecoveryService;

    @Autowired
    private ObjectMapper objectMapper;

    // Tests for /auth/recover-password

    @Test
    void recoverPassword_WithValidIdentifier_ReturnsSuccess() throws Exception {
        // Arrange
        PasswordRecoveryRequestDTO request = new PasswordRecoveryRequestDTO();
        request.setIdentifier("testuser");

        // Act & Assert
        mockMvc.perform(post("/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Si el usuario existe, se ha enviado un correo de recuperación."));
    }

    @Test
    void recoverPassword_WhenEmailServiceFails_Returns500() throws Exception {
        // Arrange
        PasswordRecoveryRequestDTO request = new PasswordRecoveryRequestDTO();
        request.setIdentifier("testuser");

        doThrow(new MessagingException("Email service error"))
                .when(passwordRecoveryService).requestPasswordReset(anyString());

        // Act & Assert
        mockMvc.perform(post("/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error al enviar correo. Intenta nuevamente."));
    }

    // Tests for /auth/reset-password

    @Test
    void resetPassword_WithValidToken_ReturnsSuccess() throws Exception {
        // Arrange
        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setToken("valid-token");
        request.setNewPassword("newPassword123");

        when(passwordRecoveryService.resetPassword(eq("valid-token"), eq("newPassword123")))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Contraseña restablecida correctamente."));
    }

    @Test
    void resetPassword_WithInvalidToken_ReturnsBadRequest() throws Exception {
        // Arrange
        PasswordResetRequestDTO request = new PasswordResetRequestDTO();
        request.setToken("invalid-token");
        request.setNewPassword("newPassword123");

        when(passwordRecoveryService.resetPassword(eq("invalid-token"), eq("newPassword123")))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token inválido o expirado."));
    }
}
