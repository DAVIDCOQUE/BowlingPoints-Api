package com.bowlingpoints.service;

import com.bowlingpoints.dto.PlayerResultUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    // Método para subir y procesar un archivo Excel con resultados de jugadores
    List<PlayerResultUploadDTO> uploadResultsFromExcel(MultipartFile file);
}
