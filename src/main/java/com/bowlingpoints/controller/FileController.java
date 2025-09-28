package com.bowlingpoints.controller;


import com.bowlingpoints.dto.PlayerResultUploadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bowlingpoints.service.ExcelService;
import com.bowlingpoints.util.Jugador;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file/process")
public class FileController {

    @Autowired
    private ExcelService jugadorService;

    @PostMapping("/upload")
    public List<PlayerResultUploadDTO> uploadResults(@RequestParam("file") MultipartFile file) {
        return jugadorService.uploadResultsFromExcel(file);
    }
}
