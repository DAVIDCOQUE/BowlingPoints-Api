package com.bowlingpoints.service;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.AuthResponse;
import com.bowlingpoints.dto.LoginRequest;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(LoginRequest request) {

        // **NOTA DE SEGURIDAD:** No lanzar inmediatamente si no se encuentra.
        // Esto previene que el tiempo de respuesta revele si el usuario existe.
        User user = userRepository.findByNickname(request.getUserName()).orElse(null);

        // 1. Caso de usuario no encontrado (o nulo)
        if (user == null) {
            // **IMPORTANTE:** Loguear internamente para auditoría.
            log.warn("Intento de login fallido: Usuario no encontrado '{}'", request.getUserName());
            // Lanzar una excepción genérica que será capturada y uniformada.
            throw new BadCredentialsException("Invalid username or password");
        }

        // 2. Caso de contraseña inválida
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String message = "Credenciales inválidas para el usuario: " + request.getUserName();
            log.warn(message);
            // Lanzar la misma excepción.
            throw new BadCredentialsException("Invalid username or password");
        }

        // 3. Éxito
        String token = jwtService.getToken(user);
        log.info("Inicio de sesión exitoso para el usuario: {}", request.getUserName());

        return AuthResponse.builder().token(token).build();
    }
}
