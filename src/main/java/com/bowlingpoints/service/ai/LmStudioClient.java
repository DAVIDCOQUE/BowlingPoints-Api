package com.bowlingpoints.service.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class LmStudioClient implements AiClient {

    private static final String LM_STUDIO_URL =
            "http://localhost:1234/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public String ask(String prompt) {

        Map<String, Object> body = Map.of(
                "model", "mistral-7b-instruct-v0.2",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content",
                                """
                                Eres un analista deportivo especializado en bolos.
                                Responde de forma clara, profesional y concisa.

                                """ + prompt
                        )
                ),
                "temperature", 0.4,
                "max_tokens", 900
        );

        Map<String, Object> response =
                restTemplate.postForObject(LM_STUDIO_URL, body, Map.class);

        if (response == null || !response.containsKey("choices")) {
            return "No se pudo obtener respuesta de la IA.";
        }

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.get("choices");

        Map<String, Object> message =
                (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString();
    }
}
