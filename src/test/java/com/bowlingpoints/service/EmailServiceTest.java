package com.bowlingpoints.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_TO_EMAIL = "test@example.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_HTML_BODY = "<h1>Test Content</h1>";
    private static final String SENDER_EMAIL = "maq.htas.gr1pm@gmail.com";

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendHtmlMessage_WhenValidParameters_ShouldSendEmail() throws MessagingException {
        // Act
        emailService.sendHtmlMessage(TEST_TO_EMAIL, TEST_SUBJECT, TEST_HTML_BODY);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlMessage_WhenMailSenderThrowsException_ShouldPropagateException() {
        // Arrange
        doThrow(new RuntimeException("Failed to send email"))
            .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            emailService.sendHtmlMessage(TEST_TO_EMAIL, TEST_SUBJECT, TEST_HTML_BODY)
        );
    }

    @Test
    void sendHtmlMessage_WhenNullEmail_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            emailService.sendHtmlMessage(null, TEST_SUBJECT, TEST_HTML_BODY)
        );
    }

    @Test
    void sendHtmlMessage_WhenEmptySubject_ShouldSendEmailSuccessfully() throws MessagingException {
        // Act
        emailService.sendHtmlMessage(TEST_TO_EMAIL, "", TEST_HTML_BODY);

        // Assert
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlMessage_WhenEmptyHtmlBody_ShouldSendEmailSuccessfully() throws MessagingException {
        // Act
        emailService.sendHtmlMessage(TEST_TO_EMAIL, TEST_SUBJECT, "");

        // Assert
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlMessage_WithComplexHtml_ShouldSendEmailSuccessfully() throws MessagingException {
        // Arrange
        String complexHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Email</title>
            </head>
            <body>
                <h1>Welcome to BowlingPoints</h1>
                <p>This is a test email with complex HTML content.</p>
                <ul>
                    <li>Item 1</li>
                    <li>Item 2</li>
                </ul>
            </body>
            </html>
            """;

        // Act
        emailService.sendHtmlMessage(TEST_TO_EMAIL, TEST_SUBJECT, complexHtml);

        // Assert
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlMessage_WhenInvalidEmail_ShouldThrowException() {
        // Arrange
        String email = "test@example.com";

        doThrow(new MailSendException("Failed to send email"))
                .when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () ->
                emailService.sendHtmlMessage(email, TEST_SUBJECT, TEST_HTML_BODY)
        );
    }

    @Test
    void sendHtmlMessage_ShouldSetCorrectSenderEmail() throws MessagingException {
        // Arrange
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        emailService.sendHtmlMessage(TEST_TO_EMAIL, TEST_SUBJECT, TEST_HTML_BODY);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();
        assertEquals(mimeMessage, sentMessage);
    }
}