package com.bowlingpoints.service.ai;

import com.bowlingpoints.dto.PlayerAiStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
    }

    @Test
    void buildGlobal_WithNoFilters_ShouldBuildPrompt() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Juan Perez", "Individual", 180.5, 10, 220, 150)
        );

        String prompt = promptBuilder.buildGlobal(stats, null, null, null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Juan Perez"));
        assertTrue(prompt.contains("Individual"));
        assertTrue(prompt.contains("180"));
        assertFalse(prompt.contains("Filtro aplicado"));
    }

    @Test
    void buildGlobal_WithBranchFilter_ShouldIncludeFilterInfo() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Maria Lopez", "Dobles", 190.0, 5, 210, 170)
        );

        String prompt = promptBuilder.buildGlobal(stats, 1, null, null);

        assertTrue(prompt.contains("Filtro aplicado: Rama"));
        assertFalse(prompt.contains("Filtro aplicado: Categoría"));
    }

    @Test
    void buildGlobal_WithCategoryFilter_ShouldIncludeFilterInfo() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Carlos Ruiz", "Individual", 175.0, 8, 200, 160)
        );

        String prompt = promptBuilder.buildGlobal(stats, null, 2, null);

        assertTrue(prompt.contains("Filtro aplicado: Categoría"));
        assertFalse(prompt.contains("Filtro aplicado: Rama"));
    }

    @Test
    void buildGlobal_WithModalityFilter_ShouldIncludeFilterInfo() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Ana Garcia", "Trios", 185.0, 6, 215, 165)
        );

        String prompt = promptBuilder.buildGlobal(stats, null, null, 3);

        assertTrue(prompt.contains("Filtro aplicado: Modalidad"));
    }

    @Test
    void buildGlobal_WithAllFilters_ShouldIncludeAllFilterInfo() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Pedro Sanchez", "Individual", 195.0, 12, 230, 175)
        );

        String prompt = promptBuilder.buildGlobal(stats, 1, 2, 3);

        assertTrue(prompt.contains("Filtro aplicado: Rama"));
        assertTrue(prompt.contains("Filtro aplicado: Categoría"));
        assertTrue(prompt.contains("Filtro aplicado: Modalidad"));
    }

    @Test
    void buildGlobal_WithMultiplePlayers_ShouldIncludeAllPlayers() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Jugador 1", "Individual", 180.0, 10, 200, 160),
                new PlayerAiStats("Jugador 2", "Dobles", 190.0, 8, 220, 170),
                new PlayerAiStats("Jugador 3", "Trios", 175.0, 6, 195, 155)
        );

        String prompt = promptBuilder.buildGlobal(stats, null, null, null);

        assertTrue(prompt.contains("Jugador 1"));
        assertTrue(prompt.contains("Jugador 2"));
        assertTrue(prompt.contains("Jugador 3"));
    }

    @Test
    void buildGlobal_ShouldContainRequiredSections() {
        List<PlayerAiStats> stats = List.of(
                new PlayerAiStats("Test Player", "Individual", 180.0, 5, 200, 160)
        );

        String prompt = promptBuilder.buildGlobal(stats, null, null, null);

        assertTrue(prompt.contains("Rendimiento Global"));
        assertTrue(prompt.contains("Jugadores Destacados"));
        assertTrue(prompt.contains("Alto Potencial Competitivo"));
        assertTrue(prompt.contains("Recomendación Final"));
    }
}
