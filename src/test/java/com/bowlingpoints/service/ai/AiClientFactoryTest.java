package com.bowlingpoints.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AiClientFactoryTest {

    @Mock
    private AiClient genericAiClient;

    @Mock
    private AiClient lmStudioClient;

    private AiClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AiClientFactory(genericAiClient, lmStudioClient);
    }

    @Test
    void getClient_WhenModeIsOnline_ShouldReturnGenericClient() {
        ReflectionTestUtils.setField(factory, "mode", "online");

        AiClient result = factory.getClient();

        assertSame(genericAiClient, result);
    }

    @Test
    void getClient_WhenModeIsLocal_ShouldReturnLmStudioClient() {
        ReflectionTestUtils.setField(factory, "mode", "local");

        AiClient result = factory.getClient();

        assertSame(lmStudioClient, result);
    }

    @Test
    void getClient_WhenModeIsUnsupported_ShouldThrowException() {
        ReflectionTestUtils.setField(factory, "mode", "invalid");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> factory.getClient()
        );

        assertTrue(exception.getMessage().contains("Modo IA no soportado"));
    }
}
