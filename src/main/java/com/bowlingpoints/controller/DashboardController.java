package com.bowlingpoints.controller;

import com.bowlingpoints.dto.DashboardDTO;
import com.bowlingpoints.dto.ResponseGenericDTO;
import com.bowlingpoints.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ResponseGenericDTO<DashboardDTO>> getDashboard() {
        DashboardDTO dashboard = dashboardService.getDashboardData();
        return ResponseEntity.ok(
                new ResponseGenericDTO<>(true, "Dashboard cargado correctamente", dashboard)
        );
    }
}
