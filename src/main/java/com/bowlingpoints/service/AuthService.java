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

        // 1. Manejo de Usuario No Encontrado
        User user;
        try {
            user = userRepository.findByNickname(request.getUserName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getUserName()));
        } catch (UsernameNotFoundException e) {
            // **Punto de Control 1: Loguear antes de relanzar**
            log.warn("Login fallido para el usuario '{}': {}", request.getUserName(), e.getMessage());
            throw e; // Relanzar para que el Controller lo maneje
        }

        // 2. Manejo de Contraseña Inválida
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // **Punto de Control 2: Loguear antes de lanzar**
            String message = "Credenciales inválidas para el usuario: " + request.getUserName();
            log.warn(message);
            throw new BadCredentialsException("Invalid password"); // No se debe incluir el nombre de usuario en el mensaje de excepción final por seguridad
        }

        // 3. Generación y Respuesta Exitosa
        String token = jwtService.getToken(user);
        log.info("Inicio de sesión exitoso para el usuario: {}", request.getUserName());

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
