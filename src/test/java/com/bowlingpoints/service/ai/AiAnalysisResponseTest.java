package com.bowlingpoints.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiAnalysisResponseTest {

    @Test
    void shouldCreateResponseWithAnalysis() {
        AiAnalysisResponse response = new AiAnalysisResponse("Test analysis");
        assertEquals("Test analysis", response.analysis());
    }

    @Test
    void shouldHandleNullAnalysis() {
        AiAnalysisResponse response = new AiAnalysisResponse(null);
        assertNull(response.analysis());
    }

    @Test
    void shouldHandleEmptyAnalysis() {
        AiAnalysisResponse response = new AiAnalysisResponse("");
        assertEquals("", response.analysis());
    }
}
