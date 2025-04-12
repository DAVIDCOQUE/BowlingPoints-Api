package com.bowlingpoints.service;

import com.bowlingpoints.util.Jugador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bowlingpoints.repository.ExcelRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ExcelService {

    @Autowired
    private ExcelRepository excelRepository;

    public List<Jugador> obtenerJugadoresDesdeArchivo(MultipartFile file) {
        return excelRepository.leerJugadoresDeExcel(file);
    }
}
