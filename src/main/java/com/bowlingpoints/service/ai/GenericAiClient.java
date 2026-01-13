package com.bowlingpoints.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GenericAiClient implements AiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.api-key-header}")
    private String apiKeyHeader;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.response-path}")
    private String responsePath;

    @Override
    @SuppressWarnings("unchecked")
    public String ask(String prompt) {

        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(apiKeyHeader, apiKey)
                //  HEADER OBLIGATORIO PARA CLAUDE
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        //  FORMATO CORRECTO PARA ANTHROPIC (Claude)
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 800,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", prompt
                                        )
                                )
                        )
                )
        );

        Map<String, Object> response = client.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            return "No se recibió respuesta del proveedor de IA.";
        }

        // lectura genérica del path (content.0.text)
        Object value = response;

        for (String key : responsePath.split("\\.")) {

            if (value instanceof Map<?, ?> map) {
                value = map.get(key);

            } else if (value instanceof List<?> list) {
                int index = Integer.parseInt(key);
                value = list.get(index);

            } else {
                return "Respuesta de IA con formato inesperado.";
            }

            if (value == null) {
                return "Respuesta de IA sin contenido válido.";
            }
        }

        return value.toString();
    }
}
