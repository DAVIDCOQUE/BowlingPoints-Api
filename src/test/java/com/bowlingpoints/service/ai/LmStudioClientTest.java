package com.bowlingpoints.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LmStudioClientTest {

    @Mock
    private RestTemplate restTemplate;

    private LmStudioClient lmStudioClient;

    @BeforeEach
    void setUp() {
        lmStudioClient = new LmStudioClient();
        ReflectionTestUtils.setField(lmStudioClient, "restTemplate", restTemplate);
    }

    @Test
    void ask_WhenValidResponse_ShouldReturnContent() {
        Map<String, Object> message = Map.of("content", "Test response from AI");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> response = Map.of("choices", List.of(choice));

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(response);

        String result = lmStudioClient.ask("Test prompt");

        assertEquals("Test response from AI", result);
    }

    @Test
    void ask_WhenNullResponse_ShouldReturnErrorMessage() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(null);

        String result = lmStudioClient.ask("Test prompt");

        assertEquals("No se pudo obtener respuesta de la IA.", result);
    }

    @Test
    void ask_WhenNoChoicesInResponse_ShouldReturnErrorMessage() {
        Map<String, Object> response = Map.of("other", "data");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(response);

        String result = lmStudioClient.ask("Test prompt");

        assertEquals("No se pudo obtener respuesta de la IA.", result);
    }
}
