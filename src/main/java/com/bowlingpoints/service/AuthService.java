package com.bowlingpoints.service;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.AuthResponse;
import com.bowlingpoints.dto.LoginRequest;
import com.bowlingpoints.dto.RegisterRequest;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByNickname(request.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String token = jwtService.getToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public AuthResponse register(RegisterRequest request){
        Person persona = Person.builder()
                .firstName(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .gender(request.getGender())
                .phone(request.getPhone())
                .status(Boolean.TRUE)
                .createdBy(-1)
                .build();

        personRepository.save(persona);

        User user = User.builder()
                .nickname(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .person(persona)
                .status(true)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }
}
