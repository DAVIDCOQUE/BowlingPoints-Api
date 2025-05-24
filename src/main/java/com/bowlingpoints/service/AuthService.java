package com.bowlingpoints.service;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.AuthResponse;
import com.bowlingpoints.dto.LoginRequest;
import com.bowlingpoints.dto.RegisterRequest;
import com.bowlingpoints.entity.Persona;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.PersonaRepository;
import com.bowlingpoints.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));
        User user=userRepository.findByNickname(request.getUserName()).orElseThrow();
        String token=jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();

    }




    public AuthResponse register(RegisterRequest request){

        Persona persona = Persona.builder()
                .firstName(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .gender(request.getGender())
                .phone(request.getPhone())
                .status(Boolean.TRUE)
                .created_by(-1)
                .build();

        personaRepository.save(persona);

        User user = User.builder()
                .nickname(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .persona(persona)
                .status("Activo")
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }
}
