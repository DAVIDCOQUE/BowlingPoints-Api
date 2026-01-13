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
                FORMATO DE RESPUESTA OBLIGATORIO (NO IGNORAR):
                
                - Máximo 12 líneas en TOTAL
                - Cada punto debe tener máximo 2 líneas
                - Usa frases cortas y concretas
                - No repitas datos numéricos innecesarios
                - Prioriza conclusiones, no descripciones largas
                
                Estructura EXACTA de la respuesta:
                
                1. Rendimiento Global (2 líneas)
                2. Jugadores Destacados (máx. 3 nombres)
                3. Alto Potencial Competitivo (máx. 3 nombres)
                4. Riesgos de Regularidad (máx. 3 nombres)
                5. Recomendación Final (1–2 líneas)
                
                Si no puedes resumir, prioriza CLARIDAD sobre detalle.
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
