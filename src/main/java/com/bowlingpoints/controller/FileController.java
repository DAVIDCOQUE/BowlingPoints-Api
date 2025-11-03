package com.bowlingpoints.controller;


import com.bowlingpoints.dto.PlayerResultUploadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bowlingpoints.service.FileService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/file/process")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload-results")
    public List<PlayerResultUploadDTO> uploadResults(@RequestParam("file") MultipartFile file) {
        return fileService.uploadResultsFromExcel(file);
    }
}
