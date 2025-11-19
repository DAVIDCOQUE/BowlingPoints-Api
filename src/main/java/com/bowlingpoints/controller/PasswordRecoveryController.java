package com.bowlingpoints.controller;

import com.bowlingpoints.dto.PasswordRecoveryRequestDTO;
import com.bowlingpoints.dto.PasswordResetRequestDTO;
import com.bowlingpoints.service.PasswordRecoveryService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody PasswordRecoveryRequestDTO request) {
        try {
            passwordRecoveryService.requestPasswordReset(request.getIdentifier());

            // 🔥 SIEMPRE devolver JSON; nunca texto plano
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Si el usuario existe, se ha enviado un correo de recuperación."
                    )
            );

        } catch (MessagingException e) {
            return ResponseEntity.status(500).body(
                    Map.of(
                            "success", false,
                            "message", "Error al enviar correo. Intenta nuevamente."
                    )
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequestDTO request) {
        boolean success = passwordRecoveryService.resetPassword(
                request.getToken(), request.getNewPassword()
        );

        if (success) {
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Contraseña restablecida correctamente."
                    )
            );
        } else {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Token inválido o expirado."
                    )
            );
        }
    }
}
