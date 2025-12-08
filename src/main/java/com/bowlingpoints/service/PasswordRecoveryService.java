package com.bowlingpoints.service;

import com.bowlingpoints.entity.PasswordResetToken;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.PasswordResetTokenRepository;
import com.bowlingpoints.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void requestPasswordReset(String identifier) throws MessagingException {
        Optional<User> userOpt = userRepository.findByNickname(identifier);

        if (userOpt.isEmpty()) {
            // Intentar buscar por email (en la tabla Person)
            userOpt = userRepository.findByPersonEmail(identifier);
        }

        // Para seguridad, mostrar mensaje genérico
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expirationDate(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Construir link
        // String resetLink = "https://bowlingpoints.com/restablecer-contraseña?token=" + token; usar en el url de produccion
        String resetLink = "http://localhost:4200/restablecer-contraseña?token=" + token;

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #2c3e50;">🔐 Recuperación de contraseña</h2>
                    <p style="font-size: 16px; color: #333;">
                        Hola,<br><br>
                        Hemos recibido una solicitud para restablecer tu contraseña.<br>
                        Para continuar, haz clic en el siguiente botón o enlace:
                    </p>
                
                    <p style="text-align: center; margin: 20px 0;">
                        <a href="%s" style="display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; border-radius: 5px; text-decoration: none;">
                            Restablecer contraseña
                        </a>
                    </p>
                
                    <p style="font-size: 14px; color: #555;">
                        O copia y pega este enlace en tu navegador:<br>
                        <a href="%s">%s</a>
                    </p>
                
                    <p style="font-size: 13px; color: #888;">
                        Este enlace es válido por <strong>30 minutos</strong> y solo puede usarse una vez.
                    </p>
                
                    <hr style="margin-top: 30px;">
                    <p style="font-size: 12px; color: #aaa;">
                        Si no solicitaste este cambio, puedes ignorar este mensaje.
                    </p>
                </div>
                """.formatted(resetLink, resetLink, resetLink);

        emailService.sendHtmlMessage(user.getPerson().getEmail(), "Recupera tu contraseña", html);
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isUsed() || resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);
        userRepository.save(user);

        return true;
    }
}
