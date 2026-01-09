package com.bowlingpoints.service.ai;

import com.bowlingpoints.dto.PlayerAiStats;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    public String buildGlobal(
            List<PlayerAiStats> stats,
            Integer branchId,
            Integer categoryId,
            Integer modalityId
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
        Eres un analista deportivo especializado en bowling.

        Tu tarea es analizar el rendimiento GLOBAL de jugadores
        considerando TODOS los torneos registrados en el sistema.

        Reglas:
        - Responde ÚNICAMENTE en ESPAÑOL
        - Análisis general (máx. 6 líneas)
        - Sé claro y directo
        - Usa lenguaje deportivo profesional
        - No inventes datos
        - Basa tus conclusiones únicamente en los datos entregados
        - Da recomendaciones prácticas y realistas

        Contexto del análisis:
        """);

        if (branchId != null) {
            sb.append("- Rama filtrada\n");
        }
        if (categoryId != null) {
            sb.append("- Categoría filtrada\n");
        }
        if (modalityId != null) {
            sb.append("- Modalidad filtrada\n");
        }

        sb.append("""
        
        Datos de jugadores:
        """);

        for (PlayerAiStats s : stats) {
            sb.append(String.format("""
            Jugador: %s
            Modalidad: %s
            Promedio general: %.2f
            Partidas jugadas: %d
            Mejor línea: %d
            Peor línea: %d

            """,
                    s.getPlayerName(),
                    s.getModalityName(),
                    s.getAverageScore(),
                    s.getGamesPlayed(),
                    s.getMaxScore(),
                    s.getMinScore()
            ));
        }

        sb.append("""
        Responde con los siguientes puntos:

        1. Análisis general del rendimiento global
        2. Jugadores más consistentes
        3. Jugadores con alto potencial competitivo
        4. Jugadores que necesitan mayor regularidad
        5. Recomendaciones para selección de torneo departamental o regional

        Mantén el análisis breve, claro y útil.
        """);

        return sb.toString();
    }
}
