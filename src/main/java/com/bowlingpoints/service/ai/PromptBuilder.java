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
                Eres un analista deportivo profesional especializado en bowling.
                
                Objetivo:
                Analizar el rendimiento GLOBAL de jugadores considerando
                todos los torneos registrados en el sistema.
                
                Reglas estrictas:
                - Idioma: Español
                - Estilo: Deportivo profesional
                - No inventar datos
                - Usar únicamente la información entregada
                - Máx. 6 líneas por punto
                - Recomendaciones prácticas y realistas
                
                Formato de respuesta:
                - Usa listas cortas
                - Evita párrafos largos
                
                Contexto del análisis:
                """);

        if (branchId != null) {
            sb.append("- Filtro aplicado: Rama\n");
        }
        if (categoryId != null) {
            sb.append("- Filtro aplicado: Categoría\n");
        }
        if (modalityId != null) {
            sb.append("- Filtro aplicado: Modalidad\n");
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
