package com.bowlingpoints.service;

import com.bowlingpoints.dto.PlayerResultUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelService {
    List<PlayerResultUploadDTO> uploadResultsFromExcel(MultipartFile file);
}
