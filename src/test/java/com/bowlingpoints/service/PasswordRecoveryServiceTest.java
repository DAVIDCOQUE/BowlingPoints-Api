package com.bowlingpoints.service;

import com.bowlingpoints.entity.PasswordResetToken;
import com.bowlingpoints.entity.Person;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.repository.PasswordResetTokenRepository;
import com.bowlingpoints.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    @Captor
    private ArgumentCaptor<String> htmlCaptor;

    private User testUser;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = Person.builder()
                .personId(1)
                .email("test@example.com")
                .fullName("John")
                .fullSurname("Doe")
                .build();

        testUser = User.builder()
                .userId(1)
                .nickname("testuser")
                .password("oldPassword")
                .person(testPerson)
                .build();
    }

    // Tests for requestPasswordReset()

    @Test
    void requestPasswordReset_WithValidNickname_CreatesTokenAndSendsEmail() throws MessagingException {
        // Arrange
        when(userRepository.findByNickname("testuser")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        passwordRecoveryService.requestPasswordReset("testuser");

        // Assert
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertNotNull(savedToken.getToken());
        assertEquals(testUser, savedToken.getUser());
        assertFalse(savedToken.isUsed());
        assertNotNull(savedToken.getExpirationDate());
        assertTrue(savedToken.getExpirationDate().isAfter(LocalDateTime.now().plusMinutes(29)));
        assertTrue(savedToken.getExpirationDate().isBefore(LocalDateTime.now().plusMinutes(31)));

        verify(emailService).sendHtmlMessage(
                eq("test@example.com"),
                eq("Recupera tu contraseña"),
                contains(savedToken.getToken())
        );
    }

    @Test
    void requestPasswordReset_WithValidEmail_CreatesTokenAndSendsEmail() throws MessagingException {
        // Arrange
        when(userRepository.findByNickname("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPersonEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        passwordRecoveryService.requestPasswordReset("test@example.com");

        // Assert
        verify(userRepository).findByNickname("test@example.com");
        verify(userRepository).findByPersonEmail("test@example.com");

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertNotNull(savedToken.getToken());
        assertEquals(testUser, savedToken.getUser());

        verify(emailService).sendHtmlMessage(
                eq("test@example.com"),
                anyString(),
                anyString()
        );
    }

    @Test
    void requestPasswordReset_WithNonExistentUser_DoesNotThrowException() throws MessagingException {
        // Arrange
        when(userRepository.findByNickname("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByPersonEmail("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> passwordRecoveryService.requestPasswordReset("nonexistent"));

        // Verify no token is created and no email is sent
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendHtmlMessage(anyString(), anyString(), anyString());
    }

    @Test
    void requestPasswordReset_EmailServiceThrows_PropagatesException() throws MessagingException {
        // Arrange
        when(userRepository.findByNickname("testuser")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new MessagingException("Email service error"))
                .when(emailService).sendHtmlMessage(anyString(), anyString(), anyString());

        // Act & Assert
        assertThrows(MessagingException.class,
                () -> passwordRecoveryService.requestPasswordReset("testuser"));

        // Verify token was saved before exception
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    // Tests for resetPassword()

    @Test
    void resetPassword_WithValidToken_UpdatesPasswordAndMarksTokenUsed() {
        // Arrange
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(1L)
                .token("valid-token")
                .user(testUser)
                .expirationDate(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // Act
        boolean result = passwordRecoveryService.resetPassword("valid-token", "newPassword123");

        // Assert
        assertTrue(result);
        assertEquals("encodedNewPassword", testUser.getPassword());
        assertTrue(resetToken.isUsed());

        verify(passwordEncoder).encode("newPassword123");
        verify(tokenRepository).save(resetToken);
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_WithNonExistentToken_ReturnsFalse() {
        // Arrange
        when(tokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        // Act
        boolean result = passwordRecoveryService.resetPassword("non-existent-token", "newPassword");

        // Assert
        assertFalse(result);

        // Verify no saves occurred
        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_WithUsedToken_ReturnsFalse() {
        // Arrange
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .id(1L)
                .token("used-token")
                .user(testUser)
                .expirationDate(LocalDateTime.now().plusMinutes(10))
                .used(true) // Already used
                .build();

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        // Act
        boolean result = passwordRecoveryService.resetPassword("used-token", "newPassword");

        // Assert
        assertFalse(result);

        // Verify no updates occurred
        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void resetPassword_WithExpiredToken_ReturnsFalse() {
        // Arrange
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .id(1L)
                .token("expired-token")
                .user(testUser)
                .expirationDate(LocalDateTime.now().minusMinutes(10)) // Expired 10 minutes ago
                .used(false)
                .build();

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        // Act
        boolean result = passwordRecoveryService.resetPassword("expired-token", "newPassword");

        // Assert
        assertFalse(result);

        // Verify no updates occurred
        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
