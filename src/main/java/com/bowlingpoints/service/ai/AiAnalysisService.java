package com.bowlingpoints.service.ai;

import com.bowlingpoints.dto.PlayerAiStats;
import com.bowlingpoints.dto.ResultDTO;
import com.bowlingpoints.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final ResultService resultService;
    private final PromptBuilder promptBuilder;
    private final LmStudioClient lmStudioClient;

    /**
     * Análisis GLOBAL: todos los torneos.
     * Filtros opcionales: branchId, categoryId, modalityId
     */
    public String analyzeGlobal(Integer branchId, Integer categoryId, Integer modalityId) {

        // 1) Traer todos los resultados
        List<ResultDTO> results = Optional.ofNullable(resultService.getAll())
                .orElseGet(Collections::emptyList);

        // 2) Filtrar (si vienen filtros)
        List<ResultDTO> filtered = results.stream()
                .filter(r -> r.getScore() != null)
                .filter(r -> branchId == null || Objects.equals(r.getBranchId(), branchId))
                .filter(r -> categoryId == null || Objects.equals(r.getCategoryId(), categoryId))
                .filter(r -> modalityId == null || Objects.equals(r.getModalityId(), modalityId))
                .toList();

        if (filtered.isEmpty()) {
            return "No hay suficientes datos para realizar un análisis con esos filtros.";
        }

        // 3) Agrupar por jugador + modalidad
        Map<String, List<ResultDTO>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(
                        r -> safe(r.getPersonName()) + "|" + safe(r.getModalityName())
                ));

        // 4) Convertir a stats IA
        List<PlayerAiStats> stats = grouped.values().stream()
                .map(list -> {
                    int[] scores = list.stream()
                            .map(ResultDTO::getScore)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .toArray();

                    double avg = Arrays.stream(scores).average().orElse(0);
                    int max = Arrays.stream(scores).max().orElse(0);
                    int min = Arrays.stream(scores).min().orElse(0);

                    return new PlayerAiStats(
                            safe(list.get(0).getPersonName()),
                            safe(list.get(0).getModalityName()),
                            avg,
                            scores.length,
                            max,
                            min
                    );
                })
                // ✅ aquí el cambio importante
                .sorted(Comparator.comparingDouble(PlayerAiStats::getAverageScore).reversed())
                .toList();

        // 5) Prompt
        String prompt = promptBuilder.buildGlobal(stats, branchId, categoryId, modalityId);

        // 6) Llamar IA
        return lmStudioClient.ask(prompt);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "Desconocido" : s.trim();
    }
}
