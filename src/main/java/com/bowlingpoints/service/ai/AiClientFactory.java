package com.bowlingpoints.service.ai;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiClientFactory {

    private final AiClient genericAiClient;
    private final AiClient lmStudioClient;

    @Value("${ai.mode}")
    private String mode;

    public AiClientFactory(
            @Qualifier("genericAiClient") AiClient genericAiClient,
            @Qualifier("lmStudioClient") AiClient lmStudioClient
    ) {
        this.genericAiClient = genericAiClient;
        this.lmStudioClient = lmStudioClient;
    }

    public AiClient getClient() {
        return switch (mode) {
            case "online" -> genericAiClient;
            case "local" -> lmStudioClient;
            default -> throw new IllegalStateException("Modo IA no soportado: " + mode);
        };
    }
}
