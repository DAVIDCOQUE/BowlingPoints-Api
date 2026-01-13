package com.bowlingpoints.controller;

import com.bowlingpoints.service.ai.AiAnalysisResponse;
import com.bowlingpoints.service.ai.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @GetMapping("/analizar-resultados-globales")
    public AiAnalysisResponse analyzeGlobal(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) Integer categoryId
    ) {
        return new AiAnalysisResponse(
                aiAnalysisService.analyzeGlobal(
                        branchId,
                        categoryId,
                        null // modalityId (no se usa en esta vista)
                )
        );
    }
}

