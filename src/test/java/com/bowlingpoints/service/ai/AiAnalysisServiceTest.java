package com.bowlingpoints.service.ai;

import com.bowlingpoints.dto.ResultDTO;
import com.bowlingpoints.service.ResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceTest {

    @Mock
    private ResultService resultService;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private AiClientFactory aiClientFactory;

    @Mock
    private AiClient aiClient;

    private AiAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        aiAnalysisService = new AiAnalysisService(resultService, promptBuilder, aiClientFactory);
    }

    @Test
    void analyzeGlobal_WhenNoResults_ShouldReturnNoDataMessage() {
        when(resultService.getAll()).thenReturn(Collections.emptyList());

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("No hay suficientes datos para realizar un análisis con esos filtros.", result);
        verify(aiClientFactory, never()).getClient();
    }

    @Test
    void analyzeGlobal_WhenNullResults_ShouldReturnNoDataMessage() {
        when(resultService.getAll()).thenReturn(null);

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("No hay suficientes datos para realizar un análisis con esos filtros.", result);
    }

    @Test
    void analyzeGlobal_WhenValidResults_ShouldCallAiClient() {
        ResultDTO resultDTO = ResultDTO.builder()
                .personName("Juan Perez")
                .modalityName("Individual")
                .score(180)
                .branchId(1)
                .categoryId(1)
                .modalityId(1)
                .build();

        when(resultService.getAll()).thenReturn(List.of(resultDTO));
        when(promptBuilder.buildGlobal(anyList(), any(), any(), any())).thenReturn("Test prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("AI Analysis Result");

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("AI Analysis Result", result);
        verify(aiClientFactory).getClient();
        verify(aiClient).ask("Test prompt");
    }

    @Test
    void analyzeGlobal_WithBranchFilter_ShouldFilterResults() {
        ResultDTO result1 = ResultDTO.builder()
                .personName("Juan")
                .modalityName("Individual")
                .score(180)
                .branchId(1)
                .build();

        ResultDTO result2 = ResultDTO.builder()
                .personName("Maria")
                .modalityName("Individual")
                .score(190)
                .branchId(2)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1, result2));
        when(promptBuilder.buildGlobal(anyList(), eq(1), any(), any())).thenReturn("Filtered prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("Filtered Analysis");

        String result = aiAnalysisService.analyzeGlobal(1, null, null);

        assertEquals("Filtered Analysis", result);
        verify(promptBuilder).buildGlobal(anyList(), eq(1), isNull(), isNull());
    }

    @Test
    void analyzeGlobal_WithCategoryFilter_ShouldFilterResults() {
        ResultDTO result1 = ResultDTO.builder()
                .personName("Carlos")
                .modalityName("Dobles")
                .score(175)
                .categoryId(1)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1));
        when(promptBuilder.buildGlobal(anyList(), any(), eq(1), any())).thenReturn("Category prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("Category Analysis");

        String result = aiAnalysisService.analyzeGlobal(null, 1, null);

        assertEquals("Category Analysis", result);
    }

    @Test
    void analyzeGlobal_WithModalityFilter_ShouldFilterResults() {
        ResultDTO result1 = ResultDTO.builder()
                .personName("Ana")
                .modalityName("Trios")
                .score(185)
                .modalityId(1)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1));
        when(promptBuilder.buildGlobal(anyList(), any(), any(), eq(1))).thenReturn("Modality prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("Modality Analysis");

        String result = aiAnalysisService.analyzeGlobal(null, null, 1);

        assertEquals("Modality Analysis", result);
    }

    @Test
    void analyzeGlobal_WhenResultsWithNullScore_ShouldFilterThem() {
        ResultDTO result1 = ResultDTO.builder()
                .personName("Test")
                .modalityName("Individual")
                .score(null)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1));

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("No hay suficientes datos para realizar un análisis con esos filtros.", result);
    }

    @Test
    void analyzeGlobal_WithMultipleResultsSamePlayer_ShouldGroupThem() {
        ResultDTO result1 = ResultDTO.builder()
                .personName("Juan")
                .modalityName("Individual")
                .score(180)
                .build();

        ResultDTO result2 = ResultDTO.builder()
                .personName("Juan")
                .modalityName("Individual")
                .score(190)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1, result2));
        when(promptBuilder.buildGlobal(anyList(), any(), any(), any())).thenReturn("Grouped prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("Grouped Analysis");

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("Grouped Analysis", result);
    }

    @Test
    void analyzeGlobal_WithNullPersonName_ShouldHandleGracefully() {
        ResultDTO result1 = ResultDTO.builder()
                .personName(null)
                .modalityName("Individual")
                .score(180)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1));
        when(promptBuilder.buildGlobal(anyList(), any(), any(), any())).thenReturn("Null name prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("Analysis with null name");

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("Analysis with null name", result);
    }

    @Test
    void analyzeGlobal_WithBlankPersonName_ShouldHandleGracefully() {
        ResultDTO result1 = ResultDTO.builder()
                .personName("   ")
                .modalityName("Individual")
                .score(180)
                .build();

        when(resultService.getAll()).thenReturn(List.of(result1));
        when(promptBuilder.buildGlobal(anyList(), any(), any(), any())).thenReturn("Blank name prompt");
        when(aiClientFactory.getClient()).thenReturn(aiClient);
        when(aiClient.ask(anyString())).thenReturn("Analysis with blank name");

        String result = aiAnalysisService.analyzeGlobal(null, null, null);

        assertEquals("Analysis with blank name", result);
    }
}
