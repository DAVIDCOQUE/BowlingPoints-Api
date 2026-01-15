package com.bowlingpoints.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericAiClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GenericAiClient genericAiClient;

    @BeforeEach
    void setUp() {
        genericAiClient = new GenericAiClient(webClientBuilder);
        ReflectionTestUtils.setField(genericAiClient, "baseUrl", "https://api.test.com");
        ReflectionTestUtils.setField(genericAiClient, "apiKeyHeader", "Authorization");
        ReflectionTestUtils.setField(genericAiClient, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(genericAiClient, "model", "test-model");
        ReflectionTestUtils.setField(genericAiClient, "responsePath", "content.0.text");
    }

    @SuppressWarnings("unchecked")
    private void setupWebClientMocks(Map<String, Object> response) {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        if (response == null) {
            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.empty());
        } else {
            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(response));
        }
    }

    @Test
    void ask_WhenNullResponse_ShouldReturnDefaultMessage() {
        setupWebClientMocks(null);

        String result = genericAiClient.ask("Test prompt");

        assertEquals("No se recibió respuesta del proveedor de IA.", result);
    }

    @Test
    void ask_WhenValidResponse_ShouldReturnContent() {
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("text", "AI response text");

        Map<String, Object> response = new HashMap<>();
        response.put("content", List.of(textContent));

        setupWebClientMocks(response);

        String result = genericAiClient.ask("Test prompt");

        assertEquals("AI response text", result);
    }

    @Test
    void ask_WhenResponseMissingPath_ShouldReturnErrorMessage() {
        Map<String, Object> response = new HashMap<>();
        response.put("other", "data");

        setupWebClientMocks(response);

        String result = genericAiClient.ask("Test prompt");

        assertEquals("Respuesta de IA sin contenido válido.", result);
    }

    @Test
    void ask_WhenResponseHasUnexpectedFormat_ShouldReturnErrorMessage() {
        Map<String, Object> response = new HashMap<>();
        response.put("content", "not a list");

        setupWebClientMocks(response);

        String result = genericAiClient.ask("Test prompt");

        assertEquals("Respuesta de IA con formato inesperado.", result);
    }
}
