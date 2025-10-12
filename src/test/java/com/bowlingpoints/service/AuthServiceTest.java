package com.bowlingpoints.service;

import com.bowlingpoints.config.jwt.JwtService;
import com.bowlingpoints.dto.AuthResponse;
import com.bowlingpoints.dto.LoginRequest;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.PersonRepository;
import com.bowlingpoints.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest testLoginRequest;
    private static final String TEST_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .nickname("testUser")
                .password("encodedPassword")
                .build();

        testLoginRequest = LoginRequest.builder()
                .userName("testUser")
                .password("password123")
                .build();
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByNickname(testLoginRequest.getUserName())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.getToken(testUser)).thenReturn(TEST_TOKEN);

        // Act
        AuthResponse response = authService.login(testLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getToken());
        verify(userRepository).findByNickname(testLoginRequest.getUserName());
        verify(passwordEncoder).matches(testLoginRequest.getPassword(), testUser.getPassword());
        verify(jwtService).getToken(testUser);
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByNickname(testLoginRequest.getUserName())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authService.login(testLoginRequest));
        verify(userRepository).findByNickname(testLoginRequest.getUserName());
        verify(jwtService, never()).getToken(any());
    }

    @Test
    void login_WhenPasswordIsInvalid_ShouldThrowBadCredentialsException() {
        // Arrange
        when(userRepository.findByNickname(testLoginRequest.getUserName())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testLoginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(testLoginRequest));
        verify(userRepository).findByNickname(testLoginRequest.getUserName());
        verify(passwordEncoder).matches(testLoginRequest.getPassword(), testUser.getPassword());
        verify(jwtService, never()).getToken(any());
    }
}